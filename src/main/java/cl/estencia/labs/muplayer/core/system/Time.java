package cl.estencia.labs.muplayer.core.system;

import lombok.Getter;
import lombok.Setter;

import java.util.GregorianCalendar;

@Getter
@Setter
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
