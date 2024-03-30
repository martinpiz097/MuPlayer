package org.muplayer.audio.track.states;

import org.muplayer.audio.track.Track;
import org.muplayer.audio.track.TrackData;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class StartedState extends TrackState {
    private final TrackData trackData;

    public StartedState(Track track) {
        super(track);
        this.trackData = track.getTrackData();
    }

    @Override
    public void handle() {
        try {
            track.initStreamAndLine();
            if (trackData.isMute())
                track.mute();
            track.setVolume(trackData.getVolume());
            track.play();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }
    }
}