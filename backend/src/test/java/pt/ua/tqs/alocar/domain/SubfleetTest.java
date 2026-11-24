package pt.ua.tqs.alocar.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** US01: each subfleet (residence) has its own join link, which the franchisee can disable. */
class SubfleetTest {

    @Test
    void newSubfleetHasAnActiveJoinLink() {
        Subfleet subfleet = new Subfleet("Residência Ria Nova");

        assertThat(subfleet.getJoinToken()).isNotBlank();
        assertThat(subfleet.isJoinLinkActive()).isTrue();
    }

    @Test
    void eachSubfleetHasItsOwnJoinToken() {
        assertThat(new Subfleet("Residência Ria Nova").getJoinToken())
                .isNotEqualTo(new Subfleet("Residência Salinas").getJoinToken());
    }

    @Test
    void disabledJoinLinkIsNoLongerActive() {
        Subfleet subfleet = new Subfleet("Residência Ria Nova");

        subfleet.disableJoinLink();

        assertThat(subfleet.isJoinLinkActive()).isFalse();
    }
}
