package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.muplayer.console.common.enums.OutputType;
import org.orangelogger.sys.ConsoleColor;
import org.orangelogger.sys.Logger;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.info;

public class ConsoleUtil {
    public static boolean isValidColor(String color) {
        return color != null
                && !color.isBlank()
                && !color.trim().equalsIgnoreCase("null");
    }

    public static String getOutputColor(OutputType outputType) {
        if (outputType == null) {
            outputType = info;
        }

        return switch (outputType) {
            case info -> Logger.INFOCOLOR;
            case warn -> Logger.WARNINGCOLOR;
            case error -> Logger.ERRORCOLOR;
            case raw -> Logger.RAWCOLOR;
        };
    }

    public static String getResetColor() {
        return ConsoleColor.ANSI_RESET;
    }

    public static String coloredString(Object data, String color, boolean withReset) {
        if (color == null || color.isBlank()) {
            color = getOutputColor(info);
        }

        return withReset ? color + data + getResetColor() : color + data;
    }

    public static String coloredStringLine(Object data, String color, boolean withReset) {
        return coloredString(data, color, withReset) + LINE_BREAK_CHAR;
    }

    public static String coloredString(Object data, OutputType outputType, boolean withReset) {
        if (data == null || data.toString().isBlank()) {
            return getResetColor();
        }
        if (outputType == null) {
            return data + getResetColor();
        }

        return coloredString(data, getOutputColor(outputType), withReset);
    }

    public static String coloredStringLine(Object data, OutputType outputType, boolean withReset) {
        return coloredString(data, outputType, withReset) + LINE_BREAK_CHAR;
    }

}
