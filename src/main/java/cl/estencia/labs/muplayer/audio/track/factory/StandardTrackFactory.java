package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.io.file.AudioFileScanner;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

@Slf4j
public class StandardTrackFactory implements TrackFactory {

    @Override
    public Track getTrack(File dataSource) throws FormatNotSupportedException, AudioFileInvalidException {
        AudioFileScanner audioFileScanner = new AudioFileScanner(dataSource);
        return audioFileScanner.loadTrack();
    }
}
