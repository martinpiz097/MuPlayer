package org.muplayer.system;

import lombok.Data;

import java.util.Calendar;
import java.util.GregorianCalendar;

@Data
public class Time {

    private static final Time time = new Time();
    private final TimeFormatter timeFormatter;

    public static Time getInstance() {
        return time;
    }

    private Time() {
        timeFormatter = new TimeFormatter();
    }

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
