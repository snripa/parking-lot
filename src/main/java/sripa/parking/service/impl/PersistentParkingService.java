package sripa.parking.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sripa.parking.api.data.ParkingSlot;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.Vehicle;
import sripa.parking.api.data.VehicleRequest;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.exceptions.InvalidTicketException;
import sripa.parking.exceptions.NoSpaceException;
import sripa.parking.repository.ParkingSlotRepository;
import sripa.parking.repository.TicketsRepository;
import sripa.parking.service.ParkingService;
import sripa.parking.service.PricingService;

/**
 * Implementation of {@link ParkingService} that takes care of persistency. State is saved in DB
 * (depending on data source implementation)
 */
@Slf4j
@Service
public class PersistentParkingService implements ParkingService {

  private final ParkingSlotRepository slotRepository;
  private final TicketsRepository ticketsRepository;
  private final String name;
  private final PricingService pricingService;
  private final Map<String, Integer> slots;

  @Autowired
  public PersistentParkingService(ParkingSlotRepository slotRepository,
      TicketsRepository ticketsRepository, ParkingSlotsConfig config,
      PricingService pricingService) {
    this.slotRepository = slotRepository;
    this.ticketsRepository = ticketsRepository;
    this.name = config.getName();
    this.pricingService = pricingService;
    this.slots = config.getSlots();
  }

  @PostConstruct
  private void init() {
    log.info("Initializing storage with available parking slots: {} ", slots);
    slots.entrySet()
        .stream()
        .filter(e -> PowerSupply.contains(e.getKey()))
        .forEach(e -> {
          final String type = e.getKey();
          final Integer capacity = e.getValue();
          IntStream.range(0, capacity).forEach(i -> {
            var slot = new ParkingSlot();
            slot.setTaken(false);
            slot.setType(PowerSupply.valueOf(type));
            this.slotRepository.save(slot);
          });
        });
    log.info("Persistent Parking service {} started  with initial capacity {}",
        name, slots);
  }


  @Override
  public Ticket takeSlot(VehicleRequest vehicle) {
    log.info("Attempt to reserve slot for {}", vehicle);
    // have to synchronize slots repository access
    ParkingSlot availableSlot;
    synchronized (this) {
      // First find available slot of given type
      availableSlot = slotRepository
          .findByTypeAndTaken(vehicle.getType(), false)
          .stream()
          .findFirst()
          .orElseThrow(() -> new NoSpaceException(vehicle.getType()));
      // Mark it as taken and save to db
      availableSlot.setTaken(true);
      slotRepository.save(availableSlot);
      log.info("Slot taken: {}", availableSlot);
    }
    // Create ticket with checkIn/slot/vehicle assigned
    var persistVehicle = new Vehicle();
    persistVehicle.setPlates(vehicle.getPlates());
    persistVehicle.setType(vehicle.getType());
    var ticket = new Ticket();
    ticket.setCheckIn(LocalDateTime.now());
    ticket.setSlot(availableSlot);
    ticket.setVehicle(persistVehicle);
    // And save to db
    final Ticket savedTicket = ticketsRepository.save(ticket);
    log.info("Ticket created: {}. Parking slot {} is taken", ticket.getId(), ticket.getSlot());
    return savedTicket;
  }

  @Override
  public Ticket freeSlot(Long ticketId) {
    // Check if ticket id is valid and not payed yet
    var ticketOptional = ticketsRepository.findById(ticketId);
    if (ticketOptional.isEmpty()) {
      log.warn("Ticket {} doesn't exist in system", ticketId);
      throw new InvalidTicketException(ticketId);
    }
    if (ticketOptional.get().getCheckOut() != null) {
      log.warn("Ticket {} was already finalized", ticketId);
      throw new InvalidTicketException(ticketId);
    }

    // Mark the slot as free and save
    final var tkt = ticketOptional.get();
    // Set price according to policy and save the ticket
    var checkOut = LocalDateTime.now();
    long timeTakenMs = tkt.getCheckIn().until(checkOut, ChronoUnit.MILLIS);
    var price = pricingService.price(timeTakenMs, tkt.getSlot().getType());

    // save ticket back with updated fields (slot will be saved as well)
    tkt.setCheckOut(checkOut);
    tkt.setPrice(price);
    tkt.getSlot().setTaken(false);
    final Ticket savedTicket = ticketsRepository.save(tkt);
    log.info("Checking out for ticket {} complete. Slot {} is free now", savedTicket.getId(),
        savedTicket.getSlot().getId());
    return savedTicket;
  }

  @Override
  public Map<PowerSupply, Integer> availableSlots() {
    log.info("Counting available slots...");
    final List<ParkingSlot> freeSlots = slotRepository.findByTaken(false);
    // count places and return in map
    var map = freeSlots.stream().collect(Collectors.groupingBy(ParkingSlot::getType));
    final Map<PowerSupply, Integer> result = map.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().size()));
    // fill with zero-s missing types
    Arrays.stream(PowerSupply.values()).filter(t -> !result.containsKey(t))
        .forEach(t -> result.put(t, 0));
    log.info("Calculation complete");
    return result;
  }
}
