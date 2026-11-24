package pt.ua.tqs.alocar.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.ua.tqs.alocar.domain.Tariff;

@Configuration
@EnableConfigurationProperties(PricingProperties.class)
public class AppConfiguration {

    /** Hub opening hours are local times in the franchise's time zone. */
    @Bean
    Clock clock(@Value("${alocar.time-zone}") ZoneId zone) {
        return Clock.system(zone);
    }

    @Bean
    Tariff tariff(PricingProperties pricing) {
        return pricing.toTariff();
    }
}
