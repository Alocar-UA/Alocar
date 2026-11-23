package pt.ua.tqs.alocar.adapters.web;

import jakarta.validation.constraints.NotNull;

/** The hub where the resident left the scooter (until telemetry confirms the location, BR7). */
public record ReturnScooterRequest(@NotNull Long hubId) {}
