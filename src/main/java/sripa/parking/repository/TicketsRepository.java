package sripa.parking.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import sripa.parking.api.data.Ticket;

/**
 * Data access  layer for {@link Ticket}-s
 */
@Repository
public interface TicketsRepository extends CrudRepository<Ticket, Long> {

}
