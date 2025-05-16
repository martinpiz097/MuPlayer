package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.aucom.audio.device.Speaker;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.audio.track.TrackStatusData;
import cl.estencia.labs.muplayer.listener.event.TrackEvent;
import cl.estencia.labs.muplayer.listener.notifier.TrackEventNotifier;
import lombok.Getter;

import javax.sound.sampled.AudioInputStream;

public abstract class TrackState {
    protected final Track track;
    protected final TrackStatusData trackData;
    protected final TrackIOUtil trackIOUtil;
    protected final Speaker speaker;
    protected final AudioInputStream decodedAudioStream;

    protected final TrackEventNotifier notifier;

    @Getter protected final TrackStateName name;

    public TrackState(Track track,
                      TrackStateName name, TrackEventNotifier notifier) {
        this.track = track;
        this.trackData = track.getTrackStatusData();
        this.trackIOUtil = track.getTrackIOUtil();
        this.speaker = track.getSpeaker();
        this.decodedAudioStream = track.getAudioDecoder().getDecodedStream();
        this.notifier = notifier;
        this.name = name;
    }

    protected TrackEvent createTrackEvent() {
        return new TrackEvent(track, getName());
    }

    protected abstract void handle();

    public void execute() {
        notifier.sendEvent(createTrackEvent());
        handle();
    }

}