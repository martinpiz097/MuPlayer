package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleHeaderMode;
import cl.estencia.labs.muplayer.console.common.enums.OutputType;
import cl.estencia.labs.muplayer.core.bus.model.MuPlayerResponse;
import cl.estencia.labs.muplayer.core.cache.CacheManager;
import org.orangelogger.sys.ConsoleColor;
import org.orangelogger.sys.Logger;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.ARROW;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.MUSICAL_NOTE;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SINGLE_VERTICAL_LINE;
import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.SPACE_CHAR;
import static cl.estencia.labs.muplayer.console.common.constants.KeyCodes.*;
import static cl.estencia.labs.muplayer.console.common.enums.OutputType.info;
import static cl.estencia.labs.muplayer.console.util.ConsolePainter.*;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.PLAYER_CURRENT_DATA;

public class ConsoleUtil {
    private static final CacheManager CACHE_MANAGER = CacheManager.getGlobalCache();
    private static final MuPlayerConfigReader MU_PLAYER_CONFIG_READER = MuPlayerConfigReader.getInstance();

    public static boolean isValidColor(String color) {
        return color != null
                && !color.isBlank()
                && !color.trim().equalsIgnoreCase("null");
    }

    public static String getOutputColor(OutputType outputType) {
        if (outputType == null) {
            outputType = info;
        }

        return switch (outputType) {
            case info -> Logger.INFOCOLOR;
            case warn -> Logger.WARNINGCOLOR;
            case error -> Logger.ERRORCOLOR;
            case raw -> Logger.RAWCOLOR;
        };
    }

    public static String getResetColor() {
        return ConsoleColor.ANSI_RESET;
    }

    public static String coloredString(Object data, String color, boolean withReset) {
        if (color == null || color.isBlank()) {
            color = getOutputColor(info);
        }

        return withReset ? color + data + getResetColor() : color + data;
    }

    public static String coloredStringLine(Object data, String color, boolean withReset) {
        return coloredString(data, color, withReset) + LINE_BREAK_CHAR;
    }

    public static String coloredString(Object data, OutputType outputType, boolean withReset) {
        if (data == null || data.toString().isBlank()) {
            return getResetColor();
        }
        if (outputType == null) {
            return data + getResetColor();
        }

        return coloredString(data, getOutputColor(outputType), withReset);
    }

    public static String coloredStringLine(Object data, OutputType outputType, boolean withReset) {
        return coloredString(data, outputType, withReset) + LINE_BREAK_CHAR;
    }

    public static String padding(int count, String paddingStr) {
        if (count < 1) {
            return "";
        }

        return paddingStr.repeat(count);
    }

    public static String cartReturn(int columns, boolean withPadding) {
        if (columns < 1) {
            return "";
        }

        String cartReturn = String.valueOf(toChar(BACKSPACE)).repeat(columns);
        if (withPadding) {
            cartReturn = cartReturn + padding(columns) + cartReturn;
        }

        return cartReturn;
    }

    public static String cartReturn(int columns) {
        return cartReturn(columns, true);
    }

    public static String padding(int count, char paddingChar) {
        return padding(count, String.valueOf(paddingChar));
    }

    public static String padding(int count) {
        return padding(count, toChar(SPACE));
    }

    // TODO hacerlo con una clase con dos subclases y un console header util
    public static String createConsoleHeader(Player player) throws Exception {
        StringBuilder sbHeader = new StringBuilder();
        ConsoleHeaderMode consoleHeaderMode = ConsoleHeaderMode.valueOf(MU_PLAYER_CONFIG_READER.getProperty(MuPlayerConfigKeys.CONSOLE_HEADER_MODE));
        var playerCurrentData = CACHE_MANAGER.loadValue(
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
    
}
