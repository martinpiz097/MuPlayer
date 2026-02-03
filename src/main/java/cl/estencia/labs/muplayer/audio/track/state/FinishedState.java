package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.bus.message.Events;
import lombok.extern.slf4j.Slf4j;

import static cl.estencia.labs.muplayer.audio.common.constants.PlayerConstants.PLAYER_BUS;
import static cl.estencia.labs.muplayer.audio.util.AudioDriverUtil.closeStream;

@Slf4j
public class FinishedState extends TrackState {

    public FinishedState(Track track) {
        super(TrackStateName.FINISHED, track);
    }

    @Override
    public void handle() {
        try {
            closeStream(decodedAudioStream);
            speaker.close();
            trackStatusData.setCanTrackContinue(false);

            PLAYER_BUS.publish(Events.playNext());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
