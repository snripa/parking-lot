package sripa.parking.api.data;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Price {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String currency;
  private BigDecimal amount;
}
