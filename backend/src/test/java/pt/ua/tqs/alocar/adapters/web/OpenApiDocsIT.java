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

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class OpenApiDocsIT {

    @Autowired
    private MockMvcTester mvc;

    @Test
    void openApiDocumentDescribesTheHubsEndpoint() {
        assertThat(mvc.get().uri("/v3/api-docs"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .hasPathSatisfying("$.info.title", title -> assertThat(title).asString().isEqualTo("Alocar API"))
                .hasPathSatisfying("$.paths['/api/v1/hubs'].get", get -> assertThat(get).isNotNull());
    }

    @Test
    void swaggerUiIsServed() {
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatus(HttpStatus.OK);
    }
}
