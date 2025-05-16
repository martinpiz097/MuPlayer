package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.listener.notifier.TrackEventNotifier;
import lombok.extern.java.Log;

import java.util.logging.Level;

@Log
public class StoppedState extends TrackState {

    public StoppedState(Track track, TrackEventNotifier notifier) {
        super(track, TrackStateName.STOPPED, notifier);
    }

    @Override
    protected void handle() {
       synchronized (track) {
           try {
               track.resetStream();
               trackData.setSecsSeeked(0);
               track.wait();
           } catch (Exception e) {
               log.log(Level.SEVERE, e.getMessage());
               track.finish();
           }
       }
    }
}