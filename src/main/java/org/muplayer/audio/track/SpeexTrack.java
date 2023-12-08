package org.muplayer.audio.track;

import org.muplayer.audio.player.Player;
import org.muplayer.util.AudioUtil;
import org.xiph.speex.spi.SpeexAudioFileReader;
import org.xiph.speex.spi.SpeexFormatConvertionProvider;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

// CORREGIR GETDURATION GOTO Y SEEK
// PROBAR FORMATO AC3
public class SpeexTrack extends Track {

    public SpeexTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public SpeexTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public SpeexTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    public SpeexTrack(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public SpeexTrack(InputStream inputStream, Player player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public SpeexTrack(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        trackIO = new TrackIO();
        trackIO.setAudioReader(new SpeexAudioFileReader());

        final SpeexFormatConvertionProvider provider = new SpeexFormatConvertionProvider();
        final AudioInputStream soundAis = AudioUtil.instanceStream(trackIO.getAudioReader(), dataSource);

        final AudioInputStream trackStream = trackIO.getDecodedStream();
        if (trackStream != null)
            trackStream.close();
        trackIO.setDecodedStream(provider.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis));
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
    public long getDuration() {
        try {
            final AudioFileFormat fileFormat = AudioUtil.getAudioFileFormat(dataSource);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
