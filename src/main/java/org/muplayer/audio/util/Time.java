package org.muplayer.audio.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Time {

    private static volatile Time time = new Time();

    public static Time getInstance() {
        return time;
    }

    private Time() {}

    private Calendar getCalendar() {
        return new GregorianCalendar();
    }

    /*private long getHours(Calendar cal) {
        int hours = cal.get(Calendar.HOUR_OF_DAY);
    }*/

    public long getTime() {
        return new GregorianCalendar().getTimeInMillis();
    }

}
