package pt.ua.tqs.alocar.domain;

import java.time.Instant;

public class Payment {

    public Payment(Use use, PaymentKind kind, long amountCents, PaymentStatus status, Instant createdAt) {}

    public Resident getResident() {
        return null;
    }

    public Use getUse() {
        return null;
    }

    public PaymentKind getKind() {
        return null;
    }

    public long getAmountCents() {
        return 0;
    }

    public PaymentStatus getStatus() {
        return null;
    }

    public Instant getCreatedAt() {
        return null;
    }
}
