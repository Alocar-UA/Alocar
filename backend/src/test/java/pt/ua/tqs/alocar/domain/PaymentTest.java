package pt.ua.tqs.alocar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** US06: every charge of a use (the chosen duration or the late fine) is a payment of the resident. */
class PaymentTest {

    private static final Instant NOW = Instant.parse("2026-11-23T12:49:00Z");

    private Use use;

    @BeforeEach
    void setUp() {
        Subfleet riaNova = new Subfleet("Residência Ria Nova");
        Hub hub = new Hub("Hub Ria Nova", "-", 10, riaNova, LocalTime.of(7, 0), LocalTime.of(23, 0));
        Resident tiago = new Resident("Tiago Mendes", "tiago.mendes@exemplo.pt", riaNova);
        use = new Use(tiago, new Scooter("RN-01", hub, ScooterStatus.IN_USE), NOW, 240, 1200);
    }

    @Test
    void paymentBelongsToTheResidentOfTheUse() {
        Payment fine = new Payment(use, PaymentKind.FINE, 280, PaymentStatus.PAID, NOW);

        assertThat(fine.getResident()).isSameAs(use.getResident());
        assertThat(fine.getUse()).isSameAs(use);
        assertThat(fine.getKind()).isEqualTo(PaymentKind.FINE);
        assertThat(fine.getAmountCents()).isEqualTo(280);
        assertThat(fine.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(fine.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void paymentOfZeroIsRejected() {
        assertThatThrownBy(() -> new Payment(use, PaymentKind.FINE, 0, PaymentStatus.PAID, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
