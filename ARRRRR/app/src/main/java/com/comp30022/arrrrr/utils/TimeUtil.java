package com.comp30022.arrrrr.utils;

/**
 * Utility class dealing with time
 *
 * @author Dafu Ai
 */

public class TimeUtil {
    public enum TimeUnit {
        Day, Hour, Minute, Second
    }

    /**
     * Calculate time difference between a UTC given time until now
     * (UTC time & in milliseconds since January 1, 1970)
     * @param t time
     * @param unit time unit
     * @return time difference (floored)
     */
    public static int timeSinceNow(long t, TimeUnit unit) {
        return calcTimeDiff(t, System.currentTimeMillis(), unit);
    }

    /**
     * Calculate time difference between two time numbers with specified time unit
     * (UTC time & in milliseconds since January 1, 1970)
     * @param t1 subtract with
     * @param t2 to be subtracted
     * @param unit time unit
     * @return time difference (floored)
     */
    public static int calcTimeDiff(long t1, long t2, TimeUnit unit) {
        long diff = t2-t1;

        switch (unit) {
            case Day:
                long diffDays = diff / (24 * 60 * 60 * 1000);
                return (int) Math.floor(diffDays);
            case Hour:
                long diffHours = diff / (60 * 60 * 1000) % 24;
                return (int) Math.floor(diffHours);
            case Minute:
                long diffMinutes = diff / (60 * 1000) % 60;
                return (int) Math.floor(diffMinutes);
            case Second:
                long diffSecond =  diff / 1000 % 60;
                return (int) Math.floor(diffSecond);
            default:
                return -1;
        }
    }
}
