package sripa.parking.config;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "parking")
public class ParkingSlotsConfig {

  private final Map<String, Integer> slots;

  private final String name;

  private final PricingConfig pricing;

  public ParkingSlotsConfig(Map<String, Integer> slots, String name, PricingConfig pricing) {
    this.slots = slots;
    this.name = name;
    this.pricing = pricing;
  }

  public Map<String, Integer> getSlots() {
    return slots;
  }

  public String getName() {
    return name;
  }

  public PricingConfig getPricing() {
    return pricing;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PricingConfig {

    String currency;
    Float hourlyRate;
  }
}