package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.listener.TrackNotifier;
import cl.estencia.labs.muplayer.audio.track.listener.TrackStateListener;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.List;

public class ReverberatedState extends TrackState {
    private final double seekSeconds;

    public ReverberatedState(Player player, Track track, double seekSeconds, TrackNotifier notifier) {
        super(player, track, TrackStateName.REVERBERATED, notifier);
        this.seekSeconds = seekSeconds;
    }

    @Override
    protected void handle() {
        try {
            track.resetStream();
            track.getTrackStatusData().setSecsSeeked(0.0d);
            track.seek(Math.max(seekSeconds, 0));
            track.play();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
