package sripa.parking.api.data;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class Ticket {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private LocalDateTime checkIn;
  private LocalDateTime checkOut;
  @OneToOne(cascade = CascadeType.ALL)
  private Price price;
  @OneToOne(cascade = CascadeType.ALL)
  private ParkingSlot slot;
  @OneToOne(cascade = CascadeType.ALL)
  private Vehicle vehicle;
}
