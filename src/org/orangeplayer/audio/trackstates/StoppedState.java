/*package org.orangeplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class StoppedState implements TrackState {

    private Track track;
    private AudioInputStream speakerAis;
    private Speaker trackLine;

    public StoppedState(Track track) {
        this.track = track;
        speakerAis = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    private void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (speakerAis != null)
            speakerAis.close();
        initLine();
    }

    private void initLine() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        track.loadAudioStream();
        if (speakerAis != null) {
            if (trackLine != null)
                trackLine.close();
            trackLine = new Speaker(speakerAis.getFormat());
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