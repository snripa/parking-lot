package sripa.parking.api.data;

import lombok.Data;

@Data
public class VehicleRequest {

  private String plates;
  private PowerSupply type;

}
