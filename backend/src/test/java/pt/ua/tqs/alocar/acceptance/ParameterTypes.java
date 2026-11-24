package pt.ua.tqs.alocar.acceptance;

import io.cucumber.java.ParameterType;
import java.time.LocalTime;
import pt.ua.tqs.alocar.domain.ScooterStatus;

public class ParameterTypes {

    /** A time of day such as 08:35. */
    @ParameterType("\\d{2}:\\d{2}")
    public LocalTime hora(String value) {
        return LocalTime.parse(value);
    }

    /** An amount such as 12,00 €, in cents. */
    @ParameterType("\\d+,\\d{2} €")
    public Long euros(String value) {
        String[] parts = value.replace(" €", "").split(",");
        return Long.parseLong(parts[0]) * 100 + Long.parseLong(parts[1]);
    }

    @ParameterType("disponível|em uso|em manutenção")
    public ScooterStatus estado(String value) {
        return switch (value) {
            case "disponível" -> ScooterStatus.AVAILABLE;
            case "em uso" -> ScooterStatus.IN_USE;
            default -> ScooterStatus.MAINTENANCE;
        };
    }
}
