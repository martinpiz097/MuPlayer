package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.aucom.core.device.output.Speaker;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.audio.track.TrackStatusData;
import cl.estencia.labs.muplayer.event.model.TrackEvent;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;
import lombok.Getter;

import javax.sound.sampled.AudioInputStream;

public abstract class TrackState {
    @Getter protected final TrackStateName name;
    protected final Track track;
    protected final TrackInternalEventNotifier internalEventNotifier;
    protected final TrackUserEventNotifier userEventNotifier;
    protected final TrackStatusData trackStatusData;
    protected final TrackIOUtil trackIOUtil;
    protected final Speaker speaker;
    protected final AudioInputStream decodedAudioStream;

    public TrackState(TrackStateName name, Track track,
                      TrackInternalEventNotifier internalEventNotifier,
                      TrackUserEventNotifier userEventNotifier) {
        this.name = name;
        this.track = track;
        this.internalEventNotifier = internalEventNotifier;
        this.userEventNotifier = userEventNotifier;
        this.trackStatusData = track.getTrackStatusData();
        this.trackIOUtil = track.getTrackIOUtil();
        this.speaker = track.getSpeaker();
        this.decodedAudioStream = track.getAudioDecoder().getDecodedStream();
    }

    protected void sendStateEvent() {
        TrackEvent trackEvent = new TrackEvent(track, getName());
        internalEventNotifier.sendEvent(trackEvent);
        userEventNotifier.sendEvent(trackEvent);
    }

    public abstract void handle();

}