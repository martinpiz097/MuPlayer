package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.muplayer.audio.player.Player;

import java.io.File;

import static cl.estencia.labs.muplayer.util.FileUtil.getFileFormatName;

public interface TrackFactory {
    default Track getTrack(String dataSource) {
        return getTrack(new File(dataSource));
    }
    default Track getTrack(File dataSource) {
        return getTrack(dataSource, null);
    }
    default Track getTrack(String dataSource, Player player) {
        return getTrack(new File(dataSource), player);
    }
    Track getTrack(File dataSource, Player player);
}
