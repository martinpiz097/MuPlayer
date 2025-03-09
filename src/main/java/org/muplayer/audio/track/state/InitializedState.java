package org.muplayer.audio.track.state;

import lombok.extern.java.Log;
import org.muplayer.audio.player.Player;
import org.muplayer.audio.player.PlayerData;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackData;

import java.util.logging.Level;

import static org.muplayer.util.AudioUtil.DEFAULT_VOLUME;

@Log
public class InitializedState extends TrackState {
    public InitializedState(Player player, Track track) {
        super(player, track);
        handle();
    }

    @Override
    public void handle() {
        try {
            synchronized (trackData) {
                trackData.setSecsSeeked(0);
                trackData.setBytesPerSecond(0);
                trackData.setVolume(DEFAULT_VOLUME);
                trackData.setMute(false);
                trackData.setCanTrackContinue(true);
            }
            track.setTagInfo(track.loadTagInfo(track.getDataSource()));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            track.kill();
        }
    }
}
