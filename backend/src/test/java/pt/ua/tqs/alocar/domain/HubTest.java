package pt.ua.tqs.alocar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HubTest {

    @Test
    void hubWithZeroCapacityIsAllowed() {
        assertThat(new Hub("Hub Bloco C", "C", 0).getCapacity()).isZero();
    }

    @Test
    void hubWithNegativeCapacityIsRejected() {
        assertThatThrownBy(() -> new Hub("Hub Bloco C", "C", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
