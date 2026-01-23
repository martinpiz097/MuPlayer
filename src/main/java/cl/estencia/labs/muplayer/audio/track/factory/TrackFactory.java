package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public interface TrackFactory {
    default Track getTrack(String dataSource) throws FormatNotSupportedException, AudioFileInvalidException {
        return getTrack(new File(dataSource));
    }

    Track getTrack(File dataSource) throws AudioFileInvalidException, FormatNotSupportedException;
}
