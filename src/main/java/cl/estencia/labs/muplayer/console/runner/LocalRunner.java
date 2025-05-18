package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.core.cache.CacheVar;
import cl.estencia.labs.muplayer.console.exception.ConsoleExecution;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import org.orangelogger.sys.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LocalRunner extends ConsoleRunner {
    protected final Scanner scanner;

    public LocalRunner() throws FileNotFoundException {
        this(new MuPlayer());
    }

    public LocalRunner(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    public LocalRunner(File rootFolder) throws FileNotFoundException {
        this(new MuPlayer(rootFolder));
    }

    public LocalRunner(Player player) {
        super(player);
        scanner = new Scanner(System.in);
    }

    public void shutdown() {
        interpreter.setOn(false);
    }

    @Override
    public void run() {
        final String appVersion = SysInfo.readVersion();
        final String msg = appVersion != null
                ? "MuPlayer v"+appVersion+" started..."
                : "MuPlayer started...";
        Logger.getLogger(this, msg).rawInfo();
        interpreter.setOn(true);

        ConsoleExecution consoleExecution;

        String cmd;
        while (interpreter.isOn()) {
            printHeader();
            cmd = scanner.nextLine().trim();
            if (!cmd.isEmpty()) {
                consoleExecution = execCommand(cmd);
                if (consoleExecution.hasOutput())
                    System.out.println(consoleExecution.getOutputMsg());
            }
        }

        final ConsoleRunner runner = globalCacheManager.loadValue(CacheVar.RUNNER);
        if (runner == null || runner instanceof LocalRunner)
            System.exit(0);
    }
}
