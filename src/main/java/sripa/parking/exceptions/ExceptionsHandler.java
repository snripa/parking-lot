package sripa.parking.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sripa.parking.api.data.ErrorResponse;

@RestControllerAdvice
public class ExceptionsHandler {

  @ExceptionHandler(value = {ParkingException.class})
  public ResponseEntity<ErrorResponse> handleParkingException(
      final ParkingException exception) {
    return new ResponseEntity<>(
        new ErrorResponse(exception.getCode(), exception.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
