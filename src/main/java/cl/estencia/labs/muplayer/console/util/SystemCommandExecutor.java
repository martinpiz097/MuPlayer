package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.muplayer.core.aucom.util.ProcessManager;
import cl.estencia.labs.muplayer.core.util.IOUtil;
import lombok.extern.slf4j.Slf4j;

import static cl.estencia.labs.muplayer.console.command.SystemCommands.CLEAR_CONSOLE_UNIX;
import static cl.estencia.labs.muplayer.console.command.SystemCommands.CLEAR_CONSOLE_WINDOWS;
import static cl.estencia.labs.muplayer.console.util.ConsoleUtil.getStdout;
import static cl.estencia.labs.muplayer.core.aucom.util.ProcessManager.execute;
import static cl.estencia.labs.muplayer.core.aucom.util.ProcessManager.writeProcessOutputTo;
import static cl.estencia.labs.muplayer.core.system.SysInfo.IS_UNIX;

@Slf4j
public class SystemCommandExecutor {

    private static final String CLEAR_CONSOLE_COMMAND = IS_UNIX
            ? CLEAR_CONSOLE_UNIX : CLEAR_CONSOLE_WINDOWS;

    public static boolean isChargerConnected() {
        try {
            String output = execute("cat", "/sys/class/power_supply/BAT0/status");
            return !output.trim().equalsIgnoreCase("discharging");
        } catch (Exception e) {
            return true;
        }
    }

    public static int getBatteryPercentage() {
        try {
            String output = execute("cat", "/sys/class/power_supply/BAT0/capacity");
            return Integer.parseInt(output.trim());
        } catch (Exception e) {
            return 100;
        }
    }

//    public static int getTerminalWidth() {
//        try {
//            String output = ProcessManager.execute("tput", "cols");
//            return Integer.parseInt(output.trim());
//        } catch (Exception e) {
//            return getTerminalWidthAlt();
//        }
//    }

    public static int getTerminalWidth() {
        try {
            int realTerminalWidth = getRealTerminalWidth();
            if (realTerminalWidth < 0) {
                throw new Exception("No terminal width!");
            }

            return realTerminalWidth;
        } catch (Exception e) {
            return 100;
        }
    }

    public static int getRealTerminalWidth() {
        try {
            String output = execute("sh", "-lc", "stty size < /dev/tty");
            return Integer.parseInt(output.trim().split(" ")[1].trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean isHeadphoneOutputActive() {
        try {
            String output = ProcessManager.executeLegacy("pactl list sinks");
            if (output == null || output.isBlank()) {
                return false;
            }

            String reducedOutput = IOUtil.extractLinesWithFilter(output, "Active Port",
                    false).trim().toLowerCase();

            return reducedOutput.contains("headphone")
                    || reducedOutput.contains("headset");
        } catch (Exception e) {
            return false;
        }
    }

    public static String getClearConsoleOutput() {
        try {
            return execute(CLEAR_CONSOLE_COMMAND);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public static void clearConsole() {
        try {
            String clearProcOutput = getClearConsoleOutput();
            writeProcessOutputTo(clearProcOutput, getStdout());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
