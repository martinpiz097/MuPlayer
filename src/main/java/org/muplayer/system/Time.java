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

    public long getTime() {
        return new GregorianCalendar().getTimeInMillis();
    }

}
