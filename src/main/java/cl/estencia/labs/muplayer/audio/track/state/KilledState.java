package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;
import cl.estencia.labs.muplayer.audio.track.listener.TrackStateListener;

import java.util.List;

public class KilledState extends TrackState {

    public KilledState(Player player, Track track, TrackNotifier notifier) {
        super(player, track, TrackStateName.KILLED, notifier);
    }

    @Override
    protected void handle() {
        trackIOUtil.closeStream(decodedAudioStream);
        speaker.close();
        trackData.setCanTrackContinue(false);
    }
}
