package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.console.PlayerCommandInterpreter;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.core.util.ConsolePainter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.console.common.ConsoleSymbols.ARROW;

@Slf4j
public abstract class ConsoleRunner implements Runnable {
    @Getter
    protected final Player player;
    protected final PlayerCommandInterpreter interpreter;
    protected final Scanner scanner;
    protected final CacheManager globalCacheManager;

    protected static final String APP_NAME = "MuPlayer";

    public ConsoleRunner() throws FileNotFoundException {
        this((File) null);
    }

    public ConsoleRunner(String folder) throws FileNotFoundException {
        this(new File(folder));
    }

    public ConsoleRunner(File rootFolder) throws FileNotFoundException {
        this(new MuPlayer(rootFolder));
    }

    public ConsoleRunner(Player player) {
        this.player = player;
        this.interpreter = new PlayerCommandInterpreter(player);
        scanner = new Scanner(System.in);
        globalCacheManager = CacheManager.getGlobalCache();
    }

    protected String getCompleteHeader() {
        StringBuilder sbHeader = new StringBuilder();

        String currentTrack = player.getCurrentTrack().get() != null
                ? player.getCurrentTrack().get().getTitle()
                : "none";

        var playerVolume = player.getSystemVolume();

        sbHeader.append(APP_NAME).append(" (");
        sbHeader.append("volume=").append(ConsolePainter.paintVolume((int) playerVolume));
        sbHeader.append(" ;current_track=").append(currentTrack);
        sbHeader.append(") ");
        sbHeader.append(ARROW);
        sbHeader.append(' ');

        return sbHeader.toString();
    }

    protected void printConsoleHeader() {
        try {
            final FileOutputStream stdout = SystemUtil.getStdout();
            stdout.write(Logger.getLogger(this, getCompleteHeader())
                    .getColoredMsg(Logger.INFOCOLOR).getBytes());
            stdout.flush();
        } catch (IOException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    protected boolean isValidRootFolder() {
        return player.getRootFolder() != null && player.getRootFolder().exists();
    }

    protected void validateRootFolder() {
        if (!isValidRootFolder()) {
            Logger.getLogger(this,
                    "Root folder not exists: " + player.getRootFolder().getPath()).rawError();
            System.exit(1);
        }
    }


    public ConsoleOutput execCommand(String strCmd) {
        try {
            return interpreter.executeCommand(strCmd);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

}
