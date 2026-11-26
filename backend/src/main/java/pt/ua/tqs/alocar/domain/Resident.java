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
import java.util.Locale;

/** A resident of a client residence; belongs to the subfleet whose join link they used (BR2). */
@Entity
@Table(name = "residents")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subfleet_id")
    private Subfleet subfleet;

    protected Resident() {
        // required by JPA
    }

    public Resident(String name, String email, Subfleet subfleet) {
        this.name = name.strip();
        this.email = email.strip().toLowerCase(Locale.ROOT);
        this.subfleet = subfleet;
    }

    public String getEmail() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Subfleet getSubfleet() {
        return subfleet;
    }
}
