package cl.estencia.labs.muplayer.thread;

import cl.estencia.labs.muplayer.audio.player.MuPlayer;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.util.MuPlayerUtil;

import java.io.File;
import java.io.IOException;

public class TPlayingTrack implements Runnable {
    private final Track track;
    private final MuPlayer trackPlayer;
    private final MuPlayerUtil trackPlayerUtil;

    public TPlayingTrack(Track track) {
        this.track = track;
        this.trackPlayer = track.getPlayer() instanceof MuPlayer ? (MuPlayer) track.getPlayer() : null;
        this.trackPlayerUtil = track.getPlayer() instanceof MuPlayer
                ? ((MuPlayer) track.getPlayer()).getMuPlayerUtil() : null;
    }

    public boolean hasTrack(Track track) {
        try {
            File dataSource = this.track.getDataSource();
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
//                    trackPlayerUtil.loadListenerMethod(ListenerMethodName.ON_PLAYING, track);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
