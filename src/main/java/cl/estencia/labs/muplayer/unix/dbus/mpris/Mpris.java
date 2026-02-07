package cl.estencia.labs.muplayer.unix.dbus.mpris;

import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtension;
import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import cl.estencia.labs.muplayer.audio.player.EventPlayer;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.util.CoverUtil;
import cl.estencia.labs.muplayer.core.bus.message.Events;
import cl.estencia.labs.muplayer.core.bus.model.PlayerInfo;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.LoopStatus;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.PlaybackStatusEnum;
import cl.estencia.labs.muplayer.unix.dbus.mpris.interfaces.MediaPlayer2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.dbus.interfaces.Properties;
import org.freedesktop.dbus.types.Variant;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cl.estencia.labs.muplayer.audio.common.constants.PlayerConstants.PLAYER_BUS;
import static cl.estencia.labs.muplayer.core.bus.message.PlayerEventTopics.SHUTDOWN;
import static cl.estencia.labs.muplayer.core.cache.CacheManager.CACHE;
import static cl.estencia.labs.muplayer.core.cache.CacheVar.PLAYER;
import static cl.estencia.labs.muplayer.core.util.CollectionUtil.newMap;
import static cl.estencia.labs.muplayer.core.util.NumberUtil.microSecsToSeconds;
import static cl.estencia.labs.muplayer.core.util.NumberUtil.secondsToMicroSecs;
import static cl.estencia.labs.muplayer.unix.dbus.mpris.common.MprisConstants.*;
import static cl.estencia.labs.muplayer.unix.dbus.mpris.common.MprisPropertyName.Metadata;
import static cl.estencia.labs.muplayer.unix.dbus.mpris.common.MprisPropertyName.PlaybackStatus;
import static org.jaudiotagger.tag.FieldKey.BPM;
import static org.jaudiotagger.tag.FieldKey.DISC_NO;

@Slf4j
public class Mpris implements MediaPlayer2, MediaPlayer2.Player, Properties {
    @Getter private final MprisConnection connection;
    @Getter private final MprisPublisher publisher;
    private final AtomicReference<PlayerInfo> playerInfoRef;
    private final AtomicReference<String> trackIdRef;
    private final AtomicReference<Map<String, Variant<?>>> metadataRef;
    private final AtomicReference<PlaybackStatusEnum> playbackStatusRef;
    private final AtomicReference<LoopStatus> loopStatusRef;

    public Mpris(String busName) {
        this.connection = new MprisConnection(BUS_NAME_HEADER + busName, this);
        this.publisher = new MprisPublisher(connection);
        this.playerInfoRef = new AtomicReference<>(null);
        this.trackIdRef = new AtomicReference<>(UNKNOWN_TRACK_ID);
        this.metadataRef = new AtomicReference<>(newMap());
        this.playbackStatusRef = new AtomicReference<>(PlaybackStatusEnum.Stopped);
        this.loopStatusRef = new AtomicReference<>(LoopStatus.None);
    }

    private void configureListeners() {
        EventPlayer eventPlayer = CACHE.get(PLAYER, EventPlayer.class);
        if (eventPlayer == null) {
            log.warn("No player available in cache, mpris listeners not created!");
            return;
        }

        eventPlayer.addResponseListener(playerInfoResp -> {
            log.debug("track changed! sending changes to mpris...");
            synchronized (playerInfoRef) {
                playerInfoRef.set(playerInfoResp);
                trackIdRef.set(buildTrackId(playerInfoRef.get().getCurrentTrack()));
                setMetadata(buildCurrentTrackMetadata());
            }

            sendMetadataChanges();
            setPlaybackStatus(PlaybackStatusEnum.Playing);
            log.debug("track changed! changes sent to mpris!");
        });

        eventPlayer.addListener(SHUTDOWN, message -> {
            CACHE.clear();
            System.exit(0);
        });
    }

    // TODO transformar metadata en un objeto que gestione internamente el Map
    private Map<String, Variant<?>> buildCurrentTrackMetadata() {
        Track current = getCurrentTrack();
        if (current == null) {
            return Map.of();
        }

        Map<String, Variant<?>> metadata = CollectionUtil.newMap();

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

        return metadata;
    }

