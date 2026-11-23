package pt.ua.tqs.alocar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

/** BR3 (hub open to start) and BR5 (use ends within the hub's hours), with the spec's boundary values. */
class HubOpeningHoursTest {

    private final Hub hub = new Hub("Hub Bloco A", "A", 8, null, LocalTime.of(7, 0), LocalTime.of(23, 0));

    @Test
    void br3StartAt659IsRefusedAndAt700IsAccepted() {
        assertThat(hub.isOpenAt(LocalTime.of(6, 59))).isFalse();
        assertThat(hub.isOpenAt(LocalTime.of(7, 0))).isTrue();
    }

    @Test
    void hubIsClosedFromClosingTime() {
        assertThat(hub.isOpenAt(LocalTime.of(22, 59))).isTrue();
        assertThat(hub.isOpenAt(LocalTime.of(23, 0))).isFalse();
    }

    @Test
    void br5StartAt2200With60MinutesIsAcceptedAnd61IsRefused() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 23, 22, 0);
        assertThat(hub.coversPeriod(start, start.plusMinutes(60))).isTrue();
        assertThat(hub.coversPeriod(start, start.plusMinutes(61))).isFalse();
    }

    @Test
    void br5UseCannotCrossMidnightAtAHubWithHours() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 23, 8, 0);
        assertThat(hub.coversPeriod(start, start.plusHours(24))).isFalse();
    }

    @Test
    void hubWithoutHoursIsAlwaysOpen() {
        Hub garage = new Hub("Hub Garagem", "Garagem", 12);
        LocalDateTime start = LocalDateTime.of(2026, 11, 23, 23, 30);
        assertThat(garage.isAlwaysOpen()).isTrue();
        assertThat(garage.isOpenAt(LocalTime.of(3, 0))).isTrue();
        assertThat(garage.coversPeriod(start, start.plusHours(2))).isTrue();
    }

    @Test
    void hoursMustHaveBothEndsAndOpenBeforeClosing() {
        assertThatThrownBy(() -> new Hub("H", "H", 1, null, LocalTime.of(7, 0), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Hub("H", "H", 1, null, LocalTime.of(23, 0), LocalTime.of(7, 0)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
