package pt.ua.tqs.alocar.domain;

import java.time.Duration;

/**
 * Price and fine rules of a franchise (BR4, BR8). Amounts are in euro cents.
 *
 * @param ratePerMinuteCents price of one minute of use
 * @param finePerLateMinuteCents fine for each whole minute of delay, once the fine applies
 * @param fineThreshold delay from which the fine applies (10 minutes in the spec)
 * @param minDurationMinutes shortest duration a resident can choose
 * @param maxDurationMinutes longest duration a resident can choose
 */
public record Tariff(
        long ratePerMinuteCents,
        long finePerLateMinuteCents,
        Duration fineThreshold,
        int minDurationMinutes,
        int maxDurationMinutes) {

    public Tariff {
        if (ratePerMinuteCents < 0 || finePerLateMinuteCents < 0) {
            throw new IllegalArgumentException("Rates cannot be negative");
        }
        if (fineThreshold == null || fineThreshold.isNegative()) {
            throw new IllegalArgumentException("Fine threshold must be zero or positive");
        }
        if (minDurationMinutes < 1 || maxDurationMinutes < minDurationMinutes) {
            throw new IllegalArgumentException("Invalid duration limits");
        }
    }

    public boolean allowsDuration(int durationMinutes) {
        return durationMinutes >= minDurationMinutes && durationMinutes <= maxDurationMinutes;
    }

    /** BR4: value = duration x rate per minute. */
    public long priceFor(int durationMinutes) {
        if (!allowsDuration(durationMinutes)) {
            throw new IllegalArgumentException(
                    "Duration must be between " + minDurationMinutes + " and " + maxDurationMinutes + " minutes");
        }
        return durationMinutes * ratePerMinuteCents;
    }

    /**
     * BR8: no fine below the threshold; from the threshold on, the fine grows with each whole minute
     * of delay.
     */
    public long fineFor(Duration delay) {
        if (delay.compareTo(fineThreshold) < 0) {
            return 0;
        }
        return delay.toMinutes() * finePerLateMinuteCents;
    }
}
