/*package org.muplayer.audio.formats;

import org.muplayer.audio.Track;
import org.xiph.speex.SpeexDecoder;
import org.xiph.speex.spi.SpeexAudioFileReader;
import org.xiph.speex.spi.SpeexFormatConvertionProvider;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class SpeexTrack extends Track {

    private SpeexDecoder decoder;
    private SpeexFormatConvertionProvider provider;

    public SpeexTrack(File dataSource) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource);
    }

    public SpeexTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        decoder = new SpeexDecoder();
        provider = new SpeexFormatConvertionProvider();
        audioReader = new SpeexAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(dataSource);
        if (trackStream != null)
            trackStream.close();
        trackStream = provider.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis);
    }

    @Override
    public String getDurationAsString() {
        return null;
    }

    @Override
    public String getFormattedDuration() {
        return null;
    }

    public static void main(String[] args) {
        Track.getTrack("/home/martin/AudioTesting/au.spx").start();

    }

}
*/
