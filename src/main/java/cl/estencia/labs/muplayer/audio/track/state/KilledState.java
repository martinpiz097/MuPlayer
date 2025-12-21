package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;

public class KilledState extends TrackState {
    public KilledState(Track track) {
        super(TrackStateName.KILLED, track);
    }

    @Override
    public void handle() {
        try {
            trackIOUtil.closeStream(decodedAudioStream);
            speaker.close();
            trackStatusData.setCanTrackContinue(false);

//            track.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
