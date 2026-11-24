package pt.ua.tqs.alocar.adapters.web;

import java.time.Instant;
import pt.ua.tqs.alocar.domain.Use;

public record UseResponse(
        Long id,
        Long residentId,
        Long scooterId,
        String scooterCode,
        Long hubId,
        Instant startedAt,
        int durationMinutes,
        Instant expectedEndAt,
        long amountPaidCents,
        Instant endedAt,
        Long lateMinutes,
        Long fineCents,
        boolean active) {

    static UseResponse from(Use use) {
        Long lateMinutes = use.isActive() ? null : use.delayAt(use.getEndedAt()).toMinutes();
        return new UseResponse(
                use.getId(),
                use.getResident().getId(),
                use.getScooter().getId(),
                use.getScooter().getCode(),
                use.getHub().getId(),
                use.getStartedAt(),
                use.getDurationMinutes(),
                use.getExpectedEndAt(),
                use.getAmountPaidCents(),
                use.getEndedAt(),
                lateMinutes,
                use.getFineCents(),
                use.isActive());
    }
}
