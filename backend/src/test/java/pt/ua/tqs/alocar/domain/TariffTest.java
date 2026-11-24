package pt.ua.tqs.alocar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.Test;

/** BR4 (price of the chosen duration) and BR8 (late fine from 10 minutes, growing with the delay). */
class TariffTest {

    private final Tariff tariff = new Tariff(5, 20, Duration.ofMinutes(10), 10, 480);

    @Test
    void br4PriceIsDurationTimesRatePerMinute() {
        assertThat(tariff.priceFor(240)).isEqualTo(1200);
    }

    @Test
    void br4DurationLimitsAreInclusive() {
        assertThat(tariff.allowsDuration(9)).isFalse();
        assertThat(tariff.allowsDuration(10)).isTrue();
        assertThat(tariff.allowsDuration(480)).isTrue();
        assertThat(tariff.allowsDuration(481)).isFalse();
        assertThatThrownBy(() -> tariff.priceFor(9)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void br8DelayOf9Min59sHasNoFine() {
        assertThat(tariff.fineFor(Duration.ofMinutes(9).plusSeconds(59))).isZero();
    }

    @Test
    void br8DelayOf10MinHasFine() {
        assertThat(tariff.fineFor(Duration.ofMinutes(10))).isEqualTo(200);
    }

    @Test
    void br8FineGrowsWithTheDelay() {
        assertThat(tariff.fineFor(Duration.ofMinutes(14))).isEqualTo(280);
        assertThat(tariff.fineFor(Duration.ofMinutes(60))).isGreaterThan(tariff.fineFor(Duration.ofMinutes(14)));
    }

    @Test
    void noDelayHasNoFine() {
        assertThat(tariff.fineFor(Duration.ZERO)).isZero();
    }

    @Test
    void invalidTariffsAreRejected() {
        assertThatThrownBy(() -> new Tariff(-1, 20, Duration.ofMinutes(10), 10, 480))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tariff(5, 20, Duration.ofMinutes(-1), 10, 480))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tariff(5, 20, Duration.ofMinutes(10), 0, 480))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Tariff(5, 20, Duration.ofMinutes(10), 60, 30))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
