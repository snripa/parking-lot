package sripa.parking.service;

import sripa.parking.api.data.ParkingSlot;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.Vehicle;

/**
 * Track tickets for parking lot.
 */
public interface TicketingService {

  /**
   * Once parking slot is taken, this method is responsible to create ticket in the system to keep
   * track of taken slot.
   *
   * @param vehicle vehicle that takes slot
   * @param slot slot taken
   * @return ticket with check in time set and slot assigned
   */
  Ticket checkIn(Vehicle vehicle, ParkingSlot slot);

  /**
   * Finalizes current ticket. Also releases the spot that was assigned to this ticket. The
   * resulting ticket will contain checkout time and price to pay for parking.
   *
   * @param ticketId ticket id to check out
   * @return ticket with price to pay setup
   * @throws sripa.parking.exceptions.InvalidTicketException in case if ticket cannot be processed
   */
  Ticket checkOut(Long ticketId);
}
