package cl.estencia.labs.muplayer.io.file;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.format.FlacTrack;
import cl.estencia.labs.muplayer.audio.track.format.MP3Track;
import cl.estencia.labs.muplayer.audio.track.format.OGGTrack;
import cl.estencia.labs.muplayer.audio.track.format.PCMTrack;
import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtensions;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class AudioFileScanner {
    private final File audioFile;

    public AudioFileScanner(File audioFile) {
        this.audioFile = audioFile;
    }

    public Track loadTrack() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        if (!AudioFileUtil.hasAudioFormatExtension(audioFile)) {
            return null;
        }

        final SupportedAudioExtensions extension = AudioFileUtil.getAudioFileExtension(audioFile);
        if (extension == null) {
            return null;
        }

        return switch (extension) {
            case aifc, aiff, au, snd, wav -> new PCMTrack(audioFile);
            case flac -> new FlacTrack(audioFile);
            case mp3 -> new MP3Track(audioFile);
            case ogg, opus -> new OGGTrack(audioFile);
        };
    }

}
