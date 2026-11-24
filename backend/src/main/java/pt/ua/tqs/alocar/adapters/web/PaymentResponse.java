package pt.ua.tqs.alocar.adapters.web;

import java.time.Instant;
import pt.ua.tqs.alocar.domain.Payment;
import pt.ua.tqs.alocar.domain.PaymentKind;
import pt.ua.tqs.alocar.domain.PaymentStatus;

public record PaymentResponse(
        Long id,
        Long useId,
        String scooterCode,
        PaymentKind kind,
        long amountCents,
        PaymentStatus status,
        Instant createdAt) {

    static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getUse().getId(),
                payment.getUse().getScooter().getCode(),
                payment.getKind(),
                payment.getAmountCents(),
                payment.getStatus(),
                payment.getCreatedAt());
    }
}
