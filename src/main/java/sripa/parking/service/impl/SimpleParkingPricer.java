package sripa.parking.service.impl;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Price;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.service.ParkingPricer;

/**
 * Simplest possible {@link ParkingPricer} implementation. Charges for at minimum 1 hour, and plus
 * entire hours spent in parking. Price calculated base on hourly rate that is fixed for every hour
 */
@Component
public class SimpleParkingPricer implements ParkingPricer {

  /**
   * Hourly rate
   */
  private final BigDecimal hourlyRate;
  /**
   * Currency of price
   */
  private final String currency;

  @Autowired
  public SimpleParkingPricer(ParkingSlotsConfig config) {
    this.hourlyRate = BigDecimal.valueOf(config.getPricing().getHourlyRate());
    this.currency = config.getPricing().getCurrency();
  }

  @Override
  public Price price(long timeSpentMs, PowerSupply slotType) {
    var minutes = TimeUnit.MILLISECONDS.toMinutes(timeSpentMs);
    var hours = minutes / 60 + 1;
    var result = new Price();
    result.setAmount(hourlyRate.multiply(BigDecimal.valueOf(hours)));
    result.setCurrency(currency);
    return result;
  }
}
