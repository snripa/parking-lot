package sripa.parking.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Utils {

  public static LocalDateTime toDateTime(long millis) {
    Instant instant = Instant.ofEpochMilli(millis);
    return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
  }
}
