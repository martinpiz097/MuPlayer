package org.muplayer.thread;

import org.muplayer.audio.Player;
import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.system.ListenersNames;

import java.io.File;
import java.io.IOException;

public class TPlayingTrack implements Runnable {
    private final Track track;

    public TPlayingTrack(Track track) {
        this.track = track;
        /*String title = track.getTitle();
        if (title != null && title.length() > 10)
            title = title.substring(0, 10);*/
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
        PlayerControls player;
        while (!track.isFinished() && !track.isKilled() && track.getTrackIO().isTrackStreamsOpened()) {
            try {
                player = track.getPlayer();
                if (player instanceof Player)
                    ((Player) player).loadListenerMethod(ListenersNames.ONPLAYING, track);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
