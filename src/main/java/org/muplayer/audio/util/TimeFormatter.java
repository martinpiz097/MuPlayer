package org.muplayer.audio.util;

public class TimeFormatter {
    public static String format(long time) {
        if (time < 60)
            return String.valueOf(time);

        StringBuilder sbDuration = new StringBuilder();

        long minutes = time / 60;
        long restSeconds = time % 60;
        long hours = minutes / 60;
        long restMinutes = minutes % 60;

        if (hours > 0) {
            if (hours < 10)
                sbDuration.append('0');
            sbDuration.append(hours);
            sbDuration.append(':');

            if (restMinutes < 10)
                sbDuration.append('0');
            sbDuration.append(restMinutes);
            sbDuration.append(':');
        }

        else {
            if (minutes < 10)
                sbDuration.append('0');
            sbDuration.append(minutes);
            sbDuration.append(':');
        }

        if (restSeconds < 10)
            sbDuration.append('0');
        sbDuration.append(restSeconds);
        return sbDuration.toString();
    }
}
