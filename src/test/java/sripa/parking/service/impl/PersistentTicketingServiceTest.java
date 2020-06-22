package sripa.parking.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sripa.parking.api.data.ParkingSlot;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Price;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.Vehicle;
import sripa.parking.exceptions.InvalidTicketException;
import sripa.parking.repository.TicketsRepository;
import sripa.parking.service.PricingService;
import sripa.parking.service.TicketingService;

class PersistentTicketingServiceTest {


  private static final PowerSupply AVAILABLE_TYPE = PowerSupply.GASOLINE;
  @Mock
  private TicketsRepository ticketsRepository;

  @Mock
  private PricingService pricingService;

  private TicketingService ticketingService;

  @Captor
  private ArgumentCaptor<Ticket> ticketCaptor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    ticketingService = new PersistentTicketingService(ticketsRepository, pricingService);
  }

  @Test
  void shouldCheckIn() {
    // Given:
    Vehicle vehicle = new Vehicle();
    vehicle.setPlates("qwe");
    vehicle.setPowerSupply(PowerSupply.GASOLINE);
    ParkingSlot slot = new ParkingSlot();
    slot.setId(5L);
    slot.setPowerSupply(PowerSupply.GASOLINE);

    // When:
    ticketingService.checkIn(vehicle, slot);

    // Then:
    verify(ticketsRepository).save(ticketCaptor.capture());
    var ticket = ticketCaptor.getValue();
    assertEquals(vehicle, ticket.getVehicle());
    assertEquals(slot, ticket.getSlot());
    assertNotNull(ticket.getCheckIn());
    assertNull(ticket.getCheckOut());
    assertNull(ticket.getPrice());
  }

  @Test
  void shouldCheckOut() {
    // Given: setup available slot and all related parameters
    ParkingSlot slot = new ParkingSlot();
    Long ticketId = 0L;
    Ticket ticket = new Ticket();
    ticket.setCheckIn(LocalDateTime.now().minusHours(1));
    Price price = new Price();
    slot.setPowerSupply(AVAILABLE_TYPE);
    ticket.setSlot(slot);
    when(ticketsRepository.findById(any())).thenReturn(Optional.of(ticket));
    when(ticketsRepository.save(any())).thenReturn(ticket);
    when(pricingService.price(any(), any())).thenReturn(price);

    // When
    ticketingService.checkOut(ticketId);

    // Then
    verify(ticketsRepository).findById(eq(ticketId));
    verify(pricingService).price(any(), eq(AVAILABLE_TYPE));
    verify(ticketsRepository).save(ticketCaptor.capture());
    Ticket ticketSaved = ticketCaptor.getValue();
    assertEquals(price, ticketSaved.getPrice());
    assertEquals(ticket.getCheckIn(), ticketSaved.getCheckIn());
    assertEquals(ticket.getSlot().getPowerSupply(), ticketSaved.getSlot().getPowerSupply());
    assertTrue(ticketSaved.getCheckOut().isAfter(ticket.getCheckIn()),
        "checkOut should be after checkIn");
  }

  @Test
  void shouldFailUnknownTicketId() {
    // Given:
    Long ticketId = -4L;
    when(ticketsRepository.findById(eq(ticketId))).thenReturn(Optional.empty());

    // When/Then
    assertThrows(InvalidTicketException.class, () -> ticketingService.checkOut(ticketId),
        "Expected exception in case when ticket does not exist");
  }

  @Test
  void shouldFailIfTicketAlreadyPayed() {
    // Given:
    var id = 5L;
    var ticket = new Ticket();
    ticket.setCheckOut(LocalDateTime.now());
    when(ticketsRepository.findById(eq(id))).thenReturn(Optional.of(ticket));

    // When/Then
    assertThrows(InvalidTicketException.class, () -> ticketingService.checkOut(id),
        "Expected exception in ticket was already processed once");
  }
}