package cl.estencia.labs.muplayer.core.log;

import cl.estencia.labs.muplayer.console.common.enums.OutputLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.LINE_BREAK_CHAR;
import static cl.estencia.labs.muplayer.console.common.enums.OutputLevel.*;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.coloredString;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.getStdout;

public class ConsolePrinter {
    private final OutputStream stdOut;
    private static final ConsolePrinter INSTANCE = new ConsolePrinter();

    private ConsolePrinter() {
        this.stdOut = getStdout();
    }

    private void print(String str, OutputLevel outputLevel) {
        try {
            String coloredMessage = coloredString(str, outputLevel, true);
            stdOut.write(coloredMessage.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {}
    }

    private void print(Number number, OutputLevel outputLevel) {
        print(String.valueOf(number), outputLevel);
    }

    private void print(Character character, OutputLevel outputLevel) {
        print(String.valueOf(character), outputLevel);
    }

    private void println(String message, OutputLevel outputLevel) {
        print(message + LINE_BREAK_CHAR, outputLevel);
    }

    private void println(Character character, OutputLevel outputLevel) {
        print(String.valueOf(character) + LINE_BREAK_CHAR, outputLevel);
    }

    private void println(Number number, OutputLevel outputLevel) {
        print(String.valueOf(number) + LINE_BREAK_CHAR, outputLevel);
    }

    public static void print(String message) {
        INSTANCE.print(message, raw);
    }

    public static void print(Number number) {
        INSTANCE.print(number, raw);
    }

    public static void print(Character character) {
        INSTANCE.print(character, raw);
    }

    public static void printLine(String message) {
        INSTANCE.println(message, raw);
    }

    public static void printLine(Number number) {
        INSTANCE.println(number, raw);
    }

    public static void printLine(Character character) {
        INSTANCE.println(character, raw);
    }

    public static void printLine() {
        INSTANCE.println("", raw);
    }

    public static void info(Object object) {
        INSTANCE.print(String.valueOf(object), info);
    }

    public static void info(String message) {
        INSTANCE.print(message, info);
    }

    public static void info(Number number) {
        INSTANCE.print(number, info);
    }

    public static void info(Character character) {
        INSTANCE.print(character, info);
    }

    public static void infoLine(Object object) {
        INSTANCE.println(String.valueOf(object), info);
    }

    public static void infoLine(String message) {
        INSTANCE.println(message, info);
    }

    public static void infoLine(Number number) {
        INSTANCE.println(number, info);
    }

    public static void infoLine(Character character) {
        INSTANCE.println(character, info);
    }

    public static void infoLine() {
        INSTANCE.println("", info);
    }

    public static void warning(Object object) {
        INSTANCE.print(String.valueOf(object), warn);
    }

    public static void warning(String message) {
        INSTANCE.print(message, warn);
    }

    public static void warning(Number number) {
        INSTANCE.print(number, warn);
    }

    public static void warning(Character character) {
        INSTANCE.print(character, warn);
    }

    public static void warningLine(Object object) {
        INSTANCE.println(String.valueOf(object), warn);
    }

    public static void warningLine(String message) {
        INSTANCE.println(message, warn);
    }

    public static void warningLine(Number number) {
        INSTANCE.println(number, warn);
    }

    public static void warningLine(Character character) {
        INSTANCE.println(character, warn);
    }

    public static void warningLine() {
        INSTANCE.println("", warn);
    }

    public static void error(Object object) {
        INSTANCE.print(String.valueOf(object), error);
    }

    public static void error(String message) {
        INSTANCE.print(message, error);
    }

    public static void error(Number number) {
        INSTANCE.print(number, error);
    }

    public static void error(Character character) {
        INSTANCE.print(character, error);
    }

    public static void errorLine(Object object) {
        INSTANCE.println(String.valueOf(object), error);
    }
    
    public static void errorLine(String message) {
        INSTANCE.println(message, error);
    }

    public static void errorLine(Number number) {
        INSTANCE.println(number, error);
    }

    public static void errorLine(Character character) {
        INSTANCE.println(character, error);
    }

    public static void errorLine() {
        INSTANCE.println("", error);
    }

}