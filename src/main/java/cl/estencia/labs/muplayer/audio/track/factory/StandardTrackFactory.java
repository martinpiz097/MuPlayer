package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtensions;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.format.FlacTrack;
import cl.estencia.labs.muplayer.audio.track.format.MP3Track;
import cl.estencia.labs.muplayer.audio.track.format.OGGTrack;
import cl.estencia.labs.muplayer.audio.track.format.PCMTrack;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static cl.estencia.labs.muplayer.audio.util.AudioFileUtil.getAudioExtensionFromFormat;

@Slf4j
public class StandardTrackFactory extends TrackFactory {

    protected StandardTrackFactory() {
        super();
    }

    @Override
    public Track loadTrack(File dataSource) {
        try {
            // aca ya se evalua si el formato esta soportado, solo que de una manera mas directa
            if (dataSource == null || !dataSource.exists() || dataSource.isDirectory()) {
                throw new AudioFileInvalidException(dataSource);
            }

            final String fileFormatName = AudioFileUtil.getFileFormatName(dataSource);
            final SupportedAudioExtensions extension = getAudioExtensionFromFormat(fileFormatName);
            if (extension == null) {
                throw new FormatNotSupportedException(fileFormatName);
            }

            return switch (extension) {
                case aifc, aiff, au, snd, wav -> new PCMTrack(dataSource);
                case flac -> new FlacTrack(dataSource);
                case mp3 -> new MP3Track(dataSource);
                case ogg -> new OGGTrack(dataSource);
            };
        } catch (IllegalArgumentException | AudioFileInvalidException | FormatNotSupportedException e) {
//            log.error(e.getMessage(), e);
            log.warn(e.getMessage());
            return null;
        }
    }

}
