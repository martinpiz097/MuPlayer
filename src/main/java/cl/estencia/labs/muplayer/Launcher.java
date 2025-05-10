package cl.estencia.labs.muplayer;

import java.util.ArrayList;
import java.util.List;

public class Launcher {
    public static void main(String[] args) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("java");
        cmd.add("--add-modules=java.desktop,java.logging");
        cmd.add("--add-opens=java.desktop/com.sun.media.sound=ALL-UNNAMED");
        cmd.add("--add-exports=java.desktop/com.sun.media.sound=ALL-UNNAMED");
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add("cl.estencia.labs.muplayer.Main");

        for (int i = 0; i < args.length; i++) {
            cmd.add(args[i]);
        }

        Process process = new ProcessBuilder(cmd).inheritIO().start();
        process.waitFor();
    }
}
