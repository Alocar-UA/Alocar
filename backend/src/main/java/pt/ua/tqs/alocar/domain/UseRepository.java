package pt.ua.tqs.alocar.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UseRepository extends JpaRepository<Use, Long> {

    boolean existsByResidentIdAndEndedAtIsNull(Long residentId);

    /** Loads the scooter too: responses show its code after the transaction has closed. */
    @Override
    @EntityGraph(attributePaths = "scooter")
    Optional<Use> findById(Long id);

    @EntityGraph(attributePaths = "scooter")
    List<Use> findByResidentIdOrderByStartedAtDesc(Long residentId);
}
