package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.util.AudioDriverUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KilledState extends TrackState {
    public KilledState(Track track) {
        super(TrackStateName.KILLED, track);
    }

    @Override
    public void handle() {
        try {
            AudioDriverUtil.closeStream(decodedAudioStream);
            speaker.close();
            trackStatusData.setCanTrackContinue(false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
