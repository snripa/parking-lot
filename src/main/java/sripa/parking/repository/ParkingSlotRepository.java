package sripa.parking.repository;


import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sripa.parking.api.data.ParkingSlot;
import sripa.parking.api.data.PowerSupply;

/**
 * Data access  layer for {@link ParkingSlot}-s
 */
@Repository
public interface ParkingSlotRepository extends CrudRepository<ParkingSlot, String> {

  List<ParkingSlot> findByTypeAndTaken(PowerSupply type, Boolean taken);

  List<ParkingSlot> findByTaken(Boolean taken);
}
