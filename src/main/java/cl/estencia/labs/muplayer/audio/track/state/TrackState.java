package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.aucom.audio.device.Speaker;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackStatusData;
import cl.estencia.labs.muplayer.audio.track.TrackIOUtil;
import cl.estencia.labs.muplayer.audio.track.listener.TrackEvent;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;
import cl.estencia.labs.muplayer.audio.track.listener.TrackStateListener;
import lombok.Getter;

import javax.sound.sampled.AudioInputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class TrackState {
    protected final Player player;
    protected final Track track;
    protected final TrackStatusData trackData;
    protected final TrackIOUtil trackIOUtil;
    protected final Speaker speaker;
    protected final AudioInputStream decodedAudioStream;

    protected final TrackNotifier notifier;

    @Getter protected final TrackStateName name;

    public TrackState(Player player, Track track,
                      TrackStateName name, TrackNotifier notifier) {
        this.player = player;
        this.track = track;
        this.trackData = track.getTrackStatusData();
        this.trackIOUtil = track.getTrackIOUtil();
        this.speaker = track.getSpeaker();
        this.decodedAudioStream = track.getAudioDecoder().getDecodedStream();
        this.notifier = notifier;
        this.name = name;
    }


    protected abstract void handle();

    public void execute() {
        CompletableFuture.runAsync(() ->
                notifier.addEvent(new TrackEvent(track, getName())));
        handle();
    }
}