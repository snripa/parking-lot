package sripa.parking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.Vehicle;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class IntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  public IntegrationTest() {
  }

  @Test
  void shouldTakePlace() {
    var vehicle = new Vehicle();
    vehicle.setPlates("AB123");
    vehicle.setType(PowerSupply.GASOLINE);
    ResponseEntity<Ticket> response = restTemplate
        .postForEntity("/takeSlot", vehicle, Ticket.class);
  }
}
