package sripa.parking.repository;


import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sripa.parking.api.data.ParkingSlot;
import sripa.parking.api.data.PowerSupply;

/**
 * Data access  layer for {@link ParkingSlot}-s
 */
@Repository
public interface ParkingSlotRepository extends CrudRepository<ParkingSlot, String> {

  Optional<ParkingSlot> findFirstByPowerSupplyAndTaken(PowerSupply type, Boolean taken);

  Long countByPowerSupplyAndTaken(PowerSupply powerSupply, Boolean taken);
}
