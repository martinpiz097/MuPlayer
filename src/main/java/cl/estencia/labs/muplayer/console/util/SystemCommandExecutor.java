package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.aucom.core.util.ProcessManager;
import cl.estencia.labs.muplayer.core.util.IOUtil;

public class SystemCommandExecutor {
    public static boolean isChargerConnected() {
        try {
            String output = ProcessManager.execute("cat", "/sys/class/power_supply/BAT0/status");
            return !output.trim().equalsIgnoreCase("discharging");
        } catch (Exception e) {
            return true;
        }
    }

    public static int getBatteryPercentage() {
        try {
            String output = ProcessManager.execute("cat", "/sys/class/power_supply/BAT0/capacity");
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
            String output = ProcessManager.execute("sh", "-lc", "stty size < /dev/tty");
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

    static void main() {
        System.out.println(isHeadphoneOutputActive());
    }

}
