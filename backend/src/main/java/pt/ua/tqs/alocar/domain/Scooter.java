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
import jakarta.persistence.Version;

@Entity
@Table(name = "scooters")
public class Scooter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hub_id")
    private Hub hub;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScooterStatus status;

    /** Optimistic lock, so two residents cannot pick up the same scooter at once. */
    @Version
    private long version;

    protected Scooter() {
        // required by JPA
    }

    public Scooter(String code, Hub hub, ScooterStatus status) {
        this.code = code;
        this.hub = hub;
        this.status = status;
    }

    public boolean isAvailable() {
        return status == ScooterStatus.AVAILABLE;
    }

    public void pickUp() {
        if (!isAvailable()) {
            throw new IllegalStateException("Scooter " + code + " is not available");
        }
        status = ScooterStatus.IN_USE;
    }

    public void giveBack() {
        if (status != ScooterStatus.IN_USE) {
            throw new IllegalStateException("Scooter " + code + " is not in use");
        }
        status = ScooterStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public Hub getHub() {
        return hub;
    }

    public ScooterStatus getStatus() {
        return status;
    }
}
