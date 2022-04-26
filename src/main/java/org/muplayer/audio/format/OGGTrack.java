package org.muplayer.audio.format;

import org.muplayer.audio.Track;
import org.muplayer.info.TrackIO;
import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.interfaces.PlayerControls;
import org.muplayer.util.AudioUtil;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class OGGTrack extends Track {

    public OGGTrack(File dataSource)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(dataSource);
    }

    public OGGTrack(String trackPath) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    public OGGTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public OGGTrack(File dataSource, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public OGGTrack(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public OGGTrack(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    private AudioInputStream createAudioStream() throws IOException, UnsupportedAudioFileException {
        trackIO.setAudioReader(new JorbisAudioFileReader());
        final AudioInputStream soundEncodedStream = AudioUtil.instanceStream(trackIO.getAudioReader(),
                dataSource);
        return DecodeManager.decodeToPcm(soundEncodedStream);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        trackIO = new TrackIO();
        trackIO.setDecodedStream(createAudioStream());
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

}
