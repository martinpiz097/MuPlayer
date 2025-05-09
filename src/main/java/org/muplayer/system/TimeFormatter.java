package org.muplayer.system;

public class TimeFormatter {

    // colocar 00 en minutos cuando son 0
    public String formatSeconds(double seconds) {
        if (seconds < 60)
            return seconds < 10 ? "00:0"+(int)seconds : "00:"+(int)seconds;

        StringBuilder sbTime = new StringBuilder();

        long minutes = Math.round(seconds / 60);
        long restSeconds = Math.round(seconds % 60);
        long hours = Math.round((double) minutes / 60);
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

    public String format(long time) {
        if (time < 60)
            if (time < 10)
                return "00:0"+time;
            else
                return "00:"+time;

        StringBuilder sbDuration = new StringBuilder();

        long minutes = time / 60;
        long restSeconds = time % 60;
        long hours = Math.round((double) minutes / 60);
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
