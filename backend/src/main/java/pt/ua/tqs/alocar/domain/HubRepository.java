package pt.ua.tqs.alocar.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HubRepository extends JpaRepository<Hub, Long> {

    List<Hub> findAllByOrderByNameAsc();
}
