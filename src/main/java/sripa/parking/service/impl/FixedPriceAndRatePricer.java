package sripa.parking.service.impl;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import sripa.parking.config.ParkingSlotsConfig;

@Slf4j
@Component
@ConditionalOnProperty(
    value = "parking.pricing.policy",
    havingValue = "FIXED")
public class FixedPriceAndRatePricer extends RateBasedPricer {

  @Autowired
  public FixedPriceAndRatePricer(ParkingSlotsConfig config) {
    super(config.getPricing().getHourlyRate(), config.getPricing().getFixedRate(),
        config.getPricing().getCurrency());
  }

  @PostConstruct
  private void init() {
    log.info("FIXED pricing policy in place. Pricing with {} + hours x {}", fixedPrice, hourlyRate);
  }
}
