package pt.ua.tqs.alocar.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.JsonPath;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pt.ua.tqs.alocar.domain.Subfleet;
import pt.ua.tqs.alocar.domain.SubfleetRepository;

/**
 * Steps for US01 (create an account through the residence's join link). Residence names and emails
 * get a unique suffix per scenario, so scenarios do not depend on each other. Requests go through
 * the REST API.
 */
public class AccountSteps {

    private static final AtomicInteger SCENARIO = new AtomicInteger();

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private SubfleetRepository subfleets;

    private final int scenario = SCENARIO.incrementAndGet();
    private final Map<String, Subfleet> subfleetByName = new HashMap<>();
    private MvcTestResult lastResult;

    @Dado("a residência {string} com o link de adesão ativo")
    public void residenceWithActiveJoinLink(String residence) {
        subfleetByName.put(residence, subfleets.save(new Subfleet(residence + " (conta #" + scenario + ")")));
    }

    @Dado("que o link de adesão da {string} foi desativado")
    public void joinLinkDisabled(String residence) {
        Subfleet subfleet = subfleetByName.get(residence);
        subfleet.disableJoinLink();
        subfleetByName.put(residence, subfleets.save(subfleet));
    }

    @Quando("{string} cria conta com o email {string} pelo link da {string}")
    public void signUpThroughResidenceLink(String name, String email, String residence) {
        signUpThroughLink(name, email, subfleetByName.get(residence).getJoinToken());
    }

    @Dado("que {string} criou conta com o email {string} pelo link da {string}")
    public void signedUpThroughResidenceLink(String name, String email, String residence) {
        signUpThroughResidenceLink(name, email, residence);
        assertThat(lastResult).hasStatus(201);
    }

    @Quando("{string} cria conta com o email {string} pelo link {string}")
    public void signUpThroughLink(String name, String email, String joinToken) {
        lastResult = mvc.post()
                .uri("/api/v1/residents")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"joinToken\": \"%s\", \"name\": \"%s\", \"email\": \"%s\"}"
                        .formatted(joinToken, name, unique(email)))
                .exchange();
    }

    @Então("a conta de {string} é criada na subfrota da {string}")
    public void accountCreatedInSubfleet(String name, String residence) {
        assertThat(lastResult).hasStatus(201);
        assertThat(lastResult).headers().containsHeader("Location");
        assertThat(this.<String>json("$.name")).isEqualTo(name);
        assertThat(this.<Number>json("$.subfleetId").longValue())
                .isEqualTo(subfleetByName.get(residence).getId());
    }

    @Então("a conta não é criada porque o link está desativado")
    public void refusedBecauseLinkDisabled() {
        assertThat(lastResult).hasStatus(410);
    }

    @Então("a conta não é criada porque o link não existe")
    public void refusedBecauseLinkUnknown() {
        assertThat(lastResult).hasStatus(404);
    }

    @Então("a conta não é criada porque o email já está registado")
    public void refusedBecauseEmailTaken() {
        assertThat(lastResult).hasStatus(409);
        assertThat(this.<String>json("$.rule")).isEqualTo("BR2");
    }

    /** marta@exemplo.pt becomes marta+7@exemplo.pt in scenario 7 (keeps the case of the input). */
    private String unique(String email) {
        return email.replace("@", "+" + scenario + "@");
    }

    private <T> T json(String path) {
        try {
            return JsonPath.read(lastResult.getResponse().getContentAsString(), path);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
