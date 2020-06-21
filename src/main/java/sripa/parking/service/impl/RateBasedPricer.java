package sripa.parking.service.impl;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Price;
import sripa.parking.exceptions.PricingException;
import sripa.parking.service.PricingService;

@Slf4j
abstract class RateBasedPricer implements PricingService {

  /**
   * Hourly rate
   */
  protected final BigDecimal hourlyRate;
  /**
   * Fixed price to be added on top of total price
   */
  protected final BigDecimal fixedPrice;
  /**
   * Currency of price
   */
  protected final String currency;

  @Autowired
  public RateBasedPricer(Float hourlyRate, Float fixedPrice, String currency) {
    this.hourlyRate = BigDecimal.valueOf(hourlyRate);
    this.fixedPrice = BigDecimal.valueOf(fixedPrice);
    this.currency = currency;
  }

  @Override
  public Price price(Long timeSpentMs, PowerSupply slotType) {
    log.info("Calculating price for slot {}", slotType);
    if (timeSpentMs < 0) {
      throw new PricingException();
    }
    var minutes = TimeUnit.MILLISECONDS.toMinutes(timeSpentMs);
    var hours = minutes / 60;
    var result = new Price();
    result.setAmount(hourlyRate.multiply(BigDecimal.valueOf(hours)).add(fixedPrice));
    result.setCurrency(currency);
    return result;
  }
}
