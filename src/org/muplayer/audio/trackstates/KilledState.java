/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;
import org.muplayer.system.Logger;

import javax.sound.sampled.AudioInputStream;

public class KilledState extends TrackState {

    private Track track;
    private AudioInputStream decodedStream;
    private Speaker trackLine;

    public KilledState(Track track) {
        this.track = track;
        decodedStream = track.getDecodedStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        Logger.getLogger(this, "Track completed!").info();
        track.closeAllStreams();
    }
}*/
