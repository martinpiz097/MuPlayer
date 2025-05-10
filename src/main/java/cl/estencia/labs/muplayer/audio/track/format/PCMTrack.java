package cl.estencia.labs.muplayer.audio.track.format;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import cl.estencia.labs.muplayer.audio.io.DefaultAudioIO;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.TrackIO;
import cl.estencia.labs.muplayer.model.MuPlayerAudioFormat;
import cl.estencia.labs.muplayer.audio.io.AudioIO;
import cl.estencia.labs.muplayer.util.FileUtil;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;

public class PCMTrack extends Track {

    public PCMTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    public PCMTrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public PCMTrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        trackIO = new TrackIO();
        final String extension = FileUtil.getFormatName(dataSource != null
                ? dataSource.getName() : "");
        MuPlayerAudioFormat muPlayerAudioFormat = MuPlayerAudioFormat.valueOf(extension.toLowerCase());
        AudioFileReader audioReader;

        switch (muPlayerAudioFormat) {
            case wav:
                audioReader = new WaveFileReader();
                break;
            case aiff:
            case aifc:
                audioReader = new AiffFileReader();
                break;
            case au:
                audioReader = new AuFileReader();
                break;
            default:
                audioReader = null;
        }

        if (audioReader != null) {
            AudioInputStream trackStream = audioIO.getAudioSteamBySource(audioReader, dataSource);
            trackIO.setAudioFileReader(audioReader);
            trackIO.setDecodedInputStream(trackStream);
        }
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final javax.sound.sampled.AudioFormat audioFormat = trackIO.getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate*seconds.doubleValue();
        return framesToSeek*frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final javax.sound.sampled.AudioFormat audioFormat = trackIO.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    protected AudioIO initAudioIO() {
        return new DefaultAudioIO();
    }

    @Override
    public MuPlayerAudioFormat[] getAudioFileFormats() {
        return new MuPlayerAudioFormat[] {MuPlayerAudioFormat.wav, MuPlayerAudioFormat.aiff,
                MuPlayerAudioFormat.aifc, MuPlayerAudioFormat.au};
    }
}
