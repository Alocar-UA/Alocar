package pt.ua.tqs.alocar.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubfleetRepository extends JpaRepository<Subfleet, Long> {

    Optional<Subfleet> findByJoinToken(String joinToken);
}
