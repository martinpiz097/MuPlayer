package org.muplayer.thread;

import org.muplayer.audio.player.MusicPlayer;
import org.muplayer.audio.track.Track;
import org.muplayer.info.ListenersNames;

import java.io.File;
import java.io.IOException;

public class TPlayingTrack implements Runnable {
    private final Track track;
    private final MusicPlayer trackMusicPlayer;

    public TPlayingTrack(Track track) {
        this.track = track;
        this.trackMusicPlayer = track.getPlayerControl() instanceof MusicPlayer ? (MusicPlayer) track.getPlayerControl() : null;
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
        if (trackMusicPlayer != null) {
            while (track.isPlaying()) {
                try {
                    trackMusicPlayer.loadListenerMethod(ListenersNames.ONPLAYING, track);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
