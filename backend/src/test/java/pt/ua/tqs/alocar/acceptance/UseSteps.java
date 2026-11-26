package pt.ua.tqs.alocar.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import pt.ua.tqs.alocar.MutableClock;
import pt.ua.tqs.alocar.domain.Hub;
import pt.ua.tqs.alocar.domain.HubRepository;
import pt.ua.tqs.alocar.domain.Resident;
import pt.ua.tqs.alocar.domain.ResidentRepository;
import pt.ua.tqs.alocar.domain.Scooter;
import pt.ua.tqs.alocar.domain.ScooterRepository;
import pt.ua.tqs.alocar.domain.ScooterStatus;
import pt.ua.tqs.alocar.domain.Subfleet;
import pt.ua.tqs.alocar.domain.SubfleetRepository;

/**
 * Steps for US03 (pick up) and US04 (return). Each scenario creates its own residence, hub, scooters
 * and residents (names get a unique suffix in the database), so scenarios do not depend on each other.
 * Requests go through the REST API.
 */
public class UseSteps {

    private static final AtomicInteger SCENARIO = new AtomicInteger();

    @Autowired
    private MockMvcTester mvc;

    @Autowired
    private MutableClock clock;

    @Autowired
    private SubfleetRepository subfleets;

    @Autowired
    private HubRepository hubs;

    @Autowired
    private ScooterRepository scooters;

    @Autowired
    private ResidentRepository residents;

    private final String suffix = " #" + SCENARIO.incrementAndGet();
    private final Map<String, Subfleet> subfleetByName = new HashMap<>();
    private final Map<String, Hub> hubByName = new HashMap<>();
    private final Map<String, Scooter> scooterByCode = new HashMap<>();
    private final Map<String, Resident> residentByName = new HashMap<>();
    private final Map<String, Long> activeUseByResident = new HashMap<>();
    private MvcTestResult lastResult;

    @Dado("a residência {string} com o hub {string} aberto das {hora} às {hora}")
    public void residenceWithHub(String residence, String hub, LocalTime opening, LocalTime closing) {
        Subfleet subfleet = subfleetByName.computeIfAbsent(residence, n -> subfleets.save(new Subfleet(n + suffix)));
        hubByName.put(hub, hubs.save(new Hub(hub + suffix, "-", 10, subfleet, opening, closing)));
    }

    @Dado("a trotinete {string} disponível no hub {string}")
    public void availableScooter(String code, String hub) {
        scooterByCode.put(
                code, scooters.save(new Scooter(code + suffix, hubByName.get(hub), ScooterStatus.AVAILABLE)));
    }

    @Dado("o residente {string} da residência {string}")
    public void resident(String name, String residence) {
        residentByName.put(name, residents.save(new Resident(name, emailOf(name), subfleetByName.get(residence))));
    }

    @Dado("que são {hora} de segunda-feira")
    public void timeIs(LocalTime time) {
        clock.setLocal(2026, 11, 23, time.getHour(), time.getMinute());
    }

    @Dado("o residente {string} tem a trotinete {string} desde as {hora} por {int} minutos")
    public void residentHasScooter(String resident, String code, LocalTime time, int minutes) {
        timeIs(time);
        residentPicksUp(resident, code, minutes);
        assertThat(lastResult).hasStatus2xxSuccessful();
    }

    @Quando("o residente {string} levanta a trotinete {string} por {int} minutos")
    public void residentPicksUp(String resident, String code, int minutes) {
        lastResult = mvc.post()
                .uri("/api/v1/uses")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"residentId\": %d, \"scooterId\": %d, \"durationMinutes\": %d}"
                        .formatted(residentByName.get(resident).getId(), scooterByCode.get(code).getId(), minutes))
                .exchange();
        if (lastResult.getResponse().getStatus() == 201) {
            activeUseByResident.put(resident, jsonNumber("$.id").longValue());
        }
    }

    @Quando("o residente {string} devolve a trotinete no hub {string} às {hora}")
    public void residentReturns(String resident, String hub, LocalTime time) {
        timeIs(time);
        lastResult = mvc.post()
                .uri("/api/v1/uses/{id}/return", activeUseByResident.get(resident))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hubId\": %d}".formatted(hubByName.get(hub).getId()))
                .exchange();
    }

    @Então("a utilização começa com fim previsto às {hora}")
    public void useStartsWithExpectedEnd(LocalTime expectedEnd) {
        assertThat(lastResult).hasStatus(201);
        Instant expected = ZonedDateTime.of(2026, 11, 23, expectedEnd.getHour(), expectedEnd.getMinute(), 0, 0,
                        ZoneId.of("Europe/Lisbon"))
                .toInstant();
        assertThat(this.<String>json("$.expectedEndAt")).isEqualTo(expected.toString());
    }

    @E("o residente pagou {euros}")
    public void residentPaid(long cents) {
        assertThat(jsonNumber("$.amountPaidCents").longValue()).isEqualTo(cents);
    }

    @E("a trotinete {string} fica {estado}")
    public void scooterStatus(String code, ScooterStatus status) {
        assertThat(scooters.findById(scooterByCode.get(code).getId()))
                .get()
                .extracting(Scooter::getStatus)
                .isEqualTo(status);
    }

    @Então("o pedido é aceite")
    public void requestAccepted() {
        assertThat(lastResult).hasStatus2xxSuccessful();
    }

    @Então("o pedido é recusado pela {word}")
    public void requestRefusedBy(String rule) {
        assertThat(lastResult).hasStatus(409);
        assertThat(this.<String>json("$.rule")).isEqualTo(rule);
    }

    @Então("a utilização termina com {int} minutos de atraso e coima de {euros}")
    public void useEndsWithFine(int lateMinutes, long fineCents) {
        assertThat(lastResult).hasStatus(200);
        assertThat(jsonNumber("$.lateMinutes").intValue()).isEqualTo(lateMinutes);
        assertThat(jsonNumber("$.fineCents").longValue()).isEqualTo(fineCents);
    }

    /** "Tiago" in scenario 7 gets tiago.7@exemplo.pt, so accounts never clash between scenarios. */
    private String emailOf(String name) {
        return name.toLowerCase().replace(' ', '.') + "." + suffix.replace(" #", "") + "@exemplo.pt";
    }

    private Number jsonNumber(String path) {
        return json(path);
    }

    private <T> T json(String path) {
        try {
            return JsonPath.read(lastResult.getResponse().getContentAsString(), path);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
