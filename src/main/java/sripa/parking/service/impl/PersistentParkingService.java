package sripa.parking.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import sripa.parking.service.ParkingPricer;
import sripa.parking.service.ParkingService;

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
  private final ParkingPricer pricer;
  private final Map<String, Integer> slots;

  @Autowired
  public PersistentParkingService(ParkingSlotRepository slotRepository,
      TicketsRepository ticketsRepository, ParkingSlotsConfig config,
      ParkingPricer pricer) {
    this.slotRepository = slotRepository;
    this.ticketsRepository = ticketsRepository;
    this.name = config.getName();
    this.pricer = pricer;
    this.slots = config.getSlots();
  }

  @PostConstruct
  private void init() {
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
    log.info("Persistent Parking service started to serve parking {} with initial capacity {}",
        name, slots);
  }


  @Override
  public Ticket takeSlot(VehicleRequest vehicle) {
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
    return ticketsRepository.save(ticket);
  }

  @Override
  public Ticket freeSlot(Long ticketId) {
    // Check if ticket id is valid and not payed yet
    var ticketOptional = ticketsRepository.findById(ticketId);
    if (ticketOptional.isEmpty() || ticketOptional.get().getCheckOut() != null) {
      throw new InvalidTicketException(ticketId);
    }

    // Mark the slot as free and save
    final var tkt = ticketOptional.get();
    var slot = tkt.getSlot();
    slot.setTaken(false);
    slotRepository.save(slot);
    // Set price according to policy and save the ticket
    var checkOut = LocalDateTime.now();
    long msTaken = tkt.getCheckIn().until(checkOut, ChronoUnit.MILLIS);
    var price = pricer.price(msTaken, tkt.getSlot().getType());
    tkt.setCheckOut(checkOut);
    tkt.setPrice(price);
    return ticketsRepository.save(tkt);
  }

  @Override
  public Map<String, Integer> availableSlots() {
    final List<ParkingSlot> freeSlots = slotRepository.findByTaken(false);
    // count places and return in map
    var map = freeSlots.stream().collect(Collectors.groupingBy(ParkingSlot::getType));
    final Map<String, Integer> result = map.entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey().name(), e -> e.getValue().size()));
    Arrays.stream(PowerSupply.values()).map(Enum::name).filter(t -> !result.containsKey(t))
        .forEach(t -> result.put(t, 0));
    return result;
  }
}
