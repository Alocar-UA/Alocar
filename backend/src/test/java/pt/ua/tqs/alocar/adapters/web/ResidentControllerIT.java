package pt.ua.tqs.alocar.adapters.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pt.ua.tqs.alocar.TestcontainersConfiguration;

/** US01 through HTTP, service, JPA and Flyway on a real PostgreSQL, using the seeded residence. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ResidentControllerIT {

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private JdbcTemplate jdbc;

    private long acacias;
    private String acaciasJoinToken;

    @BeforeEach
    void setUp() {
        acacias = jdbc.queryForObject(
                "SELECT id FROM subfleets WHERE name = 'Residencial Quinta das Acácias'", Long.class);
        acaciasJoinToken = jdbc.queryForObject("SELECT join_token FROM subfleets WHERE id = ?", String.class, acacias);
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM residents WHERE email LIKE '%@teste.exemplo.pt'");
        jdbc.update("UPDATE subfleets SET join_link_active = TRUE");
    }

    @Test
    void signUpThroughTheJoinLinkCreatesTheResidentInItsSubfleet() {
        MvcTestResult result = signUp(acaciasJoinToken, "Marta Lopes", "Marta@Teste.Exemplo.PT");

        assertThat(result).hasStatus(201);
        assertThat(result).headers().hasValue("Location", "/api/v1/residents/" + residentId("marta@teste.exemplo.pt"));
        assertThat(result).bodyJson().extractingPath("$.subfleetId").asNumber().isEqualTo((int) acacias);
        assertThat(result).bodyJson().extractingPath("$.subfleetName").isEqualTo("Residencial Quinta das Acácias");
        assertThat(result).bodyJson().extractingPath("$.email").isEqualTo("marta@teste.exemplo.pt");
    }

    @Test
    void unknownJoinLinkIsNotFound() {
        assertThat(signUp("link-que-nao-existe", "Marta Lopes", "marta@teste.exemplo.pt")).hasStatus(404);
    }

    @Test
    void disabledJoinLinkIsGone() {
        jdbc.update("UPDATE subfleets SET join_link_active = FALSE WHERE id = ?", acacias);

        assertThat(signUp(acaciasJoinToken, "Marta Lopes", "marta@teste.exemplo.pt")).hasStatus(410);
    }

    @Test
    void br2SecondAccountWithTheSameEmailIsRefused() {
        signUp(acaciasJoinToken, "Marta Lopes", "marta@teste.exemplo.pt");

        MvcTestResult result = signUp(acaciasJoinToken, "Marta Lopes", "MARTA@teste.exemplo.pt");

        assertThat(result).hasStatus(409);
        assertThat(result).bodyJson().extractingPath("$.rule").isEqualTo("BR2");
    }

    @Test
    void invalidEmailOrMissingNameIsABadRequest() {
        assertThat(signUp(acaciasJoinToken, "Marta Lopes", "nao-e-um-email")).hasStatus(400);
        assertThat(signUp(acaciasJoinToken, " ", "marta@teste.exemplo.pt")).hasStatus(400);
    }

    private MvcTestResult signUp(String joinToken, String name, String email) {
        return mvc.post()
                .uri("/api/v1/residents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"joinToken\": \"%s\", \"name\": \"%s\", \"email\": \"%s\"}".formatted(joinToken, name, email))
                .exchange();
    }

    private long residentId(String email) {
        return jdbc.queryForObject("SELECT id FROM residents WHERE email = ?", Long.class, email);
    }
}
