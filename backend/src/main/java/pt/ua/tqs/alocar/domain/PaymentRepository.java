package pt.ua.tqs.alocar.domain;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** US06: the resident's payments, most recent first; loads the use's scooter for its code. */
    @EntityGraph(attributePaths = {"use", "use.scooter"})
    List<Payment> findByResidentIdOrderByCreatedAtDescIdDesc(Long residentId);
}
