/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class StoppedState extends TrackState {

    private Track track;
    private Speaker trackLine;

    public StoppedState(Track track) {
        this.track = track;
        trackLine = track.getTrackLine();
    }

    private void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        track.resetStream();
    }

    @Override
    public void handle() {
        try {
            resetStream();
            while (track.isStopped()) {}
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}
*/
