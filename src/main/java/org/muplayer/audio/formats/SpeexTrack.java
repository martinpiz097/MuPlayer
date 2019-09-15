package org.muplayer.audio.formats;

import org.muplayer.audio.Track;
import org.muplayer.audio.codec.DecodeManager;
import org.xiph.speex.spi.Speex2PcmAudioInputStream;
import org.xiph.speex.spi.SpeexAudioFileReader;
import org.xiph.speex.spi.SpeexFormatConvertionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class SpeexTrack extends Track {

    //private SpeexDecoder decoder;
    private SpeexFormatConvertionProvider provider;

    public SpeexTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public SpeexTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        //decoder = new SpeexDecoder();
        provider = new SpeexFormatConvertionProvider();
        audioReader = new SpeexAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(dataSource);
        if (trackStream != null)
            trackStream.close();
        AudioFormat targetFormat = DecodeManager.getPcmFormatByMpeg(soundAis.getFormat());
        //trackStream = new Speex2PcmAudioInputStream(soundAis, targetFormat, dataSource.length());
        trackStream = provider.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis);
    }

}
