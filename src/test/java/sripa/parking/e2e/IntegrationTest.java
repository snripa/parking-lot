package sripa.parking.e2e;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.TicketId;
import sripa.parking.api.data.Vehicle;
import sripa.parking.config.ParkingSlotsConfig;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class IntegrationTest {

  @LocalServerPort
  private int port;

  private ParkingSlotsConfig config;

  @Autowired
  private TestRestTemplate restTemplate;

  final Map<PowerSupply, Integer> initialSlots;

  @Autowired
  public IntegrationTest(ParkingSlotsConfig config) {
    this.config = config;
    this.initialSlots = config.getSlots().entrySet().stream()
        .collect(toMap(e -> PowerSupply.valueOf(e.getKey()), Entry::getValue));
  }

  @Test
  void shouldTakeAndFreeSlot() {
    // Given:
    var vehicle = new Vehicle("AB-123-CD", PowerSupply.GASOLINE);
    var slotsBefore = initialSlots.get(vehicle.getPowerSupply());

    // When: take slot success
    var responseTake = takeSlot(vehicle);
    assertEquals(HttpStatus.OK, responseTake.getStatusCode());
    Ticket ticketBefore = responseTake.getBody();
    assertNotNull(ticketBefore, "no response");

    // Then: check slots number after taking slot and ticket
    var actualSlots = availableSlots(vehicle.getPowerSupply());
    assertEquals(slotsBefore - 1, actualSlots, "Didn't reserve slot");
    assertNull(ticketBefore.getCheckOut(), "should not checkout yet");

    // When: free slot
    var responseFree = freeSlot(new TicketId(ticketBefore.getId()));
    assertEquals(HttpStatus.OK, responseFree.getStatusCode());
    Ticket ticketAfter = responseFree.getBody();
    assertNotNull(ticketAfter, "no response");

    // Then:
    actualSlots = availableSlots(vehicle.getPowerSupply());
    assertEquals(slotsBefore, actualSlots, "Slot was not freed");
    assertNotNull(ticketAfter.getCheckOut(), "should checkout");
    assertEquals(BigDecimal.valueOf(0F),
        ticketAfter.getPrice().getAmount(),
        "should have charged for 1 hour");
  }

  @Test
  void shouldFailInvalidTicket() {
    // Given: invalid ticket
    var ticketId = new TicketId(-5L);

    // When: try to checkout with invalid ticket
    final ResponseEntity<Ticket> ticketResponseEntity = freeSlot(ticketId);

    // Then
    assertEquals(HttpStatus.BAD_REQUEST, ticketResponseEntity.getStatusCode(),
        "should have received bad request for invalid ticket");
  }

  @Test
  void shouldFailPayedTicket() {
    // Given: get used ticket
    var ticket = takeSlot(new Vehicle("asd", PowerSupply.ELECTRIC_50KW)).getBody();
    freeSlot(new TicketId(ticket.getId()));

    // When: free same spot again
    var response = freeSlot(new TicketId(ticket.getId()));

    // Then: check slots number after taking slot and ticket
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "should have failed to checkout with the same ticket twice");
  }

  @Test
  void shouldFailIfNoSpace() {
    // Given: take all slots
    var type = PowerSupply.ELECTRIC_20KW;
    // GASOLINE slots are also available for electric cars
    var available = availableSlots(PowerSupply.ELECTRIC_20KW)  + availableSlots(PowerSupply.GASOLINE);
    IntStream.range(0, available).forEach(i -> takeSlot(new Vehicle("1234-" + i, type)));

    // When: take slot success
    var response = takeSlot(new Vehicle("ABC", type));

    // Then: check slots number after taking slot and ticket
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(),
        "should have received bad request for unavailable parking slot");
  }

  private ResponseEntity<Ticket> takeSlot(Vehicle vehicle) {
    return restTemplate
        .postForEntity("/takeSlot", vehicle, Ticket.class);
  }

  private ResponseEntity<Ticket> freeSlot(TicketId ticket) {
    return restTemplate
        .postForEntity("/freeSlot", ticket, Ticket.class);
  }

  @SuppressWarnings("rawtypes")
  private Integer availableSlots(PowerSupply powerSupply) {
    // allow raw types and npe risk as test will fail loudly
    final Map response = restTemplate
        .getForEntity("/availableSlots/" + powerSupply, Map.class)
        .getBody();
    return (Integer) requireNonNull(response).getOrDefault(powerSupply.name(), 0);
  }

}
