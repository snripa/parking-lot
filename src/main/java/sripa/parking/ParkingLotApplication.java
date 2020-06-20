package sripa.parking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ParkingLotApplication {

  public static void main(String[] args) {
    SpringApplication.run(ParkingLotApplication.class, args);
  }
}
