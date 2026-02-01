package cl.estencia.labs.muplayer.unix.dbus.mpris;

import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.ebot.utils.threads.Interruptor;
import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtension;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.player.EventPlayer;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.util.CoverUtil;
import cl.estencia.labs.muplayer.core.bus.message.Events;
import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.PlaybackStatus;
import cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces.MediaPlayer2;
import cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces.Seeked;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.*;
import static org.jaudiotagger.tag.FieldKey.*;

@Slf4j
public class Mpris extends Thread
        implements MediaPlayer2, MediaPlayer2.Player, Properties {
    @Getter private final MprisConnection connection;
    private final MessageBus playerBus;
    private final AtomicReference<PlayerInfo> playerInfoRef;
    private final AtomicReference<String> trackIdRef;
    private volatile EventPlayer eventPlayer;
    private final Interruptor interruptor;

    private PlaybackStatus playbackStatus = PlaybackStatus.Stopped;
    private long position = 0;
    private final boolean shuffle = false;
    private final String loopStatus = "None";

    private static final String BUS_NAME = "org.mpris.MediaPlayer2.muplayer";
    private static final String DBUS_OBJECT_PATH = "/org/mpris/MediaPlayer2";
    private static final String DBUS_PLAYER_INTERFACE = "org.mpris.MediaPlayer2.Player";
    private static final String UNKNOWN_TRACK_ID = "/com/muplayer/track/-1";

    public Mpris() throws DBusException {
        this.connection = new MprisConnection(BUS_NAME);
        this.playerBus = CACHE.get(PLAYER_BUS);
        this.playerInfoRef = new AtomicReference<>(null);
        this.trackIdRef = new AtomicReference<>(UNKNOWN_TRACK_ID);
        this.interruptor = Interruptor.manual(this);
    }

    private void configureBusListeners() {
        eventPlayer = CACHE.get(PLAYER, EventPlayer.class);
        if (eventPlayer == null) {
            return;
        }

        eventPlayer.addResponseListener(playerInfoResp -> {
            synchronized (playerInfoRef) {
                playerInfoRef.set(playerInfoResp);
                trackIdRef.set(buildTrackId(playerInfoRef.get().getCurrentTrack()));
                CACHE.set(PLAYER_CURRENT_DATA, playerInfoRef.get());
            }

            emitPropertiesChanged(Map.of("Metadata", new Variant<>(buildMetadata(), "a{sv}")));
            setPlaybackStatus(PlaybackStatus.Playing);
        });
    }

    private String buildTrackId(Track track) {
        if (track == null || track.getDataSource() == null) {
            return UNKNOWN_TRACK_ID;
        }

        return "/com/muplayer/track" + track.getDataSource().getPath();
    }

    private Track getCurrentTrack() {
        PlayerInfo playerInfo = playerInfoRef.get();
        if (playerInfo == null) {
            return null;
        }

        return playerInfo.getCurrentTrack();
    }

    private PlayerStatusData getStatusData() {
        PlayerInfo playerInfo = playerInfoRef.get();
        if (playerInfo == null) {
            return null;
        }

        return playerInfo.getPlayerStatusData();
    }

    @Override
    public String getObjectPath() {
        return DBUS_OBJECT_PATH;
    }

    @Override
    public void raise() {
        log.debug("[MPRIS] Raise");
    }

    @Override
    public void quit() {
        log.debug("[MPRIS] Quit");
        playerBus.publish(Events.shutdown());
        CACHE.clear();
        connection.close();

        System.exit(0);
    }

    @Override
    public void play() {
        log.debug("[MPRIS] Play");
        playerBus.publish(Events.play());
        setPlaybackStatus(PlaybackStatus.Playing);
    }

    @Override
    public void pause() {
        log.debug("[MPRIS] Pause");
        playerBus.publish(Events.pause());
        setPlaybackStatus(PlaybackStatus.Paused);
    }

    @Override
    public void playPause() {
        if (PlaybackStatus.Playing.equals(playbackStatus)) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void Stop() {
        log.debug("[MPRIS] Stop");
        setPlaybackStatus(PlaybackStatus.Stopped);
        position = 0;
    }

    @Override
    public void next() {
        log.debug("[MPRIS] Next");
        playerBus.publish(Events.playNext());
    }

    @Override
    public void previous() {
        log.debug("[MPRIS] Previous");
        playerBus.publish(Events.playPrev());
    }

    @Override
    public void seek(long offset) {
        Track currentTrack = getCurrentTrack();
        if (currentTrack == null) {
            return;
        }

        position = Math.max(0, Math.min(position + offset, currentTrack.getDuration()));
        log.debug("[MPRIS] Seek to: " + position + "μs");
        emitSeeked(position);
    }

    @Override
    public void setPosition(String incomingTrackId, long newPosition) {
        log.debug("[MPRIS] SetPosition: " + position + "μs");

//        if (trackIdRef.get().equals(incomingTrackId)) {
//            position = Math.max(0, Math.min(newPosition, trackLength));
//            log.debug("[MPRIS] SetPosition: " + position + "μs");
//            emitSeeked(position);
//        }
    }

    @Override
    public void openUri(String uri) {
        log.debug("[MPRIS] OpenUri: " + uri);
    }

    // ============================================================
    // Métodos internos para emitir señales (minúscula)
    // ============================================================

    private void emitPropertiesChanged(Map<String, Variant<?>> changed) {
        try {
            connection.getDbus().sendMessage(new PropertiesChanged(
                    DBUS_OBJECT_PATH,
                    DBUS_PLAYER_INTERFACE,
                    changed,
                    List.of()
            ));
        } catch (DBusException e) {
            log.error("Error emitiendo PropertiesChanged: " + e.getMessage());
        }
    }

    private void emitSeeked(long newPosition) {
        try {
            connection.getDbus().sendMessage(new Seeked(DBUS_OBJECT_PATH, newPosition));
        } catch (DBusException e) {
            log.error("Error emitiendo Seeked: " + e.getMessage());
        }
    }

    // ============================================================
    // Setters públicos (minúscula) - emiten señales
    // ============================================================

    public void setPlaybackStatus(PlaybackStatus status) {
        if (!this.playbackStatus.equals(status)) {
            this.playbackStatus = status;
            emitPropertiesChanged(Map.of("PlaybackStatus", new Variant<>(status)));
        }
    }

    public void setVolume(double vol) {
        float newVolume = Math.min(100, (float) Math.max(0d, vol));
        eventPlayer.sendEvent(Events.setVolume(newVolume));
        emitPropertiesChanged(Map.of("Volume", new Variant<>(newVolume)));
    }

    public void setShuffle(boolean shuffle) {
//        if (this.shuffle != shuffle) {
//            this.shuffle = shuffle;
//            emitPropertiesChanged(Map.of("Shuffle", new Variant<>(shuffle)));
//        }
    }

    public void setLoopStatus(String status) {
//        if (!this.loopStatus.equals(status)) {
//            this.loopStatus = status;
//            emitPropertiesChanged(Map.of("LoopStatus", new Variant<>(status)));
//        }
    }

    // ============================================================
    // Properties interface (PascalCase requerido por D-Bus)
    // ============================================================

    @Override
    @SuppressWarnings("unchecked")
    public <A> A Get(String iface, String property) {
        return (A) switch (iface) {
            case "org.mpris.MediaPlayer2" -> getMediaPlayer2Property(property);
            case DBUS_PLAYER_INTERFACE -> getPlayerProperty(property);
            default -> null;
        };
    }

    @Override
    public Map<String, Variant<?>> GetAll(String iface) {
        return switch (iface) {
            case "org.mpris.MediaPlayer2" -> getAllMediaPlayer2Properties();
            case DBUS_PLAYER_INTERFACE -> getAllPlayerProperties();
            default -> Map.of();
        };
    }

    @Override
    public <A> void Set(String iface, String property, A value) {
        if (DBUS_PLAYER_INTERFACE.equals(iface)) {
            switch (property) {
                case "Volume" -> setVolume(((Number) value).doubleValue());
                case "LoopStatus" -> setLoopStatus((String) value);
                case "Shuffle" -> setShuffle((Boolean) value);
            }
        }
    }

    // ============================================================
    // Métodos auxiliares privados (minúscula)
    // ============================================================

    private Object getMediaPlayer2Property(String name) {
        return switch (name) {
            case "CanQuit" -> true;
            case "CanRaise" -> true;
            case "HasTrackList" -> false;
            case "Identity" -> "Muplayer";
            case "DesktopEntry" -> "muplayer";
            case "SupportedUriSchemes" -> List.of("file");
            case "SupportedMimeTypes" -> List.of("audio/mpeg", "audio/flac", "audio/ogg", "audio/wav");
            default -> null;
        };
    }

    private Object getPlayerProperty(String name) {
        return switch (name) {
            case "PlaybackStatus" -> playbackStatus;
            case "LoopStatus" -> loopStatus;
            case "Rate" -> 1.0;
            case "MinimumRate" -> 1.0;
            case "MaximumRate" -> 1.0;
            case "Shuffle" -> shuffle;
            case "Volume" -> playerInfoRef.get() != null
                    ? playerInfoRef.get().getPlayerStatusData().getVolume()
                    : 100;
            case "Position" -> position;
            case "CanGoNext" -> true;
            case "CanGoPrevious" -> true;
            case "CanPlay" -> true;
            case "CanPause" -> true;
            case "CanSeek" -> true;
            case "CanControl" -> true;
            case "Metadata" -> buildMetadata();
            default -> null;
        };
    }

    private void addOptionalValue(Map<String, Variant<?>> metadata,
                                  String key, Object value) {
        String valueStr = value != null ? value.toString().trim() : null;
        if (valueStr == null || valueStr.isEmpty()) {
            return;
        }

        metadata.put(key, new Variant<>(value));
    }

    // cuando hay un error con los metadatos, ya sea por temas de tipo de dato
    // o key desconocida, no se enviara nada a mpris
    private Map<String, Variant<?>> buildMetadata() {
        Track current = getCurrentTrack();
        if (current == null) {
            return Map.of();
        }

        Map<String, Variant<?>> metadata = new HashMap<>();
        metadata.put("mpris:trackid", new Variant<>(trackIdRef.get()));
        metadata.put("mpris:length", new Variant<>(current.getDuration()));
        metadata.put("xesam:title", new Variant<>(current.getTitle()));
        metadata.put("xesam:album", new Variant<>(current.getAlbum()));
        metadata.put("xesam:artist", new Variant<>(current.getArtist()));

        addOptionalValue(metadata, "xesam:audioBPM", current.getPropertyAsInt(BPM));
        addOptionalValue(metadata, "xesam:contentCreated", current.getYear());
        addOptionalValue(metadata, "xesam:discNumber", current.getPropertyAsInt(DISC_NO));
        addOptionalValue(metadata, "xesam:genre", current.getGenre());
        addOptionalValue(metadata, "xesam:url", "file://" + current.getDataSource().getPath());
        addOptionalValue(metadata, "xesam:artUrl", CoverUtil.createTempUri(current.getCover()));

        return metadata;
    }

    private Map<String, Variant<?>> getAllMediaPlayer2Properties() {
        Map<String, Variant<?>> props = new HashMap<>();
        props.put("CanQuit", new Variant<>(true));
        props.put("CanRaise", new Variant<>(true));
        props.put("HasTrackList", new Variant<>(false));
        props.put("Identity", new Variant<>("MuPlayer"));
        props.put("DesktopEntry", new Variant<>("muplayer"));
        props.put("SupportedUriSchemes", new Variant<>(List.of("file")));
        props.put("SupportedMimeTypes", new Variant<>(SupportedAudioExtension.getMimeTypes()));
        return props;
    }

    private Map<String, Variant<?>> getAllPlayerProperties() {
        PlayerInfo playerInfo = playerInfoRef.get();

        Map<String, Variant<?>> props = new HashMap<>();
        props.put("PlaybackStatus", new Variant<>(playbackStatus));
        props.put("LoopStatus", new Variant<>(loopStatus));
        props.put("Rate", new Variant<>(1.0));
        props.put("Shuffle", new Variant<>(shuffle));
        props.put("Volume", new Variant<>(playerInfo != null
                ? playerInfo.getPlayerStatusData().getVolume() : 100));
        props.put("Position", new Variant<>(position));
        props.put("Metadata", new Variant<>(buildMetadata(), "a{sv}"));
        props.put("CanGoNext", new Variant<>(true));
        props.put("CanGoPrevious", new Variant<>(true));
        props.put("CanPlay", new Variant<>(true));
        props.put("CanPause", new Variant<>(true));
        props.put("CanSeek", new Variant<>(true));
        props.put("CanControl", new Variant<>(true));
        return props;
    }

    public void shutdown() {
        interrupt();
        interruptor.switchOn();
    }

    @Override
    public void run() {
        try {
            log.trace("Mpris server started!");
            configureBusListeners();
            connection.start(this);
            while (!Thread.currentThread().isInterrupted()) {
                interruptor.checkSignal();
            }

        } catch (DBusException e) {
            log.error("Error of type " + e.getClass().getSimpleName()
                    + " and message " + e.getMessage()
                    + " when trying to start Mpris server");
        }
    }

}
