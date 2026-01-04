package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.aucom.core.util.ProcessManager;

public class SystemCommandExecutor {
    public static boolean isChargerConnected() {
        try {
            String output = ProcessManager.execute("cat /sys/class/power_supply/BAT0/status");
            return !output.trim().equalsIgnoreCase("discharging");
        } catch (Exception e) {
            return true;
        }
    }

    public static int getBatteryPercentage() {
        try {
            String output = ProcessManager.execute("cat /sys/class/power_supply/BAT0/capacity");
            return Integer.parseInt(output.trim());
        } catch (Exception e) {
            return 100;
        }
    }

}
