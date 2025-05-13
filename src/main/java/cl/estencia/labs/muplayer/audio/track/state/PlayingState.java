package cl.estencia.labs.muplayer.audio.track.state;

import cl.estencia.labs.muplayer.listener.TrackEvent;
import lombok.extern.java.Log;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.thread.TPlayingTrack;
import cl.estencia.labs.muplayer.thread.TaskRunner;
import cl.estencia.labs.muplayer.thread.ThreadUtil;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import static cl.estencia.labs.aucom.common.IOConstants.DEFAULT_BUFF_SIZE;
import static cl.estencia.labs.aucom.common.IOConstants.EOF;

@Log
public class PlayingState extends TrackState {
    private final byte[] audioBuffer;
    private final TPlayingTrack trackThread;
    private final AudioInputStream decodedAudioStream;

    public PlayingState(Player player, Track track, List<TrackEvent> listInternalEvents) {
        super(player, track, TrackStateName.PLAYING, listInternalEvents);
        this.audioBuffer = new byte[DEFAULT_BUFF_SIZE];
        this.trackThread = new TPlayingTrack(track);
        this.decodedAudioStream = trackIO.getDecodedInputStream();
    }

    private int readNextBytes() throws IOException {
        return decodedAudioStream.read(audioBuffer);
    }

    @Override
    protected void handle() {
        try {
            String trackThreadName = ThreadUtil.generateTrackThreadName(trackThread.getClass(), track);
            TaskRunner.execute(trackThread, trackThreadName);
            int read;
            while (track.isPlaying()) {
                if ((read = readNextBytes()) != EOF) {
                    speaker.playAudio(audioBuffer, read);
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
