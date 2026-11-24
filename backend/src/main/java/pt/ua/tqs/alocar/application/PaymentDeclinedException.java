package pt.ua.tqs.alocar.application;

public class PaymentDeclinedException extends RuntimeException {

    public PaymentDeclinedException(String message) {
        super(message);
    }
}
