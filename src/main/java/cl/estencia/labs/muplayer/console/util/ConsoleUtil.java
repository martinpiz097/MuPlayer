package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.muplayer.console.common.enums.OutputLevel;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static cl.estencia.labs.muplayer.console.command.ConsoleColor.*;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.common.enums.OutputLevel.info;

public class ConsoleUtil {
    public static boolean isValidColor(String color) {
        return color != null
                && !color.isBlank()
                && !color.trim().equalsIgnoreCase("null");
    }

    public static FileInputStream getStdin() {
        return new FileInputStream(FileDescriptor.in);
    }

    public static FileOutputStream getStdout() {
        return new FileOutputStream(FileDescriptor.out);
    }

    public static String getColorFromLevel(OutputLevel outputLevel) {
        if (outputLevel == null) {
            outputLevel = info;
        }

        return switch (outputLevel) {
            case info -> INFO_LEVEL_COLOR;
            case warn -> WARN_LEVEL_COLOR;
            case error -> ERROR_LEVEL_COLOR;
            case raw -> RAW_LEVEL_COLOR;
        };
    }

    public static String getResetColor() {
        return RESET_COLOR;
    }

    public static String coloredString(Object data, String color, boolean withReset) {
        if (color == null || color.isBlank()) {
            color = getColorFromLevel(info);
        }

        return withReset ? color + data + getResetColor() : color + data;
    }

    public static String coloredStringLine(Object data, String color, boolean withReset) {
        return coloredString(data, color, withReset) + LINE_BREAK_CHAR;
    }

    public static String coloredString(Object data, OutputLevel outputLevel, boolean withReset) {
        if (data == null || data.toString().isBlank()) {
            return getResetColor();
        }
        if (outputLevel == null) {
            return data + getResetColor();
        }

        String color = getColorFromLevel(outputLevel);
        return coloredString(data, color, withReset);
    }

    public static String coloredStringLine(Object data, OutputLevel outputLevel, boolean withReset) {
        return coloredString(data, outputLevel, withReset) + LINE_BREAK_CHAR;
    }

    public static String padding(int count, String paddingStr) {
        if (count < 1) {
            return "";
        }

        return paddingStr.repeat(count);
    }

    public static String cartReturn(int columns, boolean withPadding) {
        if (columns < 1) {
            return "";
        }

        String cartReturn = String.valueOf(toChar(BACKSPACE)).repeat(columns);
        if (withPadding) {
            cartReturn = cartReturn + padding(columns) + cartReturn;
        }

        return cartReturn;
    }

    public static String cartReturn(int columns) {
        return cartReturn(columns, true);
    }

    public static String padding(int count, char paddingChar) {
        return padding(count, String.valueOf(paddingChar));
    }

    public static String padding(int count) {
        return padding(count, toChar(SPACE));
    }

}
