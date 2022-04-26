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
    private final TPlayingTrack trackThread;

    public PlayingState(Track track) {
        super(track);
        trackLine = track.getTrackIO().getTrackLine();
        trackThread = new TPlayingTrack(track);
    }

    private boolean canPlay() throws IOException {
        return trackLine != null && readNextBytes() != EOF;
    }

    private int readNextBytes() throws IOException {
        return track.getTrackIO().getDecodedStream().read(audioBuffer);
    }

    @Override
    public void handle() {
        try {
            TaskRunner.execute(trackThread);
            while (track.isPlaying())
                if (canPlay())
                    trackLine.playAudio(audioBuffer);
                else
                    track.finish();
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            track.finish();
            e.printStackTrace();
            final String exClassName = e.getClass().getSimpleName();
            Logger.getLogger(this, exClassName.concat(" in track "+track.getTitle())).error();
        }
    }

}
