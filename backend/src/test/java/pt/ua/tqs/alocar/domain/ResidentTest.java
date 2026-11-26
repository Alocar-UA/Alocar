package pt.ua.tqs.alocar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResidentTest {

    private final Subfleet riaNova = new Subfleet("Residência Ria Nova");

    @Test
    void residentBelongsToTheSubfleetOfTheJoinLink() {
        Resident marta = new Resident("Marta Lopes", "marta@exemplo.pt", riaNova);

        assertThat(marta.getSubfleet()).isSameAs(riaNova);
    }

    /** BR2: one account per email, so the same address in another case is the same account. */
    @Test
    void emailIsKeptInLowerCaseWithoutSurroundingSpaces() {
        Resident marta = new Resident("Marta Lopes", "  Marta.Lopes@Exemplo.PT ", riaNova);

        assertThat(marta.getEmail()).isEqualTo("marta.lopes@exemplo.pt");
    }

    @Test
    void nameIsKeptWithoutSurroundingSpaces() {
        assertThat(new Resident("  Marta Lopes  ", "marta@exemplo.pt", riaNova).getName()).isEqualTo("Marta Lopes");
    }
}
