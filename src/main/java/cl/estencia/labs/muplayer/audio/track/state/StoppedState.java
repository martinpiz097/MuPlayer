package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
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
               log.log(Level.SEVERE, e.getMessage());
               track.finish();
           }
       }
    }
}