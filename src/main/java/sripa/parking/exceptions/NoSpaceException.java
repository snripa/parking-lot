package sripa.parking.exceptions;

import sripa.parking.api.data.PowerSupply;

/**
 * Thrown in case no parking slot of given type was found
 */
public class NoSpaceException extends ParkingException {

  public static final int CODE = ErrorCodes.NO_SPACE;
  private static final String ERR_MSG_FMT = "No available spot for cars with power supply %s";

  public NoSpaceException(PowerSupply type) {
    super(CODE, String.format(ERR_MSG_FMT, type.name()));
  }

}
