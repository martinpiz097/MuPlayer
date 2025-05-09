package cl.estencia.labs.muplayer.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.muplayer.audio.track.Track;

public class PausedState extends TrackState {
    public PausedState(Player player, Track track) {
        super(player, track);
    }

    @Override
    public void handle() {
        synchronized (track) {
            try {
                track.wait();
            } catch (InterruptedException e) {
                track.kill();
            }
        }
    }
}