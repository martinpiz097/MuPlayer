package org.muplayer.audio.format;

import org.muplayer.audio.Track;
import org.muplayer.audio.interfaces.PlayerControls;
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

    public SpeexTrack(File dataSource, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public SpeexTrack(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public SpeexTrack(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        //decoder = new SpeexDecoder();
        //private SpeexDecoder decoder;
        final SpeexFormatConvertionProvider provider = new SpeexFormatConvertionProvider();
        this.audioReader = new SpeexAudioFileReader();
        final AudioInputStream soundAis = AudioUtil.instanceStream(audioReader, source);

        if (trackStream != null)
            trackStream.close();
        //AudioFormat targetFormat = DecodeManager.getPcmFormatByMpeg(soundAis.getFormat());
        //trackStream = new Speex2PcmAudioInputStream(soundAis, targetFormat, dataSource.length());
        trackStream = provider.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis);
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final AudioFormat audioFormat = getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate*seconds.doubleValue();
        return framesToSeek*frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    public long getDuration() {
        try {
            final AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(dataSource);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
