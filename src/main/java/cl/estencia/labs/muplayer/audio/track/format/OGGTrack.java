package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.decoder.DefaultAudioDecoder;
import cl.estencia.labs.muplayer.model.AudioFileExtension;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class OGGTrack extends Track {

    public OGGTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new DefaultAudioDecoder(dataSource));
    }

    public OGGTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, new DefaultAudioDecoder(trackPath));
    }

    public OGGTrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new DefaultAudioDecoder(dataSource), player);
    }

    public OGGTrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, new DefaultAudioDecoder(trackPath), player);
    }

    @Override
    protected AudioFileReader getAudioFileReader() {
        return new JorbisAudioFileReader();
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final AudioFormat audioFormat = speaker.getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate * seconds.doubleValue();
        return framesToSeek * frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = speaker.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    public List<String> getAudioFileExtensions() {
        return List.of(AudioFileExtension.ogg.name());
    }

}
