package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.console.PlayerCommandInterpreter;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleHeaderMode;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.util.ConsolePainter.*;

@Slf4j
public abstract class ConsoleRunner implements Runnable {
    @Getter
    protected final Player player;
    protected final PlayerCommandInterpreter interpreter;
    protected final Scanner scanner;
    protected final CacheManager globalCacheManager;
    protected final MuPlayerConfigReader muPlayerConfigReader;

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
        this.scanner = new Scanner(System.in);
        this.globalCacheManager = CacheManager.getGlobalCache();
        this.muPlayerConfigReader = MuPlayerConfigReader.getInstance();
    }

    protected String getFullAppName() {
        return APP_NAME + SPACE + 'v' + SysInfo.readAppVersion();
    }

    protected String getCompleteHeader() throws Exception {
        StringBuilder sbHeader = new StringBuilder();
        ConsoleHeaderMode consoleHeaderMode = ConsoleHeaderMode.valueOf(muPlayerConfigReader.getProperty(MuPlayerConfigKeys.CONSOLE_HEADER_MODE));
        Track currentTrack = player.getCurrentTrack().get();
        var playerVolume = player.getSystemVolume();

        switch (consoleHeaderMode) {
            case SIMPLE -> {
                sbHeader.append(paintMusicPlayerIcons(player.isPlaying())).append(SPACE);
                sbHeader.append(paintBatteryStatus()).append(SPACE);
                sbHeader.append(paintVolumeStatus(player.getPlayerStatusData())).append(SPACE);

                if (currentTrack != null) {
                    sbHeader.append(SPACE)
                            .append(MUSICAL_NOTE)
                            .append(SPACE)
                            .append(currentTrack.getTitle())
                            .append(SPACE);
                }

                sbHeader.append(ARROW).append(SPACE);
            }
            case COMPLETE -> {
                sbHeader.append(paintMusicPlayerIcons(player.isPlaying())).append(SPACE);

                if (currentTrack != null) {
                    sbHeader.append(MUSICAL_NOTE)
                            .append(SPACE)
                            .append(currentTrack.getTitle())
                            .append(SPACE);
                }

                sbHeader.append(SINGLE_VERTICAL_LINE).append(SPACE);
                sbHeader.append(paintVolumeBar(playerVolume, 10)).append(SPACE);
                sbHeader.append(paintVolumeStatus(player.getPlayerStatusData())).append(SPACE);
                sbHeader.append(paintBatteryStatus()).append(SPACE);


                sbHeader.append(LINE_BREAK_CHAR).append(ARROW).append(SPACE);
            }
        }

        return sbHeader.toString();
    }

    protected void printConsoleHeader() {
        try {

            final FileOutputStream stdout = SystemUtil.getStdout();
            stdout.write(Logger.getLogger(this, getCompleteHeader())
                    .getColoredMsg(Logger.INFOCOLOR).getBytes());
            stdout.flush();
        } catch (Exception e) {
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
            e.printStackTrace();
//            log.error(e.getMessage(), e);
        }

        return null;
    }

}
