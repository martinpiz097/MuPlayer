package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.bus.message.Messages;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinishedState extends TrackState {

    public FinishedState(Track track) {
        super(TrackStateName.FINISHED, track);
    }

    @Override
    public void handle() {
        try {
            trackIOUtil.closeStream(decodedAudioStream);
            speaker.close();
            trackStatusData.setCanTrackContinue(false);

            messageBus.publish(Messages.playNext());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