    private String buildTrackId(Track track) {
        if (track == null || track.getDataSource() == null) {
            return UNKNOWN_TRACK_ID;
        }

        return TRACK_ID_HEADER + track.getTrackId();
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

    // funcion necesaria porque un valor dentro de un objeto Variant no puede ser null
    private void addValueNullSafe(Map<String, Variant<?>> metadata,
                                  String key, Object value) {
        String valueStr = value != null ? value.toString().trim() : null;
        if (valueStr == null || valueStr.isEmpty()) {
            return;
        }

        metadata.put(key, new Variant<>(value));
    }

    private void addValueNullSafe(Map<String, Variant<?>> metadata,
                                  String key, Object value, String sig) {
        String valueStr = value != null ? value.toString().trim() : null;
        if (valueStr == null || valueStr.isEmpty()) {
            return;
        }

        metadata.put(key, new Variant<>(value, sig));
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
        props.put("SupportedUriSchemes", new Variant<>(new String[] {"file"}));
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
        double rate = 1.0f;
        double volume = playerStatusData != null
                ? playerStatusData.getVolume()
                : 100;
        long position = currentTrack != null
                ? secondsToMicroSecs(currentTrack.getProgress())
                : 0;

        Map<String, Variant<?>> props = new HashMap<>();
        props.put("PlaybackStatus", new Variant<>(playbackStatusRef.get().name()));
        props.put("LoopStatus", new Variant<>(loopStatusRef.get().name()));
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

    public void setPlaybackStatus(PlaybackStatusEnum status) {
        if (playbackStatusRef.get().equals(status)) {
            return;
        }

        playbackStatusRef.set(status);
        publisher.sendPropertiesChangedEvent(PlaybackStatus, status.name());
    }

    public void sendMetadataChanges() {
        Map<String, Variant<?>> metadata = metadataRef.get();
        if (metadata == null) {
            metadata = Map.of();
        }

        log.debug("Updating metadata...");
        publisher.sendPropertiesChangedEvent(Metadata, metadata, "a{sv}");
        log.debug("Metadata updated successfully!");
    }

    public void setMetadata(Map<String, Variant<?>> metadata) {
        metadataRef.set(metadata);
    }

    public Map<String, Variant<?>> getMetadata() {
        return metadataRef.get();
    }

    public PlaybackStatusEnum getPlaybackStatus() {
        return playbackStatusRef.get();
    }

    public LoopStatus getLoopStatus() {
        return loopStatusRef.get();
    }

    public void setVolume(double vol) {
        PLAYER_BUS.publish(Events.setVolume((float) vol));
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

    public void shutdown() {
        publisher.shutdown();
        connection.close();
//        interrupt();
//        interruptor.switchOn();
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
        PLAYER_BUS.publish(Events.shutdown());
    }

    @Override
    public void play() {
        log.debug("[MPRIS] Play");
        PLAYER_BUS.publish(Events.play());
    }

    @Override
    public void pause() {
        log.debug("[MPRIS] Pause");
        PLAYER_BUS.publish(Events.pause());
    }

    @Override
    public void playPause() {
        if (PlaybackStatusEnum.Playing.equals(playbackStatusRef.get())) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void stop() {
        log.debug("[MPRIS] Stop");
        PLAYER_BUS.publish(Events.stop());
    }

    @Override
    public void next() {
        log.debug("[MPRIS] Next");
        PLAYER_BUS.publish(Events.playNext());
    }

    @Override
    public void previous() {
        log.debug("[MPRIS] Previous");
        PLAYER_BUS.publish(Events.playPrev());
    }

    @Override
    public void seek(long offset) {
        final long offsetInSeconds = microSecsToSeconds(offset);
        PLAYER_BUS.publish(Events.seekSeconds((int) offsetInSeconds));

        log.debug("[MPRIS] Seek to: " + offsetInSeconds + "μs");
    }

    @Override
    public void setPosition(String incomingTrackId, long position) {
        log.debug("[MPRIS] SetPosition: " + position + "μs");

        final long positionInSeconds = microSecsToSeconds(position);
        PLAYER_BUS.publish(Events.gotoSeconds((int) positionInSeconds));
    }

    @Override
    public void openUri(String uri) {
        log.debug("[MPRIS] OpenUri: " + uri);
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

    public void start() {
        try {
            configureListeners();
            connection.open();
            publisher.start();
        } catch (Exception e) {
            log.error("Error of type " + e.getClass().getSimpleName()
                    + " and message " + e.getMessage()
                    + " when trying to start Mpris server");
        }
    }

}