package cl.estencia.labs.muplayer.core.aucom.io;

import cl.estencia.labs.muplayer.audio.common.enums.SupportedAudioExtension;
import cl.estencia.labs.muplayer.audio.track.decoder.DefaultAudioDecoder;
import cl.estencia.labs.muplayer.audio.track.decoder.FlacAudioDecoder;
import cl.estencia.labs.muplayer.audio.util.AudioFileUtil;
import cl.estencia.labs.muplayer.core.aucom.util.AudioDecodingUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Getter
@Slf4j
public abstract class AudioDecoder {
    protected final File source;
    protected final AudioDecodingUtil audioDecodingUtil;

    protected volatile AudioInputStream decodedAudioStream;

    public AudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        this(new File(path));
    }

    public AudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        this.source = file;
        this.audioDecodingUtil = new AudioDecodingUtil();
        this.decodedAudioStream = decodeAudio();
    }

    protected abstract AudioFormat convertToPcmFormat(AudioFormat baseFormat);

    protected AudioInputStream decodeAudio() throws UnsupportedAudioFileException, IOException {
        AudioInputStream sourceStream = AudioSystem.getAudioInputStream(source);
        if (sourceStream == null) {
            return null;
        }

        AudioFormat baseFormat = sourceStream.getFormat();
        AudioFormat pcmFormat = convertToPcmFormat(baseFormat);

        return audioDecodingUtil.decodeToPcm(sourceStream, pcmFormat);
    }

    public boolean tryCloseCurrentStream(AudioInputStream audioInputStream) {
        try {
            if (audioInputStream == null) {
                return false;
            }

            audioInputStream.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void redecodeAudio() throws UnsupportedAudioFileException, IOException {
        AudioInputStream decodedAudioStream = decodeAudio();

        tryCloseCurrentStream(this.decodedAudioStream);
        this.decodedAudioStream = decodedAudioStream;
    }

    public synchronized AudioInputStream getDecodedAudioStream() {
        return decodedAudioStream;
    }

    public synchronized AudioFormat getDecodedFormat() {
        return decodedAudioStream != null ? decodedAudioStream.getFormat() : null;
    }

    public static AudioDecoder getDecoder(File audioFile, SupportedAudioExtension audioFileExtension) throws UnsupportedAudioFileException, IOException {
        return switch (audioFileExtension) {
            case flac -> new FlacAudioDecoder(audioFile);
            default -> new DefaultAudioDecoder(audioFile);
        };
    }

    public static AudioDecoder getDecoder(File audioFile) throws UnsupportedAudioFileException, IOException {
        SupportedAudioExtension extension = AudioFileUtil.getAudioFileExtension(audioFile);
        return getDecoder(audioFile, extension);
    }

}
