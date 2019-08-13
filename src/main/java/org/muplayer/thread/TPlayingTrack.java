package org.muplayer.thread;

import org.muplayer.audio.Track;
import org.muplayer.system.ListenersNames;

public class TPlayingTrack extends Thread {
    private final Track track;

    public TPlayingTrack(Track track) {
        this.track = track;
        String title = track.getTitle();
        if (title.length() > 10)
            title = title.substring(0, 10);

        setName("threadPlaying: "+title);
    }

    @Override
    public void run() {
        while (!track.isFinished() && !track.isKilled() && track.isValidTrack()) {
            try {
                PlayerHandler.getPlayer().loadListenerMethod(ListenersNames.ONPLAYING, track);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
