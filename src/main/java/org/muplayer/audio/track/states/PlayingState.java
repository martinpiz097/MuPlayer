package org.muplayer.audio.track.states;

import lombok.extern.java.Log;
import org.aucom.sound.Speaker;
import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;
import org.muplayer.thread.TPlayingTrack;
import org.muplayer.thread.TaskRunner;

import java.io.IOException;
import java.util.logging.Level;

@Log
public class PlayingState extends TrackState {
    private final Speaker trackLine;
    private final short BUFF_SIZE = 4096;
    private final byte[] audioBuffer = new byte[BUFF_SIZE];

    private final int EOF = -1;
    private final TPlayingTrack trackThread;

    private final Player player;

    public PlayingState(Track track, Player player) {
        super(track);
        this.trackLine = track.getTrackIO().getSpeaker();
        this.trackThread = new TPlayingTrack(track);
        this.player = player;
    }

    private boolean canPlay() throws IOException {
        return trackLine != null && readNextBytes() != EOF;
    }

    private int readNextBytes() throws IOException {
        return track.getTrackIO().getDecodedStream().read(audioBuffer);
    }

    private void killAndPlayNext() {
        track.kill();
        player.playNext();;
    }

    @Override
    public void handle() {
        try {
            TaskRunner.execute(trackThread);
            while (track.isPlaying())
                if (canPlay()) {
                    trackLine.playAudio(audioBuffer);
                }
                else {
                    player.playNext();
                }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Error on playing sound "+track.getTitle()+": ", e);
            player.playNext();
        }
    }

}
