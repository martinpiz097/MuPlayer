package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.aucom.core.device.output.Speaker;
import cl.estencia.labs.ebot.bus.MessageBus;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackStatusData;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.bus.MuPlayerBusUtil;
import lombok.Getter;

import javax.sound.sampled.AudioInputStream;

public abstract class TrackState {
    @Getter protected final TrackStateName name;
    protected final Track track;
    protected final TrackStatusData trackStatusData;
    protected final TrackIOUtil trackIOUtil;
    protected final Speaker speaker;
    protected final AudioInputStream decodedAudioStream;
    protected final MessageBus messageBus;

    public TrackState(TrackStateName name, Track track) {
        this.name = name;
        this.track = track;
        this.trackStatusData = track.getTrackStatusData();
        this.trackIOUtil = track.getTrackIOUtil();
        this.speaker = track.getSpeaker();
        this.decodedAudioStream = track.getAudioDecoder().getDecodedStream();
        this.messageBus = MuPlayerBusUtil.getMessageBus();
    }

    public abstract void handle();

}