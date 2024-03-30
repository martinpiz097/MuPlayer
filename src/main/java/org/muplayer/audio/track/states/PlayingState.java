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
    private final byte[] audioBuffer;
    private final TPlayingTrack trackThread;

    private static final short BUFF_SIZE = 4096;
    private static final int EOF = -1;

    public PlayingState(Player player, Track track) {
        super(player, track);
        this.audioBuffer = new byte[BUFF_SIZE];
        this.trackThread = new TPlayingTrack(track);
    }

    private boolean canPlay() throws IOException {
        return trackIO.getSpeaker() != null && readNextBytes() != EOF;
    }

    private int readNextBytes() throws IOException {
        return trackIO.getDecodedStream().read(audioBuffer);
    }

    @Override
    public void handle() {
        try {
            TaskRunner.execute(trackThread);
            while (track.isPlaying())
                if (canPlay()) {
                    trackIO.playAudio(audioBuffer);
                }
                else if (player != null) {
                    player.playNext();
                }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Error on playing sound "+track.getTitle()+": ", e);
            player.playNext();
        }
    }

}
