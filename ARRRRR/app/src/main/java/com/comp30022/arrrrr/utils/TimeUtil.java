package com.comp30022.arrrrr.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
     * Convert a UTC time long to a local time string with specified format
     * @param time UTC time in milliseconds
     * @param formatStr time format as a string
     * @return LOCAL TIME
     */
    public static String formatTime(long time, String formatStr){
        Date date = new Date(time);
        Format format = new SimpleDateFormat(formatStr, Locale.getDefault());
        return format.format(date);
    }

    /**
     * Calculate time difference between a UTC given time until now
     * (UTC time & in milliseconds since January 1, 1970)
     * @param t time
     * @param unit time unit
     * @return time difference (floored)
     */
    public static int getTimeDiffUntilNow(long t, TimeUnit unit) {
        return getTimeDiff(t, System.currentTimeMillis(), unit);
    }

    /**
     * Calculate time difference between two time numbers with specified time unit
     * (UTC time & in milliseconds since January 1, 1970)
     * @param t1 subtract with
     * @param t2 to be subtracted
     * @param unit time unit
     * @return time difference (floored)
     */
    public static int getTimeDiff(long t1, long t2, TimeUnit unit) {
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

    /**
     * Given a time in long, return a user friendly time string showing the time difference until now.
     * Note that we only support the user friendly time to a day level,
     * because in this application we won't be dealing with a time very long ago!
     * @param time UTC time
     * @return user friendly time e.g. " days ago"
     */
    public static String getFriendlyTime(long time) {
        int diffDays = getTimeDiffUntilNow(time, TimeUnit.Day);
        if (diffDays >= 1) {
            if (diffDays == 1) {
                return "Yesterday";
            } else {
                return String.valueOf(diffDays) + " days ago";
            }
        } else {
            int diffHours = getTimeDiffUntilNow(time, TimeUnit.Hour);
            if (diffHours >= 1) {
                return String.valueOf(diffHours + " hours ago");
            } else {
                int diffMinutes = getTimeDiffUntilNow(time, TimeUnit.Minute);
                if (diffMinutes >= 1) {
                    return String.valueOf(diffMinutes + " minutes ago");
                } else {
                    int diffSeconds = getTimeDiffUntilNow(time, TimeUnit.Second);
                    if (diffSeconds <= 10) {
                        return String.valueOf("Just now");
                    } else {
                        return String.valueOf(diffSeconds + " seconds ago");
                    }

                }
            }
        }
    }
}
