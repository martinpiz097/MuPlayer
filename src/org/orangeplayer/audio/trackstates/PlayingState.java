/*package org.orangeplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;

import static org.orangeplayer.audio.Track.BUFFSIZE;

// Testing
public class PlayingState implements TrackState {

    private Track track;
    private AudioInputStream speakerAis;
    private Speaker trackLine;

    public PlayingState(Track track) {
        this.track = track;
        speakerAis = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        try {
            byte[] audioBuffer = new byte[BUFFSIZE];
            int read = speakerAis.read(audioBuffer);
            while (read != -1) {
                trackLine.playAudio(audioBuffer);
                read = speakerAis.read(audioBuffer);
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