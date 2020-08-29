package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;
import org.muplayer.thread.TPlayingTrack;
import org.muplayer.thread.TaskRunner;
import org.orangelogger.sys.Logger;

import java.io.IOException;

// Testing
public class PlayingState extends TrackState {

    private Speaker trackLine;
    private final short BUFF_SIZE = 4096;

    public PlayingState(Track track) {
        super(track);
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        final byte[] audioBuffer = new byte[BUFF_SIZE];
        int read;

        TaskRunner.execute(new TPlayingTrack(track));
        while (track.isValidTrack()) {
            try {
                while (track.isPlaying())
                    try {
                        if (track.isPaused())
                            break;
                        read = track.getDecodedStream().read(audioBuffer);

                        if (read == -1) {
                            track.finish();
                            finish();
                            break;
                        }
                        if (trackLine != null)
                            trackLine.playAudio(audioBuffer);
                    } catch (IndexOutOfBoundsException e) {
                        finish();
                        track.finish();
                    }
                Thread.sleep(50);
            } catch (IOException |
                    InterruptedException e) {
                e.printStackTrace();
            } catch(IllegalArgumentException e) {
                finish();
                track.finish();
                Logger.getLogger(this, e.getMessage()).error();
                break;
            }
        }

        finish();
    }

}
