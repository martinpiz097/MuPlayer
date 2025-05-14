package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;
import cl.estencia.labs.muplayer.audio.track.listener.TrackStateListener;

import java.util.List;

public class StartedState extends TrackState {
    public StartedState(Player player, Track track, TrackNotifier notifier) {
        super(player, track, TrackStateName.STARTED, notifier);
    }

    @Override
    protected void handle() {
        if (trackData.isMute()) {
            track.mute();
        }
        track.setVolume(trackData.getVolume());
        track.play();
    }
}