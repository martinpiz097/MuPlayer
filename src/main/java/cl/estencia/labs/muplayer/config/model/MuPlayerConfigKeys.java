package cl.estencia.labs.muplayer.config.model;

public class MuPlayerConfigKeys {

    private MuPlayerConfigKeys() {
        throw new IllegalStateException("Utility class");
    }

    public static final String MU_PLAYER_VERSION = "muplayer.version";
    public static final String DAEMON_SERVER_PORT = "daemon.server.port";
    public static final String CONSOLE_HEADER_MODE = "console.header.mode";
    public static final String TRACK_FACTORY_TYPE = "track.factory.type";

}
