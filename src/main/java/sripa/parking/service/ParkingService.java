package sripa.parking.service;

import java.util.Map;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.Vehicle;

/**
 * Service that allows manipulating parking slots. Allows to take/free parking slots
 */
public interface ParkingService {

  /**
   * Queries the parking slot for the vehicle
   *
   * @param vehicle vehicle to park
   * @return ticket with initial fields setup if there is place available at parking
   * @throws sripa.parking.exceptions.NoSpaceException if no parking slot available
   */
  Ticket takeSlot(Vehicle vehicle);


  /**
   * Counts available slots of given type
   *
   * @param powerSupply type of slots to count
   * @return available places for given slot
   */
  Map<PowerSupply, Long> availableSlots(String powerSupply);
}
