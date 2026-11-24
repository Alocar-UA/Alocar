package pt.ua.tqs.alocar.adapters.web;

import java.time.LocalTime;
import pt.ua.tqs.alocar.domain.Hub;

/** A hub with its opening hours (null when always open) and how many scooters are free (US02). */
public record HubResponse(
        Long id,
        String name,
        String block,
        int capacity,
        LocalTime openingTime,
        LocalTime closingTime,
        long availableScooters) {

    static HubResponse from(Hub hub, long availableScooters) {
        return new HubResponse(
                hub.getId(),
                hub.getName(),
                hub.getBlock(),
                hub.getCapacity(),
                hub.getOpeningTime(),
                hub.getClosingTime(),
                availableScooters);
    }
}
