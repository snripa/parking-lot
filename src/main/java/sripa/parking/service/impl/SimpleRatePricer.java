package sripa.parking.service.impl;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.service.PricingService;

/**
 * Simplest possible {@link PricingService} implementation. Charges for at minimum 1 hour, and plus
 * entire hours spent in parking. Price calculated base on hourly rate that is fixed for every hour
 */
@Slf4j
@Component
@ConditionalOnProperty(
    value = "parking.pricing.policy",
    havingValue = "SIMPLE")
public class SimpleRatePricer extends RateBasedPricer {

  @Autowired
  public SimpleRatePricer(ParkingSlotsConfig config) {
    super(config.getPricing().getHourlyRate(), 0F, config.getPricing().getCurrency());
  }

  @PostConstruct
  private void init() {
    log.info("SIMPLE pricing policy in place. Pricing with (hours + 1)  x {}", this.hourlyRate);
  }

}
