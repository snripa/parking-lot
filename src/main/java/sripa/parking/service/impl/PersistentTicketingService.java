package sripa.parking.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sripa.parking.api.data.ParkingSlot;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.Vehicle;
import sripa.parking.exceptions.InvalidTicketException;
import sripa.parking.repository.TicketsRepository;
import sripa.parking.service.PricingService;
import sripa.parking.service.TicketingService;

@Slf4j
@Component
public class PersistentTicketingService implements TicketingService {


  private final TicketsRepository ticketsRepository;
  private final PricingService pricingService;

  @Autowired
  public PersistentTicketingService(TicketsRepository ticketsRepository,
      PricingService pricingService) {
    this.ticketsRepository = ticketsRepository;
    this.pricingService = pricingService;
  }

  @Override
  public Ticket checkIn(Vehicle vehicle, ParkingSlot slot) {
    var ticket = new Ticket();
    ticket.setCheckIn(LocalDateTime.now());
    ticket.setSlot(slot);
    ticket.setVehicle(vehicle);
    // And save to db
    final Ticket savedTicket = ticketsRepository.save(ticket);
    log.info("Ticket created: {}. Parking slot {} is taken", ticket.getId(), ticket.getSlot());
    return savedTicket;
  }

  @Override
  public Ticket checkOut(Long ticketId) {
    // Check if ticket id is valid and not payed yet
    var ticketOptional = ticketsRepository.findById(ticketId);
    if (ticketOptional.isEmpty()) {
      log.warn("The ticket {} doesn't exist in the system", ticketId);
      throw new InvalidTicketException(ticketId);
    }
    if (ticketOptional.get().getCheckOut() != null) {
      log.warn("The ticket {} has been already finalized", ticketId);
      throw new InvalidTicketException(ticketId);
    }
    var ticket = ticketOptional.get();
    // Set price according to policy and save the ticket
    var checkOut = LocalDateTime.now();
    long timeTakenMs = ticket.getCheckIn().until(checkOut, ChronoUnit.MILLIS);
    var price = pricingService.price(timeTakenMs, ticket.getSlot().getPowerSupply());
    // save ticket back with updated fields (slot will be saved as well)
    ticket.setCheckOut(checkOut);
    ticket.setPrice(price);
    ticket.getSlot().setTaken(false);
    final Ticket savedTicket = ticketsRepository.save(ticket);
    log.info("Checking out for ticket {} complete. Slot {} is free now", savedTicket.getId(),
        savedTicket.getSlot().getId());
    return savedTicket;
  }
}
