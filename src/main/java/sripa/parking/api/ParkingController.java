package sripa.parking.api;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.TicketId;
import sripa.parking.api.data.VehicleRequest;
import sripa.parking.service.ParkingService;

@RestController
@RequestMapping("/")
public class ParkingController {

  @Autowired
  private ParkingService parkingService;

  @PostMapping(path = "/takeSlot", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Ticket> takeSlot(@RequestBody VehicleRequest vehicle) {
    var ticket = parkingService.takeSlot(vehicle);
    return ResponseEntity.ok(ticket);
  }

  @PostMapping(path = "/freeSlot", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Ticket> freeSlot(@RequestBody TicketId ticketId) {
    var ticket = parkingService.freeSlot(ticketId.getTicketId());
    return ResponseEntity.ok(ticket);
  }

  @GetMapping(path = "/availableSlots", produces = "application/json")
  public ResponseEntity<Map<PowerSupply, Integer>> availableSlots() {
    return ResponseEntity.ok(parkingService.availableSlots());
  }
}
