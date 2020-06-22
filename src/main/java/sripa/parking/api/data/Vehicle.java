package sripa.parking.api.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Vehicle {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String plates;
  private PowerSupply powerSupply;

  public Vehicle(String plates, PowerSupply powerSupply) {
    this.plates = plates;
    this.powerSupply = powerSupply;
  }
}
