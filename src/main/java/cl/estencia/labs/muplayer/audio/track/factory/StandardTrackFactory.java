package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.format.FlacTrack;
import cl.estencia.labs.muplayer.audio.track.format.MP3Track;
import cl.estencia.labs.muplayer.audio.track.format.OGGTrack;
import cl.estencia.labs.muplayer.audio.track.format.PCMTrack;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.core.common.enums.AudioFileExtension;
import cl.estencia.labs.muplayer.core.util.LogUtil;
import cl.estencia.labs.muplayer.v2.file.AudioFileScanner;
import lombok.extern.java.Log;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

@Log
public class StandardTrackFactory implements TrackFactory {

    @Override
    public Track getTrack(File dataSource) throws FormatNotSupportedException,
            UnsupportedAudioFileException, LineUnavailableException, IOException {
        return new AudioFileScanner(dataSource).loadTrack();
    }
}
