package pt.ua.tqs.alocar.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScooterRepository extends JpaRepository<Scooter, Long> {

    List<Scooter> findByHubIdOrderByCodeAsc(Long hubId);

    long countByHubIdAndStatus(Long hubId, ScooterStatus status);
}
