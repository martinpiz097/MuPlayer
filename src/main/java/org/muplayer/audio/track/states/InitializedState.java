package org.muplayer.audio.track.states;

import lombok.extern.java.Log;
import org.muplayer.audio.player.Player;
import org.muplayer.audio.player.PlayerData;
import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackData;

import java.util.logging.Level;

@Log
public class InitializedState extends TrackState {
    public InitializedState(Player player, Track track) {
        super(player, track);
        handle();
    }

    @Override
    public void handle() {
        try {
            this.trackData = TrackData.builder()
                    .secsSeeked(0)
                    .bytesPerSecond(0)
                    .volume(PlayerData.DEFAULT_VOLUME)
                    .isMute(false)
                    .canTrackContinue(true)
                    .build();
            track.setTrackData(trackData);
            track.setTagInfo(track.loadTagInfo(track.getDataSourceAsFile()));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            track.kill();
        }
    }
}
