package pt.ua.tqs.alocar.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pt.ua.tqs.alocar.MutableClock;
import pt.ua.tqs.alocar.TestClockConfiguration;
import pt.ua.tqs.alocar.TestcontainersConfiguration;

/**
 * US03/US04/US06 through HTTP, service, JPA and Flyway on a real PostgreSQL, using the seeded
 * residence (Quinta das Acácias). Not transactional, so lazy loading and constraints behave as in
 * production; each test cleans up the uses it created.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestClockConfiguration.class})
class UseControllerIT {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MutableClock clock;

    private long tiago;
    private long rita;
    private long tr001;
    private long tr003InMaintenance;
    private long hubA;
    private long hubB;

    @BeforeEach
    void setUp() {
        clock.setLocal(2026, 11, 23, 8, 35);
        tiago = id("SELECT id FROM residents WHERE name = 'Tiago Mendes'");
        rita = id("SELECT id FROM residents WHERE name = 'Rita Faria'");
        tr001 = id("SELECT id FROM scooters WHERE code = 'TR-001'");
        tr003InMaintenance = id("SELECT id FROM scooters WHERE code = 'TR-003'");
        hubA = id("SELECT id FROM hubs WHERE name = 'Hub Bloco A'");
        hubB = id("SELECT id FROM hubs WHERE name = 'Hub Bloco B'");
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM uses");
        jdbc.update("UPDATE scooters SET status = 'AVAILABLE' WHERE status = 'IN_USE'");
    }

    @Test
    void residentPicksUpReturnsLateAndSeesTheFineInTheHistory() {
        MvcTestResult started = startUse(tiago, tr001, 240);
        assertThat(started)
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .hasPathSatisfying("$.amountPaidCents", v -> assertThat(v).asNumber().isEqualTo(1200))
                .hasPathSatisfying("$.expectedEndAt", v -> assertThat(v).asString().isEqualTo("2026-11-23T12:35:00Z"))
                .hasPathSatisfying("$.active", v -> assertThat(v).asBoolean().isTrue());
        assertThat(scooterStatus("TR-001")).isEqualTo("IN_USE");
        long useId = id("SELECT id FROM uses WHERE resident_id = " + tiago);

        clock.setLocal(2026, 11, 23, 12, 49);
        assertThat(mvc.post()
                        .uri("/api/v1/uses/{id}/return", useId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hubId\": " + hubA + "}"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.lateMinutes", v -> assertThat(v).asNumber().isEqualTo(14))
                .hasPathSatisfying("$.fineCents", v -> assertThat(v).asNumber().isEqualTo(280))
                .hasPathSatisfying("$.active", v -> assertThat(v).asBoolean().isFalse());
        assertThat(scooterStatus("TR-001")).isEqualTo("AVAILABLE");

        assertThat(mvc.get().uri("/api/v1/residents/{id}/uses", tiago))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$[0].scooterCode", v -> assertThat(v).asString().isEqualTo("TR-001"));
        assertThat(mvc.get().uri("/api/v1/uses/{id}", useId))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.fineCents", v -> assertThat(v).asNumber().isEqualTo(280));
    }

    @Test
    void br3ScooterInMaintenanceIsRefusedWithTheRuleId() {
        assertThat(startUse(tiago, tr003InMaintenance, 60))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.rule", v -> assertThat(v).asString().isEqualTo("BR3"));
    }

    @Test
    void br3ScooterAlreadyInUseIsRefused() {
        assertThat(startUse(tiago, tr001, 60)).hasStatus(HttpStatus.CREATED);

        assertThat(startUse(rita, tr001, 60))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.rule", v -> assertThat(v).asString().isEqualTo("BR3"));
    }

    @Test
    void br6SecondActiveUseIsRefused() {
        long tr002 = id("SELECT id FROM scooters WHERE code = 'TR-002'");
        assertThat(startUse(tiago, tr001, 60)).hasStatus(HttpStatus.CREATED);

        assertThat(startUse(tiago, tr002, 60))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.rule", v -> assertThat(v).asString().isEqualTo("BR6"));
    }

    @Test
    void br7ReturnAtAnotherHubIsRefused() {
        assertThat(startUse(tiago, tr001, 60)).hasStatus(HttpStatus.CREATED);
        long useId = id("SELECT id FROM uses WHERE resident_id = " + tiago);

        assertThat(mvc.post()
                        .uri("/api/v1/uses/{id}/return", useId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hubId\": " + hubB + "}"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson()
                .hasPathSatisfying("$.rule", v -> assertThat(v).asString().isEqualTo("BR7"));
    }

    @Test
    void invalidRequestIsBadRequest() {
        assertThat(mvc.post()
                        .uri("/api/v1/uses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"residentId\": " + tiago + "}"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unknownUseIsNotFound() {
        assertThat(mvc.get().uri("/api/v1/uses/{id}", 999_999)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void hubListsItsScootersWithStatus() {
        assertThat(mvc.get().uri("/api/v1/hubs/{id}/scooters", hubA))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$[*].code")
                .asArray()
                .containsExactly("TR-001", "TR-002", "TR-003");
        assertThat(mvc.get().uri("/api/v1/hubs/{id}/scooters", 999_999)).hasStatus(HttpStatus.NOT_FOUND);
    }

    private MvcTestResult startUse(long residentId, long scooterId, int durationMinutes) {
        return mvc.post()
                .uri("/api/v1/uses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"residentId\": %d, \"scooterId\": %d, \"durationMinutes\": %d}"
                        .formatted(residentId, scooterId, durationMinutes))
                .exchange();
    }

    private String scooterStatus(String code) {
        return jdbc.queryForObject("SELECT status FROM scooters WHERE code = ?", String.class, code);
    }

    private long id(String sql) {
        Long value = jdbc.queryForObject(sql, Long.class);
        assertThat(value).isNotNull();
        return value;
    }
}
