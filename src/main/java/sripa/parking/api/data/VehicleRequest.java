package sripa.parking.api.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleRequest {

  private String plates;
  private PowerSupply type;

}
