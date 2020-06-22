package sripa.parking.api.data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;


@Data
@Entity
public class ParkingSlot {

  private @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  Long id;
  @Enumerated(EnumType.STRING)
  private PowerSupply powerSupply;
  private Boolean taken;
}
