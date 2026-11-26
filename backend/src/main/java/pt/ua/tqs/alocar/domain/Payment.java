package pt.ua.tqs.alocar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** A charge to a resident for one use: the chosen duration (BR4) or the late fine (BR8). */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "use_id")
    private Use use;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentKind kind;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Payment() {
        // required by JPA
    }

    public Payment(Use use, PaymentKind kind, long amountCents, PaymentStatus status, Instant createdAt) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        this.resident = use.getResident();
        this.use = use;
        this.kind = kind;
        this.amountCents = amountCents;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Resident getResident() {
        return resident;
    }

    public Use getUse() {
        return use;
    }

    public PaymentKind getKind() {
        return kind;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
