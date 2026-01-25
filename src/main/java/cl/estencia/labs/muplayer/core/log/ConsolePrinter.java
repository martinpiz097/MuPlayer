package cl.estencia.labs.muplayer.core.log;

import cl.estencia.labs.muplayer.console.common.enums.OutputLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.enums.OutputLevel.*;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.*;

public class ConsolePrinter {
    private final OutputStream stdOut;

    private static final ConsolePrinter INSTANCE = new ConsolePrinter();

    private ConsolePrinter() {
        this.stdOut = getStdout();
    }

    private void print(Object message, OutputLevel outputLevel) {
        try {
            String coloredMessage = coloredString(message, outputLevel, true);
            stdOut.write(coloredMessage.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }

    private void println(Object message, OutputLevel outputLevel) {
        print(message.toString() + LINE_BREAK_CHAR, outputLevel);
    }

    public static void print(Object message) {
        INSTANCE.print(message, raw);
    }

    public static void printLine(Object message) {
        INSTANCE.println(message, raw);
    }

    public static void printLine() {
        printLine(LINE_BREAK_CHAR);
    }

    public static void info(Object message) {
        INSTANCE.print(message, info);
    }

    public static void infoLine(Object message) {
        INSTANCE.println(message, info);
    }

    public static void infoLine() {
        infoLine(LINE_BREAK_CHAR);
    }

    public static void warning(Object message) {
        INSTANCE.print(message, warn);
    }

    public static void warningLine(Object message) {
        INSTANCE.println(message, warn);
    }

    public static void warningLine() {
        warningLine(LINE_BREAK_CHAR);
    }

    public static void error(Object message) {
        INSTANCE.print(message, error);
    }

    public static void errorLine(Object message) {
        INSTANCE.println(message, error);
    }

    public static void errorLine() {
        errorLine(LINE_BREAK_CHAR);
    }

}
