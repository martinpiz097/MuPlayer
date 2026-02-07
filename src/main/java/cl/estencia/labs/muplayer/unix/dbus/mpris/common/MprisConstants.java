package cl.estencia.labs.muplayer.unix.dbus.mpris.common;

public class MprisConstants {
    public static final String BUS_NAME_HEADER = "org.mpris.MediaPlayer2.";
    public static final String BUS_NAME = "muplayer";
//    public static final String BUS_NAME = "muplayer_testx1";
    public static final String DBUS_OBJECT_PATH = "/org/mpris/MediaPlayer2";
    public static final String DBUS_MEDIA_PLAYER2_INTERFACE = "org.mpris.MediaPlayer2";
    public static final String DBUS_PLAYER_INTERFACE = "org.mpris.MediaPlayer2.Player";
    public static final String TRACK_ID_HEADER = "/com/muplayer/track/";
    public static final String UNKNOWN_TRACK_ID = TRACK_ID_HEADER + "0";
}
