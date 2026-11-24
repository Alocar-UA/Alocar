package pt.ua.tqs.alocar.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import pt.ua.tqs.alocar.domain.Tariff;

/**
 * Franchise tariff until franchises are stored in the database. Values in application.properties
 * are «proposta» and must be confirmed by the group.
 */
@ConfigurationProperties(prefix = "alocar.pricing")
public record PricingProperties(
        long ratePerMinuteCents,
        long finePerLateMinuteCents,
        Duration fineThreshold,
        int minDurationMinutes,
        int maxDurationMinutes) {

    public Tariff toTariff() {
        return new Tariff(
                ratePerMinuteCents, finePerLateMinuteCents, fineThreshold, minDurationMinutes, maxDurationMinutes);
    }
}
