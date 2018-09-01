/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;
import org.muplayer.system.Logger;
import org.muplayer.thread.ThreadManager;

import java.io.IOException;

// Testing
public class PlayingState extends TrackState {

    private Track track;
    private Speaker trackLine;

    private int read;
    private int readedBytes;
    private byte[] audioBuffer;
    private int currentSeconds;
    private long ti;

    public PlayingState(Track track) {
        this.track = track;
        trackLine = track.getTrackLine();
        audioBuffer = new byte[4096];
        currentSeconds = 0;
    }

    @Override
    public void handle() {
        while (track.isPlaying()) {
            try {
                read = track.getDecodedStream().read(audioBuffer);
                readedBytes+=read;
                if (ThreadManager.hasOneSecond(ti)) {
                    //currentSeconds=(getSecondsByBytes(readedBytes));
                    currentSeconds++;
                    ti = System.currentTimeMillis();
                }
                if (read == -1) {
                    track.finish();
                    break;
                }
                else if (trackLine != null)
                    trackLine.playAudio(audioBuffer);
                else
                    Logger.getLogger(this, "TrackLineNull").info();
            } catch (IndexOutOfBoundsException e) {
                track.finish();
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
*/
