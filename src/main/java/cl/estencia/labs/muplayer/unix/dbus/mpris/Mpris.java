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
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.LoopStatus;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.PlaybackStatus;
import cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces.MediaPlayer2;
import cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces.Seeked;
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
import static cl.estencia.labs.muplayer.core.util.CollectionUtil.newMap;
import static cl.estencia.labs.muplayer.core.util.NumberUtil.microSecsToSeconds;
import static cl.estencia.labs.muplayer.core.util.NumberUtil.secondsToMicroSecs;
import static cl.estencia.labs.muplayer.unix.dbus.mpris.common.MprisConstants.*;
import static org.jaudiotagger.tag.FieldKey.*;

@Slf4j
public class Mpris extends Thread
        implements MediaPlayer2, MediaPlayer2.Player, Properties {
    private final MprisConnection connection;
    private final MessageBus playerBus;
    private final AtomicReference<PlayerInfo> playerInfoRef;
    private final AtomicReference<String> trackIdRef;
    private final AtomicReference<Map<String, Variant<?>>> metadataRef;
    private final Interruptor interruptor;
    private volatile PlaybackStatus playbackStatus;
    private volatile LoopStatus loopStatus;

    public Mpris() throws DBusException {
        this.connection = new MprisConnection(BUS_NAME);
        this.playerBus = CACHE.get(PLAYER_BUS);
        this.playerInfoRef = new AtomicReference<>(null);
        this.trackIdRef = new AtomicReference<>(UNKNOWN_TRACK_ID);
        this.metadataRef = new AtomicReference<>(newMap());
        this.interruptor = Interruptor.manual(this);
        this.playbackStatus = PlaybackStatus.Stopped;
        this.loopStatus = LoopStatus.None;
    }

    private void configureBusListeners() {
        EventPlayer eventPlayer = CACHE.get(PLAYER, EventPlayer.class);
        if (eventPlayer == null) {
            return;
        }

        eventPlayer.addResponseListener(playerInfoResp -> {
            synchronized (playerInfoRef) {
                playerInfoRef.set(playerInfoResp);
                trackIdRef.set(buildTrackId(playerInfoRef.get().getCurrentTrack()));
                buildCurrentTrackMetadata();
            }

            emitPropertiesChanged(Map.of(
                    "Metadata", new Variant<>(metadataRef.get(), "a{sv}")));
            setPlaybackStatus(PlaybackStatus.Playing);
        });
    }

    private void buildCurrentTrackMetadata() {
        Track current = getCurrentTrack();
        if (current == null) {
            return;
        }

        synchronized (metadataRef) {
            Map<String, Variant<?>> metadata = metadataRef.get();
            if (!metadata.isEmpty()) {
                metadata.clear();
            }

            addValueNullSafe(metadata, "mpris:trackid", trackIdRef.get());
            addValueNullSafe(metadata, "mpris:length", secondsToMicroSecs(current.getDuration()));
            addValueNullSafe(metadata, "xesam:title", current.getTitle());
            addValueNullSafe(metadata, "xesam:album", current.getAlbum());
            addValueNullSafe(metadata, "xesam:artist", current.getArtist());
            addValueNullSafe(metadata, "xesam:audioBPM", current.getPropertyAsInt(BPM));
            addValueNullSafe(metadata, "xesam:contentCreated", current.getYear());
            addValueNullSafe(metadata, "xesam:discNumber", current.getPropertyAsInt(DISC_NO));
            addValueNullSafe(metadata, "xesam:genre", current.getGenre());
            addValueNullSafe(metadata, "xesam:url", "file://" + current.getDataSource().getPath());
            addValueNullSafe(metadata, "xesam:artUrl", CoverUtil.createTempUri(current.getCover()));
//            addValueNullSafe(metadata, "xesam:asText", "the track lyrics");
        }
    }

    private String buildTrackId(Track track) {
        if (track == null || track.getDataSource() == null) {
            return UNKNOWN_TRACK_ID;
        }

        return TRACK_ID_HEADER + track.getDataSource().getPath();
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

        final int offsetInSeconds = microSecsToSeconds(offset);
        currentTrack.seek(offsetInSeconds);

        // por si llega a cambiar
        currentTrack = getCurrentTrack();
        if (currentTrack == null) {
            return;
        }

        int progressInMicros = secondsToMicroSecs(currentTrack.getProgress());
        log.debug("[MPRIS] Seek to: " + progressInMicros + "μs");
        emitSeeked(progressInMicros);
    }

    @Override
    public void setPosition(String incomingTrackId, long position) {
        log.debug("[MPRIS] SetPosition: " + position + "μs");
        Track currentTrack = getCurrentTrack();
        if (currentTrack == null) {
            return;
        }

        final int positionInSeconds = Math.round( (((float) position) / Math.powExact(10, 6)));
        currentTrack.gotoSecond(positionInSeconds);
    }

    @Override
    public void openUri(String uri) {
        log.debug("[MPRIS] OpenUri: " + uri);
    }

    private void emitPropertiesChanged(Map<String, Variant<?>> propertiesChanged) {
        try {
            connection.getDbus().sendMessage(new PropertiesChanged(
                    DBUS_OBJECT_PATH,
                    DBUS_PLAYER_INTERFACE,
                    propertiesChanged,
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

    public void setPlaybackStatus(PlaybackStatus status) {
        if (this.playbackStatus.equals(status)) {
            return;
        }

        this.playbackStatus = status;
        emitPropertiesChanged(Map.of("PlaybackStatus", new Variant<>(status)));
    }

    public void setVolume(double vol) {
        float newVolume = Math.min(100, (float) Math.max(0d, vol));
        playerBus.publish(Events.setVolume(newVolume));
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

    @Override
    @SuppressWarnings("unchecked")
    public <A> A Get(String iface, String property) {
        return (A) switch (iface) {
            case DBUS_MEDIA_PLAYER2_INTERFACE -> getMediaPlayer2Property(property);
            case DBUS_PLAYER_INTERFACE -> getPlayerProperty(property);
            default -> null;
        };
    }

    @Override
    public Map<String, Variant<?>> GetAll(String iface) {
        return switch (iface) {
            case DBUS_MEDIA_PLAYER2_INTERFACE -> getAllMediaPlayer2Properties();
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

    // funcion necesaria porque un valor dentro de un objeto Variant no puede ser null
    private void addValueNullSafe(Map<String, Variant<?>> metadata,
                                  String key, Object value) {
        String valueStr = value != null ? value.toString().trim() : null;
        if (valueStr == null || valueStr.isEmpty()) {
            return;
        }

        metadata.put(key, new Variant<>(value));
    }

    // cuando hay un error con los metadatos, ya sea por temas de tipo de dato
    // o key desconocida, no se enviara nada a mpris
    private Map<String, Variant<?>> getAllMediaPlayer2Properties() {
        Map<String, Variant<?>> props = new HashMap<>();
        props.put("CanQuit", new Variant<>(true));
        props.put("Fullscreen", new Variant<>(false));
        props.put("CanSetFullscreen", new Variant<>(false));
        props.put("CanRaise", new Variant<>(false));
        props.put("HasTrackList", new Variant<>(false));
        props.put("Identity", new Variant<>("MuPlayer"));
        props.put("DesktopEntry", new Variant<>("muplayer"));
        props.put("SupportedUriSchemes", new Variant<>(List.of("file")));
        props.put("SupportedMimeTypes", new Variant<>(SupportedAudioExtension.getMimeTypes()));
        return props;
    }

    private Object getMediaPlayer2Property(String name) {
        Map<String, Variant<?>> playerProperties = getAllMediaPlayer2Properties();
        Variant<?> variant = playerProperties.get(name);

        return variant != null ? variant.getValue() : null;
    }

    private Map<String, Variant<?>> getAllPlayerProperties() {
        Track currentTrack = getCurrentTrack();
        PlayerStatusData playerStatusData = getStatusData();
        float rate = 1.0f;
        float volume = playerStatusData != null
                ? playerStatusData.getVolume()
                : 100;
        int position = currentTrack != null
                ? secondsToMicroSecs(currentTrack.getProgress())
                : 0;

        Map<String, Variant<?>> props = new HashMap<>();
        props.put("PlaybackStatus", new Variant<>(playbackStatus.name()));
        props.put("LoopStatus", new Variant<>(loopStatus.name()));
        props.put("Rate", new Variant<>(rate));
        props.put("MaximumRate", new Variant<>(rate));
        props.put("MinimumRate", new Variant<>(rate));
        props.put("Shuffle", new Variant<>(false));
        props.put("Volume", new Variant<>(volume));
        props.put("Position", new Variant<>(position));
        props.put("Metadata", new Variant<>(metadataRef.get(), "a{sv}"));
        props.put("CanGoNext", new Variant<>(true));
        props.put("CanGoPrevious", new Variant<>(true));
        props.put("CanPlay", new Variant<>(true));
        props.put("CanPause", new Variant<>(true));
        props.put("CanSeek", new Variant<>(true));
        props.put("CanControl", new Variant<>(true));

        return props;
    }

    private Object getPlayerProperty(String name) {
        Map<String, Variant<?>> playerProperties = getAllPlayerProperties();
        Variant<?> variant = playerProperties.get(name);

        return variant != null ? variant.getValue() : null;
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
