package sripa.parking.exceptions;

public class UnsupportedSlotTypeException extends ParkingException {

  public static final int CODE = ErrorCodes.UNSUPPORTED_SLOT;
  private static final String ERR_MSG_FMT = "Slot type not supported: %s";

  /**
   * Exception that is thrown if pricing attempt has failed
   */
  public UnsupportedSlotTypeException(String slotType) {
    super(CODE, String.format(ERR_MSG_FMT, slotType));
  }
}