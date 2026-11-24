package pt.ua.tqs.alocar.adapters.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StartUseRequest(
        @NotNull Long residentId, @NotNull Long scooterId, @NotNull @Positive Integer durationMinutes) {}
