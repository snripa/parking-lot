package sripa.parking.service;

import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Price;

/**
 * Calculates price of parking
 */
public interface PricingService {

  /**
   * Calculate price of parking based on time taken and slot type
   *
   * @param timeSpentMs time parking slot has been taken
   * @param slotType type of taken slot
   * @throws sripa.parking.exceptions.PricingException in case if failed to calculate price
   */
  Price price(Long timeSpentMs, PowerSupply slotType);

}
