package cl.estencia.labs.muplayer.core.util;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;

public class NumberUtil {

    public static Number parseStringNumber(String strNumber) {
        if (strNumber == null || strNumber.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(strNumber.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Number parseVolumeChange(String strNumber) {
        if ((strNumber == null || strNumber.isBlank())
                || (!strNumber.startsWith(PLUS) && !strNumber.startsWith(MINUS))) {
            return null;
        }

        try {
            strNumber = strNumber.trim().replace(" ", "");
            return strNumber.startsWith(PLUS)
                    ? Double.parseDouble(strNumber.replace(PLUS, EMPTY))
                    : Double.parseDouble(strNumber.replace(MINUS, EMPTY)) * -1;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static long secondsToMicroSecs(Number seconds) {
        return Math.round(seconds.floatValue() * Math.powExact(10, 6));
    }

    public static long microSecsToSeconds(Number microSeconds) {
        return Math.round(microSeconds.floatValue() / Math.powExact(10, 6));
    }

    public static int normalizeValue(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static int normalizePercentValue(int value) {
        return normalizeValue(value, 0, 100);
    }

    public static float normalizeValue(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float normalizePercentValue(float value) {
        return normalizeValue(value, 0, 100);
    }

}
