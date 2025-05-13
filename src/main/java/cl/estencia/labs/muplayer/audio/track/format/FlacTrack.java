package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.track.decoder.FlacAudioDecoder;
import org.jflac.sound.spi.FlacAudioFileReader;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.model.AudioFileExtension;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class FlacTrack extends Track {

    public FlacTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new FlacAudioDecoder(dataSource));
    }

    public FlacTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, new FlacAudioDecoder(trackPath));
    }

    public FlacTrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new FlacAudioDecoder(dataSource), player);
    }

    public FlacTrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, new FlacAudioDecoder(trackPath), player);
    }

    @Override
    protected AudioFileReader getAudioFileReader() {
        return new FlacAudioFileReader();
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
        return List.of(AudioFileExtension.flac.name());
    }
    @Override
    public synchronized void seek(double seconds) throws IOException {
       if (seconds != 0) {
           trackStatusData.setSecsSeeked(trackStatusData.getSecsSeeked()+seconds);
           final int bytesToSeek = (int) Math.round(convertSecondsToBytes(seconds));

           AudioInputStream decodedStream = trackIO.getDecodedInputStream();
           if (decodedStream != null) {
               decodedStream.read(new byte[bytesToSeek]);
           }
       }
    }

}
