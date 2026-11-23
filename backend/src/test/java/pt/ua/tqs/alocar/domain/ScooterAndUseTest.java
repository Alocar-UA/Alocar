package pt.ua.tqs.alocar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ScooterAndUseTest {

    private final Hub hub = new Hub("Hub Bloco A", "A", 8);
    private final Instant start = Instant.parse("2026-11-23T08:35:00Z");

    @Test
    void availableScooterCanBePickedUpAndGivenBack() {
        Scooter scooter = new Scooter("TR-001", hub, ScooterStatus.AVAILABLE);
        scooter.pickUp();
        assertThat(scooter.getStatus()).isEqualTo(ScooterStatus.IN_USE);
        scooter.giveBack();
        assertThat(scooter.isAvailable()).isTrue();
    }

    @Test
    void scooterInMaintenanceCannotBePickedUp() {
        Scooter scooter = new Scooter("TR-003", hub, ScooterStatus.MAINTENANCE);
        assertThatThrownBy(scooter::pickUp).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void scooterNotInUseCannotBeGivenBack() {
        Scooter scooter = new Scooter("TR-001", hub, ScooterStatus.AVAILABLE);
        assertThatThrownBy(scooter::giveBack).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void useEndsAfterTheChosenDurationAtThePickUpHub() {
        Use use = new Use(null, new Scooter("TR-001", hub, ScooterStatus.IN_USE), start, 240, 1200);
        assertThat(use.getExpectedEndAt()).isEqualTo(Instant.parse("2026-11-23T12:35:00Z"));
        assertThat(use.getHub()).isSameAs(hub);
        assertThat(use.isActive()).isTrue();
    }

    @Test
    void delayIsZeroBeforeTheExpectedEnd() {
        Use use = new Use(null, new Scooter("TR-001", hub, ScooterStatus.IN_USE), start, 60, 300);
        assertThat(use.delayAt(start.plus(Duration.ofMinutes(30)))).isZero();
        assertThat(use.delayAt(start.plus(Duration.ofMinutes(74)))).isEqualTo(Duration.ofMinutes(14));
    }

    @Test
    void useCannotEndTwice() {
        Use use = new Use(null, new Scooter("TR-001", hub, ScooterStatus.IN_USE), start, 60, 300);
        use.end(start.plus(Duration.ofMinutes(60)), 0);
        assertThat(use.isActive()).isFalse();
        assertThatThrownBy(() -> use.end(start, 0)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void durationMustBePositive() {
        Scooter scooter = new Scooter("TR-001", hub, ScooterStatus.IN_USE);
        assertThatThrownBy(() -> new Use(null, scooter, start, 0, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
