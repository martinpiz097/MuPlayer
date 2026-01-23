package cl.estencia.labs.muplayer.core.aucom.io;

import cl.estencia.labs.muplayer.core.aucom.util.AudioDecodingUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

@Getter
@Setter
@Slf4j
public abstract class AudioDecoder {
    protected final File source;
    protected final AudioDecodingUtil audioDecodingUtil;

    protected volatile AudioInputStream decodedAudioStream;

    public AudioDecoder(String path) throws UnsupportedAudioFileException, IOException {
        this(new File(path), new AudioDecodingUtil());
    }

    public AudioDecoder(File file) throws UnsupportedAudioFileException, IOException {
        this(file, new AudioDecodingUtil());
    }

    public AudioDecoder(String path, AudioDecodingUtil audioDecodingUtil) throws UnsupportedAudioFileException, IOException {
        this(new File(path), audioDecodingUtil);
    }

    public AudioDecoder(File file, AudioDecodingUtil audioDecodingUtil) throws UnsupportedAudioFileException, IOException {
        this.source = file;
        this.audioDecodingUtil = audioDecodingUtil;
        this.decodedAudioStream = buildDecodedAudioStream();
    }

    protected AudioInputStream initSourceStream(File file) {
        try {
            return AudioSystem.getAudioInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized AudioFormat getDecodedFormat() {
        return decodedAudioStream.getFormat();
    }

    public abstract AudioFormat convertToPcmFormat(AudioFormat baseFormat);

    public boolean tryCloseCurrentStream(AudioInputStream audioInputStream) {
        try {
            audioInputStream.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized void reDecode() {
        setDecodedAudioStream(buildDecodedAudioStream());
    }

    public AudioInputStream buildDecodedAudioStream() {
        AudioInputStream sourceStream = initSourceStream(source);
        if (sourceStream == null) {
            return null;
        }

        AudioFormat baseFormat = sourceStream.getFormat();
        AudioFormat pcmFormat = convertToPcmFormat(baseFormat);

        return audioDecodingUtil.decodeToPcm(sourceStream, pcmFormat);
    }

    public synchronized AudioInputStream getDecodedAudioStream() {
        return decodedAudioStream;
    }

    public synchronized void setDecodedAudioStream(AudioInputStream decodedAudioStream) {
        tryCloseCurrentStream(this.decodedAudioStream);
        this.decodedAudioStream = decodedAudioStream;
    }

}
