package cl.estencia.labs.muplayer.io.file;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.format.FlacTrack;
import cl.estencia.labs.muplayer.audio.track.format.MP3Track;
import cl.estencia.labs.muplayer.audio.track.format.OGGTrack;
import cl.estencia.labs.muplayer.audio.track.format.PCMTrack;
import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtensions;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;
import cl.estencia.labs.muplayer.core.exception.AudioFileInvalidException;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static cl.estencia.labs.muplayer.audio.util.AudioFileUtil.getAudioExtensionFromFormat;

@Slf4j
public class AudioFileScanner {
    private final File audioFile;

    public AudioFileScanner(File audioFile) {
        this.audioFile = audioFile;
    }

    public Track loadTrack() throws AudioFileInvalidException, FormatNotSupportedException {
        try {
            // aca ya se evalua si el formato esta soportado, solo que de una manera mas directa
            if (audioFile == null || !audioFile.exists() || audioFile.isDirectory()) {
                throw new AudioFileInvalidException(audioFile);
            }

            final String fileFormatName = AudioFileUtil.getFileFormatName(audioFile);
            final SupportedAudioExtensions extension = getAudioExtensionFromFormat(fileFormatName);
            if (extension == null) {
                throw new FormatNotSupportedException(fileFormatName);
            }

            return switch (extension) {
                case aifc, aiff, au, snd, wav -> new PCMTrack(audioFile);
                case flac -> new FlacTrack(audioFile);
                case mp3 -> new MP3Track(audioFile);
                case ogg -> new OGGTrack(audioFile);
            };
        } catch (IllegalArgumentException | LineUnavailableException
                 | IOException | UnsupportedAudioFileException e) {
//            log.error(e.getMessage(), e);
            log.warn(e.getMessage());
            return null;
        }
    }

}
