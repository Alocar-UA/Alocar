package pt.ua.tqs.alocar.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A pick-up/return point for vehicles, usually one per building block of the condominium. */
@Entity
@Table(name = "hubs")
public class Hub {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String block;

    @Column(nullable = false)
    private int capacity;

    protected Hub() {
        // required by JPA
    }

    public Hub(String name, String block, int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Hub capacity cannot be negative");
        }
        this.name = name;
        this.block = block;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBlock() {
        return block;
    }

    public int getCapacity() {
        return capacity;
    }
}
