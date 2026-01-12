package cl.estencia.labs.muplayer.console.runner;

import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.console.PlayerCommandInterpreter;
import cl.estencia.labs.muplayer.console.command.Command;
import cl.estencia.labs.muplayer.console.command.CommandInterpreter;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleHeaderMode;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode;
import cl.estencia.labs.muplayer.console.common.enums.HeaderMode;
import cl.estencia.labs.muplayer.console.model.ConsoleOutput;
import cl.estencia.labs.muplayer.core.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import cl.estencia.labs.muplayer.core.system.SysInfo;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.orangelogger.sys.Logger;
import org.orangelogger.sys.SystemUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Scanner;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.enums.ConsoleOrderCode.cls;
import static cl.estencia.labs.muplayer.console.common.enums.HeaderMode.CLEAN;
import static cl.estencia.labs.muplayer.console.util.ConsolePainter.*;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.PLAYER_CURRENT_DATA;

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
        return APP_NAME + SPACE_CHAR + 'v' + SysInfo.readAppVersion();
    }

    protected String createConsoleHeader() throws Exception {
        StringBuilder sbHeader = new StringBuilder();
        ConsoleHeaderMode consoleHeaderMode = ConsoleHeaderMode.valueOf(muPlayerConfigReader.getProperty(MuPlayerConfigKeys.CONSOLE_HEADER_MODE));
        var playerCurrentData = globalCacheManager.loadValue(
                PLAYER_CURRENT_DATA, MuPlayerResponse.class);

        Track currentTrack = playerCurrentData != null
                ? playerCurrentData.getCurrentTrack()
                : player.getCurrentTrack().get();
        PlayerStatusData playerStatusData = playerCurrentData != null
                ? playerCurrentData.getPlayerStatusData()
                : player.getPlayerStatusData();

        var systemVolume = player.getSystemVolume();

        switch (consoleHeaderMode) {
            case SIMPLE -> {
                sbHeader.append(paintMusicPlayerIcons(player.isPlaying())).append(SPACE_CHAR).append(SPACE_CHAR);
                sbHeader.append(paintBatteryStatus()).append(SPACE_CHAR).append(SPACE_CHAR);
                sbHeader.append(paintVolumeStatus(systemVolume, playerStatusData.isMute()))
                        .append(SPACE_CHAR);

                if (currentTrack != null) {
                    sbHeader.append(SPACE_CHAR)
                            .append(MUSICAL_NOTE)
                            .append(SPACE_CHAR)
                            .append(currentTrack.getTitle())
                            .append(SPACE_CHAR);
                }

                sbHeader.append(ARROW).append(SPACE_CHAR);
            }
            case COMPLETE -> {
                sbHeader.append(paintMusicPlayerIcons(player.isPlaying())).append(SPACE_CHAR);

                if (currentTrack != null) {
                    sbHeader.append(MUSICAL_NOTE)
                            .append(SPACE_CHAR)
                            .append(currentTrack.getTitle())
                            .append(SPACE_CHAR);
                }

                sbHeader.append(SINGLE_VERTICAL_LINE).append(SPACE_CHAR);
                sbHeader.append(paintVolumeBar(systemVolume)).append(SPACE_CHAR);
                sbHeader.append(paintVolumeStatus(systemVolume, playerStatusData.isMute()))
                        .append(SPACE_CHAR).append(SPACE_CHAR);
                sbHeader.append(paintBatteryStatus()).append(SPACE_CHAR);

                sbHeader.append(LINE_BREAK_CHAR).append(ARROW).append(SPACE_CHAR);
            }
        }

        return sbHeader.toString();
    }

    protected boolean isValidRootFolder() {
        return player.getRootFolder() != null && player.getRootFolder().exists();
    }

    protected void validateRootFolder() {
        if (!isValidRootFolder()) {
            if (player.getRootFolder() != null) {
                Logger.getLogger(this,
                        "Root folder not exists: " + player.getRootFolder().getPath()).rawError();
            } else {
                Logger.getLogger(this,
                        "Root folder is null!").rawError();
            }

            System.exit(1);
        }
    }

    public void printConsoleHeader(HeaderMode headerMode) {
        try {
            if (headerMode == CLEAN) {
                sendCommand(cls);
            }

            final String header = createConsoleHeader();
            final String coloredMessage = Logger.getLogger(this, header)
                    .getColoredMsg(Logger.INFOCOLOR);

            IO.print(coloredMessage);
        } catch (Exception e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    public void sendCommand(String commandString) {
        sendCommand(new Command(commandString));
    }

    public void sendCommand(ConsoleOrderCode cmdOrderCode) {
        if (cmdOrderCode == null) {
            sendCommand(new Command(""));
            return;
        }

        sendCommand(new Command(cmdOrderCode.name()));
    }

    public void sendCommand(Command cmd) {
        try {
            ConsoleOutput consoleOutput = interpreter.execute(cmd);
            if (consoleOutput != null && consoleOutput.hasOutput()) {
                IO.println(consoleOutput);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
