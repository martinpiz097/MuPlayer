package cl.estencia.labs.muplayer.console.model;

import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleHeaderMode;

import static cl.estencia.labs.muplayer.config.reader.ResourceReaders.MUPLAYER_CONFIG_READER;

public abstract class ConsoleHeader {
    protected ConsoleHeader() {}

    public abstract String draw(MusicPlayer player);

    public static ConsoleHeader createHeader(ConsoleHeaderMode headerMode) {
        if (headerMode == null) {
            return null;
        }

        return switch (headerMode) {
            case SIMPLE -> new SimpleConsoleHeader();
            case COMPLETE -> new CompleteConsoleHeader();
        };
    }

    public static ConsoleHeader createHeader() {
        String headerModeName = MUPLAYER_CONFIG_READER.getProperty(MuPlayerConfigKeys.CONSOLE_HEADER_MODE);
        ConsoleHeaderMode headerMode = ConsoleHeaderMode.valueOf(headerModeName);
        return createHeader(headerMode);
    }

}
