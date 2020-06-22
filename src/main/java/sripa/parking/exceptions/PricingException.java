package sripa.parking.exceptions;

public class PricingException extends ParkingException {

  public static final int CODE = ErrorCodes.PRICING_FAILED;
  private static final String ERR_MSG_FMT = "Failed to calculate price";

  /**
   * Exception that is thrown if pricing attempt has failed
   */
  public PricingException() {
    super(CODE, ERR_MSG_FMT);
  }
}