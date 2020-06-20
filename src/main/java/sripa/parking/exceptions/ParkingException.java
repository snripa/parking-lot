package sripa.parking.exceptions;

public abstract class ParkingException extends RuntimeException {

  private final int code;

  public ParkingException(int code, String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return this.code;
  }
}
