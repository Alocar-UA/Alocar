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
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A pick-up/return point for scooters of one subfleet, with optional opening hours. A hub without
 * opening hours is always open.
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subfleet_id")
    private Subfleet subfleet;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    protected Hub() {
        // required by JPA
    }

    public Hub(String name, String block, int capacity) {
        this(name, block, capacity, null, null, null);
    }

    public Hub(
            String name,
            String block,
            int capacity,
            Subfleet subfleet,
            LocalTime openingTime,
            LocalTime closingTime) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Hub capacity cannot be negative");
        }
        if ((openingTime == null) != (closingTime == null)) {
            throw new IllegalArgumentException("Opening and closing time must both be set or both be empty");
        }
        if (openingTime != null && !openingTime.isBefore(closingTime)) {
            throw new IllegalArgumentException("Opening time must be before closing time");
        }
        this.name = name;
        this.block = block;
        this.capacity = capacity;
        this.subfleet = subfleet;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public boolean isAlwaysOpen() {
        return openingTime == null;
    }

    /** BR3: the hub is open from the opening time (inclusive) to the closing time (exclusive). */
    public boolean isOpenAt(LocalTime time) {
        return isAlwaysOpen() || (!time.isBefore(openingTime) && time.isBefore(closingTime));
    }

    /** BR5: a use that starts and ends at these local times ends within the same opening period. */
    public boolean coversPeriod(LocalDateTime start, LocalDateTime end) {
        if (isAlwaysOpen()) {
            return true;
        }
        return isOpenAt(start.toLocalTime())
                && end.toLocalDate().equals(start.toLocalDate())
                && !end.toLocalTime().isAfter(closingTime);
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

    public Subfleet getSubfleet() {
        return subfleet;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }
}
