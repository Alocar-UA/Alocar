package pt.ua.tqs.alocar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * The part of a city's fleet that serves one client residence or condominium. Residents join it
 * through its join link (US01, BR2).
 */
@Entity
@Table(name = "subfleets")
public class Subfleet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "join_token", nullable = false, unique = true)
    private String joinToken;

    @Column(name = "join_link_active", nullable = false)
    private boolean joinLinkActive;

    protected Subfleet() {
        // required by JPA
    }

    public Subfleet(String name) {
        this.name = name;
        this.joinToken = UUID.randomUUID().toString();
        this.joinLinkActive = true;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getJoinToken() {
        return joinToken;
    }

    public boolean isJoinLinkActive() {
        return joinLinkActive;
    }

    public void disableJoinLink() {
        this.joinLinkActive = false;
    }
}
