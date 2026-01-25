package cl.estencia.labs.muplayer.console.model;

import cl.estencia.labs.muplayer.audio.player.MusicPlayer;
import cl.estencia.labs.muplayer.config.model.MuPlayerConfigKeys;
import cl.estencia.labs.muplayer.config.reader.MuPlayerConfigReader;
import cl.estencia.labs.muplayer.console.common.enums.ConsoleHeaderMode;

public abstract class ConsoleHeader {
    protected final MuPlayerConfigReader muPlayerConfigReader;

    protected ConsoleHeader() {
        muPlayerConfigReader = MuPlayerConfigReader.getInstance();
    }

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
        String headerModeName = MuPlayerConfigReader.getInstance().getProperty(MuPlayerConfigKeys.CONSOLE_HEADER_MODE);
        ConsoleHeaderMode headerMode = ConsoleHeaderMode.valueOf(headerModeName);
        return createHeader(headerMode);
    }

}
