package pt.ua.tqs.alocar.domain;

/** Outcome of the charge at the payment gateway; a declined fine stays owed. */
public enum PaymentStatus {
    PAID,
    DECLINED
}
