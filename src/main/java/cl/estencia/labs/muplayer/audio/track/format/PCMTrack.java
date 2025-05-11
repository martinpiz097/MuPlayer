package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.track.decoder.DefaultAudioDecoder;
import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import cl.estencia.labs.muplayer.audio.track.decoder.util.DefaultDecoderFormatUtil;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.model.MuPlayerAudioFormat;
import cl.estencia.labs.aucom.util.DecoderFormatUtil;
import cl.estencia.labs.muplayer.util.FileUtil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;

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
        MuPlayerAudioFormat muPlayerAudioFormat = MuPlayerAudioFormat.valueOf(extension.toLowerCase());
        return switch (muPlayerAudioFormat) {
            case wav -> new WaveFileReader();
            case aiff, aifc -> new AiffFileReader();
            case au -> new AuFileReader();
            default -> null;
        };
    }

    @Override
    public void updateIOData() throws IOException, UnsupportedAudioFileException {
        AudioInputStream decodedStream = audioDecoder.getDecodedStream();

        trackIO.setAudioFileReader(getAudioFileReader());
        trackIO.setDecodedInputStream(decodedStream);

        speaker.reopen(decodedStream.getFormat());
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
        final javax.sound.sampled.AudioFormat audioFormat = speaker.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    public MuPlayerAudioFormat[] getAudioFileFormats() {
        return new MuPlayerAudioFormat[] {MuPlayerAudioFormat.wav, MuPlayerAudioFormat.aiff,
                MuPlayerAudioFormat.aifc, MuPlayerAudioFormat.au};
    }
}
