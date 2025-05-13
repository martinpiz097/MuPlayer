package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.listener.TrackEvent;
import lombok.extern.java.Log;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;

import java.util.List;
import java.util.logging.Level;

@Log
public class StoppedState extends TrackState {

    public StoppedState(Player player, Track track, List<TrackEvent> listInternalEvents) {
        super(player, track, TrackStateName.STOPPED, listInternalEvents);
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
               track.kill();
           }
       }
    }
}