package pt.ua.tqs.alocar.adapters.web;

import pt.ua.tqs.alocar.domain.Scooter;
import pt.ua.tqs.alocar.domain.ScooterStatus;

public record ScooterResponse(Long id, String code, Long hubId, ScooterStatus status) {

    static ScooterResponse from(Scooter scooter) {
        return new ScooterResponse(scooter.getId(), scooter.getCode(), scooter.getHub().getId(), scooter.getStatus());
    }
}
