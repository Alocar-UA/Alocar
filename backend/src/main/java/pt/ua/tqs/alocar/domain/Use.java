package pt.ua.tqs.alocar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;

/** One use (utilização) of a scooter by a resident, from pick-up to return at the same hub. */
@Entity
@Table(name = "uses")
public class Use {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scooter_id")
    private Scooter scooter;

    /** Pick-up hub: where the scooter was when the use started; it must be returned there (BR7). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hub_id")
    private Hub hub;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "expected_end_at", nullable = false)
    private Instant expectedEndAt;

    @Column(name = "amount_paid_cents", nullable = false)
    private long amountPaidCents;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "fine_cents")
    private Long fineCents;

    protected Use() {
        // required by JPA
    }

    public Use(Resident resident, Scooter scooter, Instant startedAt, int durationMinutes, long amountPaidCents) {
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.resident = resident;
        this.scooter = scooter;
        this.hub = scooter.getHub();
        this.startedAt = startedAt;
        this.durationMinutes = durationMinutes;
        this.expectedEndAt = startedAt.plus(Duration.ofMinutes(durationMinutes));
        this.amountPaidCents = amountPaidCents;
    }

    public boolean isActive() {
        return endedAt == null;
    }

    /** Delay at the given instant; zero when the scooter is returned on time. */
    public Duration delayAt(Instant instant) {
        Duration delay = Duration.between(expectedEndAt, instant);
        return delay.isNegative() ? Duration.ZERO : delay;
    }

    public void end(Instant endedAt, long fineCents) {
        if (!isActive()) {
            throw new IllegalStateException("Use already ended");
        }
        this.endedAt = endedAt;
        this.fineCents = fineCents;
    }

    public Long getId() {
        return id;
    }

    public Resident getResident() {
        return resident;
    }

    public Scooter getScooter() {
        return scooter;
    }

    public Hub getHub() {
        return hub;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public Instant getExpectedEndAt() {
        return expectedEndAt;
    }

    public long getAmountPaidCents() {
        return amountPaidCents;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public Long getFineCents() {
        return fineCents;
    }
}
