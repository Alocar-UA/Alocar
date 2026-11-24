package pt.ua.tqs.alocar;

import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class TestClockConfiguration {

    /** Replaces the system clock; starts on Monday 2026-11-23 at 10:00 in Lisbon. */
    @Bean
    @Primary
    MutableClock testClock() {
        return new MutableClock(Instant.parse("2026-11-23T10:00:00Z"), ZoneId.of("Europe/Lisbon"));
    }
}
