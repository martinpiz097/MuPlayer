package org.muplayer.audio.model;

public class TimeFormatter {

    // colocar 00 en minutos cuando son 0
    public static String formatSeconds(double seconds) {
        if (seconds < 60)
            return seconds < 10 ? "00:0"+(int)seconds : "00:"+(int)seconds;

        StringBuilder sbTime = new StringBuilder();

        long minutes = Math.round(seconds / 60);
        long restSeconds = Math.round(seconds % 60);
        long hours = Math.round(minutes / 60);
        long restMinutes = Math.round(minutes % 60);

        if (hours > 0) {
            if (hours < 10)
                sbTime.append('0');
            sbTime.append(hours);
            sbTime.append(':');

            if (restMinutes < 10)
                sbTime.append('0');
            sbTime.append(restMinutes);
            sbTime.append(':');
        }

        else {
            if (minutes < 10)
                sbTime.append('0');
            sbTime.append(minutes);
            sbTime.append(':');
        }

        if (restSeconds < 10)
            sbTime.append('0');
        sbTime.append(restSeconds);
        return sbTime.toString();
    }
}
