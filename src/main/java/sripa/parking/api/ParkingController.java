package sripa.parking.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sripa.parking.api.data.ErrorResponse;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Ticket;
import sripa.parking.api.data.TicketId;
import sripa.parking.api.data.Vehicle;
import sripa.parking.service.ParkingService;
import sripa.parking.service.TicketingService;

@RestController
@RequestMapping("/")
public class ParkingController {

  @Autowired
  private ParkingService parkingService;

  @Autowired
  private TicketingService ticketingService;

  @Operation(summary = "Take a parking slot if available and receive a ticket")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Parking slot found",
          content = {
              @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class))}),
      @ApiResponse(
          responseCode = "400",
          description = "No parking spots available",
          content = {
              @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
  @PostMapping(path = "/takeSlot", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Ticket> takeSlot(@RequestBody Vehicle vehicle) {
    var ticket = parkingService.takeSlot(vehicle);
    return ResponseEntity.ok(ticket);
  }


  @Operation(summary = "Free a parking slot and get ticket with price finalized")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Parking slot released and ticket received",
          content = {
              @Content(mediaType = "application/json", schema = @Schema(implementation = Ticket.class))}),
      @ApiResponse(
          responseCode = "400",
          description = "Ticket invalid or already processed",
          content = {
              @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
  @PostMapping(path = "/freeSlot", consumes = "application/json", produces = "application/json")
  public ResponseEntity<Ticket> freeSlot(@RequestBody TicketId ticketId) {
    var ticket = ticketingService.checkOut(ticketId.getTicketId());
    return ResponseEntity.ok(ticket);
  }

  @Operation(summary = "Get available parking lots for given power supply type ")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "slot type to number of available slots mapping (i e {'GASOLINE' : 43})",
          content = {@Content(mediaType = "application/json")}),
      @ApiResponse(
          responseCode = "400",
          description = "Slot type not supported",
          content = {
              @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))})})
  @GetMapping(path = "/availableSlots/{powerSupply}", produces = "application/json")
  public ResponseEntity<Map<PowerSupply, Long>> availableSlots(
      @PathVariable String powerSupply) {
    return ResponseEntity.ok(parkingService.availableSlots(powerSupply));
  }
}
