package sripa.parking.exceptions;

/**
 * Thrown in case if ticket is not known to the system or ticket is not valid
 */
public class InvalidTicketException extends ParkingException {

  public static final int CODE = 4006;
  private static final String ERR_MSG_FMT = "Failed to process ticket: %d";

  /**
   * Exception that is thrown in case of failure to process ticket with id
   *
   * @param ticketId ticket id
   */
  public InvalidTicketException(Long ticketId) {
    super(CODE, String.format(ERR_MSG_FMT, ticketId));
  }
}