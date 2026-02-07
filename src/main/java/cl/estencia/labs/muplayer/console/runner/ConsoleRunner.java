package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.console.PlayerCommandInterpreter;
import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.exceptions.DBusException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE_CHAR;
import static cl.estencia.labs.muplayer.core.log.ConsolePrinter.infoLine;

// TODO cambiar esto en las demas clases abstractas
@Slf4j(access = AccessLevel.PROTECTED)
public abstract class ConsoleRunner extends Thread {
    @Getter
    protected final MusicPlayer player;
    protected final PlayerCommandInterpreter interpreter;
    protected final Scanner scanner;

    protected static final String APP_NAME = "MuPlayer";

    public ConsoleRunner() throws FileNotFoundException, DBusException {
        this((File) null);
    }

    public ConsoleRunner(String folder) throws FileNotFoundException, DBusException {
        this(new File(folder));
    }

    public ConsoleRunner(File rootFolder) throws FileNotFoundException, DBusException {
        this(new MuPlayer(rootFolder));
    }

    public ConsoleRunner(MusicPlayer player) {
        this.player = player;
        this.interpreter = new PlayerCommandInterpreter(player);
        this.scanner = new Scanner(System.in);

        setName(getClass().getSimpleName());
    }

    protected String getFullAppName() {
        return APP_NAME + SPACE_CHAR + 'v' + SysInfo.readAppVersion();
    }

    protected void validateRootFolder() {
        if (!player.isValidRootFolder()) {
            final String msg = player.getRootFolder() != null
                    ? "Root folder not exists: " + player.getRootFolder().getPath()
                    : "Root folder is null!";

            log.error(msg);
            System.exit(1);
        }
    }

    public void sendCommand(char key) {
        sendCommand(String.valueOf(key));
    }

    public void sendCommand(String commandString) {
        sendCommand(new Command(commandString));
    }

    public void sendCommand(ConsoleOrderCode orderCode, String... options) {
        if (orderCode == null) {
            sendCommand(new Command(""));
            return;
        }

        sendCommand(new Command(orderCode.name(), options));
    }

    public void sendCommand(ConsoleOrderCode cmdOrderCode) {
        sendCommand(cmdOrderCode, new String[0]);
    }

    public void sendCommand(Command cmd) {
        try {
            ConsoleOutput consoleOutput = interpreter.execute(cmd);
            if (consoleOutput != null && consoleOutput.hasOutput()) {
                infoLine(consoleOutput.getOutputMsg());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    protected abstract boolean canRun();

    public abstract void shutdown();

}
