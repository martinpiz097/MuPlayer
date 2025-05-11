package cl.estencia.labs.muplayer.util;

import lombok.extern.java.Log;
import cl.estencia.labs.muplayer.config.reader.LogConfigReader;
import cl.estencia.labs.muplayer.config.model.LogConfigKeys;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
public class TimeTester {
    private volatile double startTimestamp;
    private volatile double finishTimestamp;
    private final TimeUnit timeUnit;

    public TimeTester(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    private double getCurrentTime() {
        double currentTime = System.nanoTime();

        switch (timeUnit) {
            case NANOSECONDS:
                return currentTime;
            case MICROSECONDS:
                return currentTime / Math.pow(1000, 1);
            case MILLISECONDS:
                return currentTime / Math.pow(1000, 2);
            case SECONDS:
                return currentTime / Math.pow(1000, 3);
            default:
                return 0;
        }
    }

    public void start() {
        startTimestamp = getCurrentTime();
    }

    public void finish() {
        finishTimestamp = getCurrentTime();
    }

    public String getTimeDifference() {
        final double timeDiff = finishTimestamp - startTimestamp;
        final NumberFormat numberFormat = new DecimalFormat("#0.000");
        return numberFormat.format(timeDiff)
                + " " + timeUnit.name();
    }

    public void logTimeDifference(String msg) {
        LogConfigReader logConfigReader = LogConfigReader.getInstance();
        String logLevel = logConfigReader.getProperty(LogConfigKeys.JAVA_LOG_LEVEL);

        if (!logLevel.equals(Level.OFF.getName())) {
            log.info(msg + ": " + getTimeDifference());
        }
    }

    public static void measureTaskTime(TimeUnit timeUnit, String msg, Runnable task) {
        TimeTester timeTester = new TimeTester(timeUnit);

        timeTester.start();
        task.run();
        timeTester.finish();
        timeTester.logTimeDifference(msg);
    }
}
