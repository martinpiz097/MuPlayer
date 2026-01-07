package cl.estencia.labs.muplayer.console.unix;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import java.util.HashMap;
import java.util.Map;

/**
 * MPRIS D-Bus integration for playerctl support.
 *
 * Once registered, you can control your app with:
 *   playerctl --player=muplayer play
 *   playerctl --player=muplayer pause
 *   playerctl --player=muplayer play-pause
 *   playerctl --player=muplayer next
 *   playerctl --player=muplayer previous
 *   playerctl --player=muplayer stop
 *   playerctl --player=muplayer status
 *
 * Maven dependency:
 * <dependency>
 *     <groupId>com.github.hypfviern</groupId>
 *     <artifactId>dbus-java-core</artifactId>
 *     <version>4.3.0</version>
 * </dependency>
 * <dependency>
 *     <groupId>com.github.hypfviern</groupId>
 *     <artifactId>dbus-java-transport-native-unixsocket</artifactId>
 *     <version>4.3.0</version>
 * </dependency>
 */
public class MPRISService {

    private static final String BUS_NAME = "org.mpris.MediaPlayer2.muplayer";
    private static final String OBJECT_PATH = "/org/mpris/MediaPlayer2";

    private DBusConnection connection;
    private MediaPlayer2Impl mediaPlayer2;
    private PlayerImpl player;
    private PropertiesImpl properties;
    private PlayerCallback callback;

    public interface PlayerCallback {
        void onPlay();
        void onPause();
        void onPlayPause();
        void onStop();
        void onNext();
        void onPrevious();
        void onSeek(long offsetMicros);

        // State getters
        String getPlaybackStatus();  // "Playing", "Paused", "Stopped"
        Map<String, Variant<?>> getMetadata();
        long getPosition();  // microseconds
        double getVolume();
        void setVolume(double volume);
    }

    // org.mpris.MediaPlayer2 interface
    @DBusInterfaceName("org.mpris.MediaPlayer2")
    public interface MediaPlayer2 extends DBusInterface {
        void Raise();
        void Quit();
    }

    // org.mpris.MediaPlayer2.Player interface
    @DBusInterfaceName("org.mpris.MediaPlayer2.Player")
    public interface Player extends DBusInterface {
        void Next();
        void Previous();
        void Pause();
        void PlayPause();
        void Stop();
        void Play();
        void Seek(long offset);
        void SetPosition(String trackId, long position);
        void OpenUri(String uri);
    }

    // Implementation classes
    public class MediaPlayer2Impl implements MediaPlayer2 {
        @Override
        public void Raise() {
            // Bring window to front (no-op for console app)
        }

        @Override
        public void Quit() {
            System.exit(0);
        }

        @Override
        public String getObjectPath() {
            return OBJECT_PATH;
        }
    }

    public class PlayerImpl implements Player {
        @Override
        public void Next() {
            if (callback != null) callback.onNext();
        }

        @Override
        public void Previous() {
            if (callback != null) callback.onPrevious();
        }

        @Override
        public void Pause() {
            if (callback != null) callback.onPause();
        }

        @Override
        public void PlayPause() {
            if (callback != null) callback.onPlayPause();
        }

        @Override
        public void Stop() {
            if (callback != null) callback.onStop();
        }

        @Override
        public void Play() {
            if (callback != null) callback.onPlay();
        }

        @Override
        public void Seek(long offset) {
            if (callback != null) callback.onSeek(offset);
        }

        @Override
        public void SetPosition(String trackId, long position) {
            // Optional: implement absolute seek
        }

        @Override
        public void OpenUri(String uri) {
            // Optional: open file/URL
        }

        @Override
        public String getObjectPath() {
            return OBJECT_PATH;
        }
    }

