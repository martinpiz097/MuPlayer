package org.muplayer.audio.track.state;

import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.Track;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class StartedState extends TrackState {
    public StartedState(Player player, Track track) {
        super(player, track);
    }

    @Override
    public void handle() {
        try {
            track.initStreamAndLine();
            if (trackData.isMute()) {
                track.mute();
            }
            track.setVolume(trackData.getVolume());
            track.play();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }
}