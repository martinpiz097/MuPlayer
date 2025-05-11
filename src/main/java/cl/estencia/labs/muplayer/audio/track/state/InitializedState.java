package cl.estencia.labs.muplayer.audio.track.state;

import lombok.extern.java.Log;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;

import java.util.logging.Level;

import static cl.estencia.labs.aucom.util.DecoderFormatUtil.DEFAULT_VOLUME;

@Log
public class InitializedState extends TrackState {
    public InitializedState(Player player, Track track) {
        super(player, track);
        handle();
    }

    @Override
    public void handle() {
        try {
            trackData.setSecsSeeked(0);
            trackData.setBytesPerSecond(0);
            trackData.setVolume(DEFAULT_VOLUME);
            trackData.setMute(false);
            trackData.setCanTrackContinue(true);
            track.setTagInfo(track.loadTagInfo(track.getDataSource()));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            track.kill();
        }
    }
}
