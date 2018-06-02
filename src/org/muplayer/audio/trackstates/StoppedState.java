/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class StoppedState implements TrackState {

    private Track track;
    private AudioInputStream trackStream;
    private Speaker trackLine;

    public StoppedState(Track track) {
        this.track = track;
        trackStream = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    private void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (trackStream != null)
            trackStream.close();
        initLine();
    }

    private void initLine() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        track.loadAudioStream();
        if (trackStream != null) {
            if (trackLine != null)
                trackLine.close();
            trackLine = new Speaker(trackStream.getFormat());
            trackLine.open();
        }
    }

    @Override
    public void handle() {
        try {
            resetStream();
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        while (true) {}
    }
}
*/