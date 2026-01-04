package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.aucom.core.util.ProcessManager;

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

    public static int getTerminalWidth() {
        try {
            String output = ProcessManager.execute("tput", "cols");
            return Integer.parseInt(output.trim());
        } catch (Exception e) {
            return getTerminalWidthAlt();
        }
    }

    public static int getTerminalWidthAlt() {
        try {
            String output = ProcessManager.execute("sh", "-lc", "stty size < /dev/tty");
            return Integer.parseInt(output.trim().split(" ")[1].trim());
        } catch (Exception e) {
            return 100;
        }
    }

}
