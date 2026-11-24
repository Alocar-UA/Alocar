package pt.ua.tqs.alocar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** The part of a city's fleet that serves one client residence or condominium. */
@Entity
@Table(name = "subfleets")
public class Subfleet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected Subfleet() {
        // required by JPA
    }

    public Subfleet(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getJoinToken() {
        return null;
    }

    public boolean isJoinLinkActive() {
        return false;
    }

    public void disableJoinLink() {
        // not implemented yet (US01)
    }
}
