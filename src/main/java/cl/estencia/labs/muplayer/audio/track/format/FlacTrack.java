package cl.estencia.labs.muplayer.audio.track.format;

import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.sound.spi.FlacFormatConversionProvider;
import cl.estencia.labs.muplayer.audio.io.FlacAudioIO;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackIO;
import cl.estencia.labs.muplayer.model.MuPlayerAudioFormat;
import cl.estencia.labs.muplayer.audio.io.AudioIO;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FlacTrack extends Track {

    public FlacTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public FlacTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    public FlacTrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public FlacTrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() {
        try {
            trackIO = new TrackIO();
            trackIO.setAudioFileReader(new FlacAudioFileReader());

            final AudioInputStream flacEncodedStream = audioIO.getAudioSteamBySource(trackIO.getAudioFileReader(),
                    dataSource);
            final AudioFormat format = flacEncodedStream.getFormat();
            final AudioFormat decodedFormat = audioIO.convertToPcmFormat(format);
            trackIO.setDecodedInputStream(new FlacFormatConversionProvider().
                    getAudioInputStream(decodedFormat, flacEncodedStream));
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final AudioFormat audioFormat = trackIO.getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate*seconds.doubleValue();
        return framesToSeek*frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = trackIO.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    protected AudioIO initAudioIO() {
        return new FlacAudioIO();
    }

    @Override
    public MuPlayerAudioFormat[] getAudioFileFormats() {
        return new MuPlayerAudioFormat[] {MuPlayerAudioFormat.flac};
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
