package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;
import org.muplayer.thread.TPlayingTrack;
import org.muplayer.thread.TaskRunner;
import org.orangelogger.sys.Logger;

import java.io.IOException;

public class PlayingState extends TrackState {
    private final Speaker trackLine;
    private final short BUFF_SIZE = 4096;
    private final byte[] audioBuffer = new byte[BUFF_SIZE];
    private final int EOF = -1;

    public PlayingState(Track track) {
        super(track);
        trackLine = track.getTrackLine();
    }

    private boolean canPlay() throws IOException {
        return trackLine != null && readNextBytes() != EOF;
    }

    private int readNextBytes() throws IOException {
        return track.getDecodedStream().read(audioBuffer);
    }

    private void checkTrackThread() {
        final TPlayingTrack trackThread = track.getPlayingTrack();
        if (trackThread == null || !trackThread.hasTrack(track)) {
            final TPlayingTrack tPlayingTrack = new TPlayingTrack(track);
            track.setPlayingTrack(tPlayingTrack);
            TaskRunner.execute(tPlayingTrack);
        }
    }

    @Override
    public void handle() {
        try {
            checkTrackThread();
            while (track.isPlaying())
                if (canPlay())
                    trackLine.playAudio(audioBuffer);
                else
                    track.finish();
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            track.finish();
            Logger.getLogger(this, e.getMessage()).error();
        }
    }

}
