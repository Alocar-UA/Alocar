package pt.ua.tqs.alocar;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** A clock that tests can set, so time-dependent rules (hub hours, late fines) are deterministic. */
public class MutableClock extends Clock {

    private final ZoneId zone;
    private Instant instant;

    public MutableClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public void setLocal(int year, int month, int day, int hour, int minute) {
        this.instant = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, zone).toInstant();
    }

    public void advanceMinutes(long minutes) {
        this.instant = instant.plusSeconds(minutes * 60);
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableClock(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
