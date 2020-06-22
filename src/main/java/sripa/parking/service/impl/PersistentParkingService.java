package sripa.parking.service.impl;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sripa.parking.api.data.ParkingSlot;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.Vehicle;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.exceptions.NoSpaceException;
import sripa.parking.exceptions.UnsupportedSlotTypeException;
import sripa.parking.repository.ParkingSlotRepository;
import sripa.parking.service.ParkingService;
import sripa.parking.service.TicketingService;

/**
 * Implementation of {@link ParkingService} that takes care of persistency. State is saved in DB
 * (depending on data source implementation)
 */
@Slf4j
@Service
public class PersistentParkingService implements ParkingService {

  private final ParkingSlotRepository slotRepository;
  private final String name;
  private final TicketingService ticketingService;
  private final Map<String, Integer> slots;

  @Autowired
  public PersistentParkingService(ParkingSlotRepository slotRepository,
      ParkingSlotsConfig config,
      TicketingService ticketingService) {
    this.slotRepository = slotRepository;
    this.name = config.getName();
    this.slots = config.getSlots();
    this.ticketingService = ticketingService;
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
            slot.setPowerSupply(PowerSupply.valueOf(type));
            this.slotRepository.save(slot);
          });
        });
    log.info("Persistent Parking service {} started  with initial capacity {}",
        name, slots);
  }

  @Override
  public Ticket takeSlot(Vehicle vehicle) {
    log.info("Attempt to reserve slot for {}", vehicle);

    Optional<ParkingSlot> optSlot;
    ParkingSlot availableSlot;
    // have to synchronize access to slots repository
    synchronized (this) {
      // First find available slot of given type
      if (vehicle.getPowerSupply() == null || vehicle.getPowerSupply() == PowerSupply.GASOLINE) {
        optSlot = slotRepository.findFirstByPowerSupplyAndTaken(PowerSupply.GASOLINE, false);
      } else {
        optSlot = slotRepository
            .findFirstByPowerSupplyAndTaken(vehicle.getPowerSupply(), false)
            .or(() -> slotRepository.findFirstByPowerSupplyAndTaken(PowerSupply.GASOLINE, false));
      }
      // If no slot found - throw exception
      availableSlot = optSlot.orElseThrow(() -> new NoSpaceException(vehicle.getPowerSupply()));
      // Mark slot as taken and save to db
      availableSlot.setTaken(true);
      slotRepository.save(availableSlot);
      log.info("Slot taken: {}", availableSlot);
    }

    // Create vehicle to save
    var persistVehicle = new Vehicle();
    persistVehicle.setPlates(vehicle.getPlates());
    persistVehicle.setPowerSupply(vehicle.getPowerSupply());
    return ticketingService.checkIn(persistVehicle, availableSlot);
  }

  @Override
  public Map<PowerSupply, Long> availableSlots(String powerSupply) {
    if (!PowerSupply.contains(powerSupply)) {
      throw new UnsupportedSlotTypeException(powerSupply);
    }
    log.info("Counting available slots...");
    var result = slotRepository.countByPowerSupplyAndTaken(PowerSupply.valueOf(powerSupply), false);
    log.info("Calculation complete");
    return Map.of(PowerSupply.valueOf(powerSupply), result);
  }
}
