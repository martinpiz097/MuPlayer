package cl.estencia.labs.muplayer.audio.track.state;

import lombok.extern.java.Log;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.thread.TPlayingTrack;
import cl.estencia.labs.muplayer.thread.TaskRunner;
import cl.estencia.labs.muplayer.thread.ThreadUtil;

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
        return trackIO.getDecodedInputStream().read(audioBuffer);
    }

    @Override
    public void handle() {
        try {
            String trackThreadName = ThreadUtil.generateTrackThreadName(trackThread.getClass(), track);
            TaskRunner.execute(trackThread, trackThreadName);
            while (track.isPlaying()) {
                if (canPlay()) {
                    trackIO.playAudio(audioBuffer);
                } else if (player != null) {
                    player.playNext();
                }
            }
        } catch (IOException | IndexOutOfBoundsException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Error on playing sound " + track.getTitle() + ": ", e);
            player.playNext();
        }
    }

}
