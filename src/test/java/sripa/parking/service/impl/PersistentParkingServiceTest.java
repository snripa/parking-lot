package sripa.parking.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
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
import sripa.parking.api.data.VehicleRequest;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.config.ParkingSlotsConfig.PricingConfig;
import sripa.parking.exceptions.InvalidTicketException;
import sripa.parking.exceptions.NoSpaceException;
import sripa.parking.repository.ParkingSlotRepository;
import sripa.parking.repository.TicketsRepository;
import sripa.parking.service.PricingService;

class PersistentParkingServiceTest {

  public static final int CAPACITY = 1;
  public static final PowerSupply AVAILABLE_TYPE = PowerSupply.GASOLINE;

  @Mock
  private ParkingSlotRepository slotsRepository;
  @Mock
  private TicketsRepository ticketsRepository;
  @Mock
  private PricingService pricingService;
  @Captor
  private ArgumentCaptor<Ticket> ticketCaptor;
  @Captor
  private ArgumentCaptor<ParkingSlot> slotCaptor;

  private PersistentParkingService parkingService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    ParkingSlotsConfig config = new ParkingSlotsConfig(Map.of(AVAILABLE_TYPE.name(), CAPACITY), "",
        new PricingConfig());
    parkingService = new PersistentParkingService(slotsRepository, ticketsRepository, config,
        pricingService);
  }

  @Test
  void shouldTakeSlot() {
    // Given:
    final ParkingSlot availableSlot = new ParkingSlot();
    VehicleRequest vehicle = new VehicleRequest("123", PowerSupply.GASOLINE);
    when(slotsRepository.findFirstByPowerSupplyAndTaken(any(), any()))
        .thenReturn(Optional.of(availableSlot));

    // When:
    parkingService.takeSlot(vehicle);

    // Then:
    verify(slotsRepository).findFirstByPowerSupplyAndTaken(any(), any());
    verify(slotsRepository).save(slotCaptor.capture());
    assertTrue(slotCaptor.getValue().getTaken(), "should have called with taken=true");
    verify(ticketsRepository).save(ticketCaptor.capture());
    assertEquals(availableSlot, ticketCaptor.getValue().getSlot());
    assertEquals(vehicle.getPlates(), ticketCaptor.getValue().getVehicle().getPlates());
  }

  @Test
  void shouldFailIfNoSlot() {
    // Given:
    VehicleRequest vehicle = new VehicleRequest("123", PowerSupply.GASOLINE);
    when(slotsRepository.findFirstByPowerSupplyAndTaken(any(), any())).thenReturn(Optional.empty());

    // When/Then
    assertThrows(NoSpaceException.class, () -> parkingService.takeSlot(vehicle),
        "Expected exception in case no space available");
  }

  @Test
  void shouldFreeSlot() {
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
    parkingService.freeSlot(ticketId);

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
  void shouldFailIfInvalidTicket() {
    // Given:

    when(ticketsRepository.findById(any())).thenReturn(Optional.empty());

    // When/Then
    assertThrows(InvalidTicketException.class, () -> parkingService.freeSlot(0L),
        "Expected exception in case when ticket does not exist");
  }

  @Test
  void shouldFailIfTicketAlreadyPayed() {
    // Given:
    var ticket = new Ticket();
    ticket.setCheckOut(LocalDateTime.now());
    when(ticketsRepository.findById(any())).thenReturn(Optional.of(ticket));

    // When/Then
    assertThrows(InvalidTicketException.class, () -> parkingService.freeSlot(0L),
        "Expected exception in ticket was already processed once");
  }

  @Test
  void shouldGetAvailableSlots() {
    // Given
    when(slotsRepository.countByPowerSupplyAndTaken(any(), any())).thenReturn(1L);

    // When
    final Map<PowerSupply, Long> result = parkingService.availableSlots(AVAILABLE_TYPE.name());

    // Then
    verify(slotsRepository).countByPowerSupplyAndTaken(eq(AVAILABLE_TYPE), eq(false));
    assertEquals(1, result.get(AVAILABLE_TYPE));
  }
}