package pt.ua.tqs.alocar.adapters.web;

import pt.ua.tqs.alocar.domain.Resident;

public record ResidentResponse(Long id, String name, String email, Long subfleetId, String subfleetName) {

    static ResidentResponse from(Resident resident) {
        return new ResidentResponse(
                resident.getId(),
                resident.getName(),
                resident.getEmail(),
                resident.getSubfleet().getId(),
                resident.getSubfleet().getName());
    }
}
