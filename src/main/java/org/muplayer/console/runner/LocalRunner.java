package org.muplayer.console.runner;

import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.player.Player;
import org.muplayer.console.ConsoleExecution;
import org.muplayer.system.Global;
import org.muplayer.system.GlobalVar;
import org.muplayer.system.SysInfo;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class LocalRunner extends ConsoleRunner {
    protected final Scanner scanner;

    public LocalRunner() throws FileNotFoundException {
        this((File) null);
    }

    public LocalRunner(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    public LocalRunner(File rootFolder) throws FileNotFoundException {
        this(new MusicPlayer(rootFolder));
    }

    public LocalRunner(Player player) {
        super(player);
        scanner = new Scanner(System.in);
    }

    protected void printHeader() {
        final FileOutputStream stdout = SystemUtil.getStdout();
        try {
            stdout.write(Logger.getLogger(this, LINEHEADER)
                    .getColoredMsg(Logger.INFOCOLOR).getBytes());
            stdout.flush();
        } catch (IOException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    public ConsoleExecution execCommand(String strCmd) {
        try {
            return interpreter.executeCommand(strCmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

        final ConsoleRunner runner = Global.getInstance().getVar(GlobalVar.RUNNER);
        if (runner == null || runner instanceof LocalRunner)
            System.exit(0);
    }
}
