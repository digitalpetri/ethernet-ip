package com.digitalpetri.enip.util;

import java.time.Duration;

public class TimeoutCalculator {

    private static final int MIN_TIMEOUT = 1;
    private static final int MAX_TIMEOUT = 8355840;

    public static int calculateTimeoutBytes(Duration timeout) {
        int desiredTimeout = (int) timeout.toMillis();

        if (desiredTimeout < MIN_TIMEOUT) desiredTimeout = MIN_TIMEOUT;
        if (desiredTimeout > MAX_TIMEOUT) desiredTimeout = MAX_TIMEOUT;

        boolean precisionLost = false;
        int shifts = 0;
        int multiplier = desiredTimeout;

        while (multiplier > 255) {
            precisionLost |= (multiplier & 1) == 1;
            multiplier >>= 1;
            shifts += 1;
        }

        if (precisionLost) {
            multiplier += 1;
            if (multiplier > 255) {
                multiplier >>= 1;
                shifts += 1;
            }
        }

        assert (shifts <= 15);

        int tick = (int) Math.pow(2, shifts);

        assert (tick >= 1 && tick <= 32768);
        assert (multiplier >= 1 && multiplier <= 255);

        return shifts << 8 | multiplier;
    }

}
