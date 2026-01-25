package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.core.aucom.device.output.Speaker;
import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.model.TrackStatusData;
import cl.estencia.labs.muplayer.core.bus.util.MessageBusUtil;
import lombok.Getter;

import javax.sound.sampled.AudioInputStream;

public abstract class TrackState {
    @Getter protected final TrackStateName name;
    protected final Track track;
    protected final TrackStatusData trackStatusData;
    protected final Speaker speaker;
    protected final AudioInputStream decodedAudioStream;
    protected final MessageBus messageBus;

    public TrackState(TrackStateName name, Track track) {
        this.name = name;
        this.track = track;
        this.trackStatusData = track.getTrackStatusData();
        this.speaker = track.getSpeaker();
        this.decodedAudioStream = track.getAudioDecoder().getDecodedAudioStream();
        this.messageBus = MessageBusUtil.getMessageBus();
    }

    public abstract void handle();

}