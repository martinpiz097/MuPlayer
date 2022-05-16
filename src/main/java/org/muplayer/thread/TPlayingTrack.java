package org.muplayer.thread;

import org.muplayer.audio.Player;
import org.muplayer.audio.Track;
import org.muplayer.info.ListenersNames;

import java.io.File;
import java.io.IOException;

public class TPlayingTrack implements Runnable {
    private final Track track;
    private final Player trackPlayer;

    public TPlayingTrack(Track track) {
        this.track = track;
        this.trackPlayer = track.getPlayer() instanceof Player ? (Player) track.getPlayer() : null;
    }

    public boolean hasTrack(Track track) {
        try {
            File dataSource = this.track.getDataSourceAsFile();
            final String dataSourcePath = dataSource.getCanonicalPath();
            final String anotherSourcePath = dataSource.getCanonicalPath();
            return dataSourcePath.equals(anotherSourcePath);
        } catch (IOException e) {
            return true;
        }
    }

    @Override
    public void run() {
        if (trackPlayer != null) {
            while (track.isPlaying()) {
                try {
                    trackPlayer.loadListenerMethod(ListenersNames.ONPLAYING, track);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
