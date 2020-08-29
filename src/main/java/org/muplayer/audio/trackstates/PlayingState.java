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

        // cambiar por canExecuteState
        while (track.isPlaying()) {
            try {
                read = track.getDecodedStream().read(audioBuffer);

                if (read == -1)
                    track.finish();
                if (trackLine != null)
                    trackLine.playAudio(audioBuffer);
            } catch (IOException |
                    IndexOutOfBoundsException | IllegalArgumentException e) {
                track.finish();
                Logger.getLogger(this, e.getMessage()).error();
            }
        }
        //finish();
    }

}
