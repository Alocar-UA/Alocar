package pt.ua.tqs.alocar.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import pt.ua.tqs.alocar.TestcontainersConfiguration;

/** Full stack test: HTTP layer + service + JPA + Flyway migrations on a real PostgreSQL container. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class HubControllerIT {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void listHubsReturnsSeededHubsOrderedByName() {
        assertThat(mvc.get().uri("/api/v1/hubs"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$[*].name")
                .asArray()
                .containsExactly("Hub Bloco A", "Hub Bloco B", "Hub Garagem Central");
    }

    @Test
    void listHubsShowsOpeningHoursAndFreeScooters() {
        assertThat(mvc.get().uri("/api/v1/hubs"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$[0].openingTime", v -> assertThat(v).asString().isEqualTo("07:00:00"))
                .hasPathSatisfying("$[0].closingTime", v -> assertThat(v).asString().isEqualTo("23:00:00"))
                // Hub Bloco A has TR-001 and TR-002 free; TR-003 is in maintenance.
                .hasPathSatisfying("$[0].availableScooters", v -> assertThat(v).asNumber().isEqualTo(2));
    }
}
