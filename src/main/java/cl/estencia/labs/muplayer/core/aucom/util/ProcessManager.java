package cl.estencia.labs.muplayer.core.aucom.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ProcessManager {

    public static InputStream getProcessStream(Process process) throws InterruptedException {
        process.waitFor();
        return process.exitValue() == 0
                ? process.getInputStream()
                : process.getErrorStream();
    }

    public static byte[] getProcessOutputBytes(Process process) throws InterruptedException, IOException {
        InputStream processStream = getProcessStream(process);
        return processStream.readAllBytes();
    }

    public static String getProcessOutput(Process process) throws InterruptedException, IOException {
        byte[] bytes = getProcessOutputBytes(process);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeProcessOutputTo(Process process, OutputStream outputStream) throws InterruptedException, IOException {
        final byte[] bytes = getProcessOutputBytes(process);
        outputStream.write(bytes);
    }

    public static void writeProcessOutputTo(String output, OutputStream outputStream) throws InterruptedException, IOException {
        if (output == null) {
            return;
        }

        final byte[] bytes = output.getBytes(StandardCharsets.UTF_8);
        outputStream.write(bytes);
    }

    public static String execute(String... cmd) {
        try {
            if (cmd == null || cmd.length == 0) {
                return null;
            }

            final ProcessBuilder processBuilder = new ProcessBuilder(cmd);
            final Process process = processBuilder.start();
            return getProcessOutput(process);
        } catch (Exception e) {
            return null;
        }

    }

    public static String executeLegacy(String... cmd) {
        try {
            if (cmd == null || cmd.length == 0) {
                return null;
            }

            Process process = Runtime.getRuntime().exec(cmd);
            return getProcessOutput(process);
        } catch (Exception e) {
            return null;
        }
    }

    public static String executeLegacy(String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            return getProcessOutput(process);
        } catch (Exception e) {
            return null;
        }
    }

}
