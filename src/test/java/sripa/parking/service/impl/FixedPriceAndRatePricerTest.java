package sripa.parking.service.impl;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Price;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.config.ParkingSlotsConfig.PricingConfig;

public class FixedPriceAndRatePricerTest {

  private static final Float RATE = 2F;
  private static final Float FIXED = 5F;
  private FixedPriceAndRatePricer pricingService;

  @BeforeEach
  void setUp() {
    pricingService = new FixedPriceAndRatePricer(
        new ParkingSlotsConfig(emptyMap(), "", new PricingConfig("", RATE, FIXED)));
  }
  @Test
  void shouldChargeForZeroHours() {
    // Given
    int hours = 0;
    Long timeSpent = Duration.ofHours(hours).toMillis();

    // When
    final Price price = pricingService.price(timeSpent, PowerSupply.GASOLINE);

    // Then:
    assertEquals(BigDecimal.valueOf(FIXED), price.getAmount());
  }

}
