package sripa.parking.service.impl;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Price;
import sripa.parking.config.ParkingSlotsConfig;
import sripa.parking.config.ParkingSlotsConfig.PricingConfig;
import sripa.parking.exceptions.PricingException;
import sripa.parking.service.PricingService;

class SimplePricingServiceTest {

  private static final String CURRENCY = "TEST";
  private static final Float RATE = 1F;
  private PricingService pricingService;

  @BeforeEach
  void setUp() {
    pricingService = new SimplePricingService(
        new ParkingSlotsConfig(emptyMap(), "", new PricingConfig(CURRENCY, RATE)));
  }

  @Test
  void shouldPriceProperly() {
    // Given
    int hours = 1;
    Long timeSpent = Duration.ofHours(hours).toMillis();

    // When
    final Price price = pricingService.price(timeSpent, PowerSupply.GASOLINE);

    // Then:
    assertEquals(BigDecimal.valueOf((hours + 1) * RATE), price.getAmount());
  }

  @Test
  void shouldChargeForZeroHours() {
    // Given
    int hours = 0;
    Long timeSpent = Duration.ofHours(hours).toMillis();

    // When
    final Price price = pricingService.price(timeSpent, PowerSupply.GASOLINE);

    // Then:
    assertEquals(BigDecimal.valueOf(RATE), price.getAmount());
  }

  @Test
  void shouldFailForNegativeTime() {
    assertThrows(PricingException.class, () -> pricingService.price(-1L, PowerSupply.GASOLINE));
  }
}