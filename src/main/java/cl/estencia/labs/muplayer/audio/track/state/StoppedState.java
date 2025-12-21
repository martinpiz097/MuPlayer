package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoppedState extends TrackState {

    public StoppedState(Track track) {
        super(TrackStateName.STOPPED, track);
    }

    @Override
    public void handle() {
        synchronized (track) {
           try {
               track.resetStream();
               trackStatusData.setSecsSeeked(0);
               track.wait();
           } catch (Exception e) {
               log.error(e.getMessage(), e);
               track.finish();
           }
       }
    }
}