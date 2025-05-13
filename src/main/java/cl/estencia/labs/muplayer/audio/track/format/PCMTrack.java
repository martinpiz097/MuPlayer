package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.track.decoder.DefaultAudioDecoder;
import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.model.AudioFileExtension;
import cl.estencia.labs.muplayer.util.FileUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class PCMTrack extends Track {

    public PCMTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new DefaultAudioDecoder(dataSource));
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, new DefaultAudioDecoder(trackPath));
    }

    public PCMTrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new DefaultAudioDecoder(dataSource), player);
    }

    public PCMTrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, new DefaultAudioDecoder(trackPath), player);
    }

    @Override
    protected AudioFileReader getAudioFileReader() {
        final String extension = FileUtil.getFormatName(dataSource != null
                ? dataSource.getName() : "");
        AudioFileExtension audioFileExtension = AudioFileExtension.valueOf(extension.toLowerCase());
        return switch (audioFileExtension) {
            case wav -> new WaveFileReader();
            case aiff, aifc -> new AiffFileReader();
            case au -> new AuFileReader();
            default -> null;
        };
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final javax.sound.sampled.AudioFormat audioFormat = speaker.getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate*seconds.doubleValue();
        return framesToSeek*frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = speaker.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    public List<String> getAudioFileExtensions() {
        return List.of(AudioFileExtension.wav.name(),
                AudioFileExtension.aiff.name(),
                AudioFileExtension.aifc.name(),
                AudioFileExtension.au.name());
    }

}
