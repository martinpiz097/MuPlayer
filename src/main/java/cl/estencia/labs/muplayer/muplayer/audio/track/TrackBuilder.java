package cl.estencia.labs.muplayer.muplayer.audio.track;

import cl.estencia.labs.muplayer.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.muplayer.data.properties.support.AudioSupportInfo;
import cl.estencia.labs.muplayer.muplayer.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.muplayer.exception.MuPlayerException;
import cl.estencia.labs.muplayer.muplayer.util.FileUtil;
import cl.estencia.labs.muplayer.muplayer.util.TrackUtil;

import java.io.File;

public class TrackBuilder {
    private final AudioSupportInfo audioSupportInfo;
    private final TrackUtil trackUtil;

    public TrackBuilder() {
        this.audioSupportInfo = new AudioSupportInfo();
        this.trackUtil = new TrackUtil();
    }

    public Track getTrack(String dataSource) {
        return getTrack(new File(dataSource));
    }

    public Track getTrack(File dataSource) {
        return getTrack(dataSource, null);
    }

    public Track getTrack(String dataSource, Player player) {
        return getTrack(new File(dataSource), player);
    }

    public Track getTrack(File dataSource, Player player) {
        if (dataSource != null) {
            Track result;

            if (dataSource.exists()) {
                final String formatName = FileUtil.getFormatName(dataSource.getName());
                final String formatClass = audioSupportInfo.getProperty(formatName);

                if (formatClass != null) {
                    result = trackUtil.getTrackFromClass(formatClass, dataSource, player);
                } else {
                    throw new FormatNotSupportedException("Audio format " + formatName + " not supported!");
                }
            } else {
                throw new MuPlayerException("The dataSource file for path " + dataSource.getPath() + " not exists");
            }

            return result;
        } else {
            throw new MuPlayerException("The dataSource object is null");
        }
    }
}
