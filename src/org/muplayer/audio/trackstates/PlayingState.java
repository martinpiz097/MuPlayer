/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

import static org.muplayer.audio.Track.BUFFSIZE;

// Testing
public class PlayingState implements TrackState {

    private Track track;
    private AudioInputStream trackStream;
    private Speaker trackLine;

    public PlayingState(Track track) {
        this.track = track;
        trackStream = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        try {
            byte[] audioBuffer = new byte[BUFFSIZE];
            int read = trackStream.read(audioBuffer);
            while (read != -1) {
                trackLine.playAudio(audioBuffer);
                read = trackStream.read(audioBuffer);
            }
            track.finish();
        } catch (IndexOutOfBoundsException e1) {
            track.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
*/