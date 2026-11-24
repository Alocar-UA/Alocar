package pt.ua.tqs.alocar.application.port;

/** Port to the external payment service (sandbox). Amounts are in euro cents. */
public interface PaymentGateway {

    /** Charges the resident; returns {@code false} when the payment is declined. */
    boolean charge(Long residentId, long amountCents, String description);
}
