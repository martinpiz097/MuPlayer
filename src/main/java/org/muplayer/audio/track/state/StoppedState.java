package org.muplayer.audio.track.state;

import lombok.extern.java.Log;
import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.logging.Level;

@Log
public class StoppedState extends TrackState {

    public StoppedState(Player player, Track track) {
        super(player, track);
    }

    private void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        track.resetStream();
    }

    @Override
    public void handle() {
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