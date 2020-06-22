package sripa.parking.service;

import java.util.Map;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.VehicleRequest;

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
  Ticket takeSlot(VehicleRequest vehicle);

  /**
   * Frees (gives up) parking slot taken by given ticket owner
   *
   * @param ticketId ticket of the parking slot occupier
   * @return ticket with price to pay setup
   * @throws sripa.parking.exceptions.InvalidTicketException in case if ticket cannot be processed
   */
  Ticket freeSlot(Long ticketId);

  /**
   * Counts available slots of given type
   *
   * @param powerSupply type of slots to count
   * @return available places for given slot
   */
  Map<PowerSupply, Long> availableSlots(String powerSupply);
}