    public class PropertiesImpl implements Properties {
        @Override
        @SuppressWarnings("unchecked")
        public <A> A Get(String interfaceName, String propertyName) {
            return (A) switch (propertyName) {
                // MediaPlayer2 properties
                case "CanQuit" -> true;
                case "CanRaise" -> false;
                case "HasTrackList" -> false;
                case "Identity" -> "muplayer";
                case "DesktopEntry" -> "muplayer";
                case "SupportedUriSchemes" -> new String[]{"file"};
                case "SupportedMimeTypes" -> new String[]{"audio/mpeg", "audio/flac", "audio/ogg", "audio/x-wav"};

                // Player properties
                case "PlaybackStatus" -> callback != null ? callback.getPlaybackStatus() : "Stopped";
                case "Rate" -> 1.0;
                case "Metadata" -> callback != null ? callback.getMetadata() : new HashMap<>();
                case "Volume" -> callback != null ? callback.getVolume() : 1.0;
                case "Position" -> callback != null ? callback.getPosition() : 0L;
                case "MinimumRate" -> 1.0;
                case "MaximumRate" -> 1.0;
                case "CanGoNext" -> true;
                case "CanGoPrevious" -> true;
                case "CanPlay" -> true;
                case "CanPause" -> true;
                case "CanSeek" -> true;
                case "CanControl" -> true;

                default -> null;
            };
        }

        @Override
        public <A> void Set(String interfaceName, String propertyName, A value) {
            if ("Volume".equals(propertyName) && callback != null) {
                callback.setVolume((Double) value);
            }
        }

        @Override
        public Map<String, Variant<?>> GetAll(String interfaceName) {
            Map<String, Variant<?>> props = new HashMap<>();

            if ("org.mpris.MediaPlayer2".equals(interfaceName)) {
                props.put("CanQuit", new Variant<>(true));
                props.put("CanRaise", new Variant<>(false));
                props.put("HasTrackList", new Variant<>(false));
                props.put("Identity", new Variant<>("muplayer"));
                props.put("SupportedUriSchemes", new Variant<>(new String[]{"file"}));
                props.put("SupportedMimeTypes", new Variant<>(new String[]{"audio/mpeg", "audio/flac", "audio/ogg"}));
            } else if ("org.mpris.MediaPlayer2.Player".equals(interfaceName)) {
                props.put("PlaybackStatus", new Variant<>(callback != null ? callback.getPlaybackStatus() : "Stopped"));
                props.put("Rate", new Variant<>(1.0));
                props.put("Volume", new Variant<>(callback != null ? callback.getVolume() : 1.0));
                props.put("Position", new Variant<>(callback != null ? callback.getPosition() : 0L));
                props.put("CanGoNext", new Variant<>(true));
                props.put("CanGoPrevious", new Variant<>(true));
                props.put("CanPlay", new Variant<>(true));
                props.put("CanPause", new Variant<>(true));
                props.put("CanSeek", new Variant<>(true));
                props.put("CanControl", new Variant<>(true));

                if (callback != null) {
                    props.put("Metadata", new Variant<>(callback.getMetadata(), "a{sv}"));
                }
            }

            return props;
        }

        @Override
        public String getObjectPath() {
            return OBJECT_PATH;
        }
    }

    public void setCallback(PlayerCallback callback) {
        this.callback = callback;
    }

    public void register() throws DBusException {
        connection = DBusConnectionBuilder.forSessionBus().build();

        mediaPlayer2 = new MediaPlayer2Impl();
        player = new PlayerImpl();
        properties = new PropertiesImpl();

        connection.requestBusName(BUS_NAME);
        connection.exportObject(mediaPlayer2);
        connection.exportObject(player);
        connection.exportObject(properties);

        System.out.println("MPRIS service registered: " + BUS_NAME);
        System.out.println("Control with: playerctl --player=muplayer <command>");
    }

    public void unregister() {
        if (connection != null) {
            try {
                connection.releaseBusName(BUS_NAME);
                connection.close();
            } catch (Exception ignored) {}
        }
    }

    // Helper to create metadata map
    public static Map<String, Variant<?>> createMetadata(String trackId, String title, String artist, String album, long lengthMicros) {
        Map<String, Variant<?>> metadata = new HashMap<>();
        metadata.put("mpris:trackid", new Variant<>(trackId));
        metadata.put("mpris:length", new Variant<>(lengthMicros));
        metadata.put("xesam:title", new Variant<>(title));
        metadata.put("xesam:artist", new Variant<>(new String[]{artist}));
        metadata.put("xesam:album", new Variant<>(album));
        return metadata;
    }

}