package cl.estencia.labs.muplayer.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.muplayer.audio.track.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class ReverberatedState extends TrackState {
    private final double seekSeconds;

    public ReverberatedState(Player player, Track track, double seekSeconds) {
        super(player, track);
        this.seekSeconds = seekSeconds;
    }

    @Override
    public void handle() {
        try {
            track.resetStream();
            track.getTrackStatusData().setSecsSeeked(0);
            track.seek(Math.max(seekSeconds, 0));
            track.play();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
