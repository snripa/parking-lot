package sripa.parking.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import sripa.parking.api.data.Vehicle;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.config.ParkingSlotsConfig.PricingConfig;
import sripa.parking.exceptions.NoSpaceException;
import sripa.parking.repository.ParkingSlotRepository;
import sripa.parking.service.TicketingService;

class PersistentParkingServiceTest {

  public static final int CAPACITY = 1;
  public static final PowerSupply AVAILABLE_TYPE = PowerSupply.GASOLINE;

  @Mock
  private ParkingSlotRepository slotsRepository;
  @Mock
  private TicketingService ticketingService;
  @Captor
  private ArgumentCaptor<ParkingSlot> slotCaptor;
  @Captor
  private ArgumentCaptor<Vehicle> vehicleCaptor;
  private PersistentParkingService parkingService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    ParkingSlotsConfig config = new ParkingSlotsConfig(Map.of(AVAILABLE_TYPE.name(), CAPACITY), "",
        new PricingConfig());
    parkingService = new PersistentParkingService(slotsRepository, config, ticketingService);
  }

  @Test
  void shouldTakeSlot() {
    // Given:
    final ParkingSlot availableSlot = new ParkingSlot();
    availableSlot.setPowerSupply(PowerSupply.GASOLINE);
    availableSlot.setId(123L);
    Vehicle vehicle = new Vehicle("123", PowerSupply.GASOLINE);
    when(slotsRepository.findFirstByPowerSupplyAndTaken(any(), any()))
        .thenReturn(Optional.of(availableSlot));

    // When:
    parkingService.takeSlot(vehicle);

    // Then:
    verify(slotsRepository).findFirstByPowerSupplyAndTaken(any(), any());
    verify(slotsRepository).save(slotCaptor.capture());
    assertTrue(slotCaptor.getValue().getTaken(), "should have called with taken=true");
    verify(ticketingService).checkIn(vehicleCaptor.capture(), eq(availableSlot));
    assertEquals(vehicle.getPlates(), vehicleCaptor.getValue().getPlates());
  }

  @Test
  void shouldFailIfNoSlot() {
    // Given:
    Vehicle vehicle = new Vehicle("123", PowerSupply.GASOLINE);
    when(slotsRepository.findFirstByPowerSupplyAndTaken(any(), any())).thenReturn(Optional.empty());

    // When/Then
    assertThrows(NoSpaceException.class, () -> parkingService.takeSlot(vehicle),
        "Expected exception in case no space available");
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