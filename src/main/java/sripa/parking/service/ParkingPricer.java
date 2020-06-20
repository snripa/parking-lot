package sripa.parking.service;

import sripa.parking.api.data.PowerSupply;
import sripa.parking.api.data.Price;

/**
 * Calculates price of parking
 */
public interface ParkingPricer {

  /**
   * Calculate price of parking based on time taken and slot type
   *
   * @param timeSpentMs time parking slot has been taken
   * @param slotType type of taken slot
   */
  Price price(long timeSpentMs, PowerSupply slotType);

}
