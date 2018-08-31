/*package org.muplayer.audio.trackstates;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;
import org.muplayer.system.Logger;
import org.muplayer.thread.PlayerHandler;

import javax.sound.sampled.AudioInputStream;

public class FinishedState extends TrackState {

    private Track track;
    private AudioInputStream trackStream;
    private Speaker trackLine;

    public FinishedState(Track track) {
        this.track = track;
        trackStream = track.getTrackStream();
        trackLine = track.getTrackLine();
    }

    @Override
    public void handle() {
        Logger.getLogger(this, "Track completed!").info();
        if (PlayerHandler.hasInstance() && track.isPlayerLinked()) {
            //Logger.getLogger(this, "Entra a if isfinished").error();
            track.closeAllStreams();
            PlayerHandler.getPlayer().waitNextTrack();
        }
    }
}
*/
