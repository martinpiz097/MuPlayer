package org.muplayer.audio.formats;

import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.sound.spi.FlacFormatConversionProvider;
import org.muplayer.audio.Track;
import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.system.AudioUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FlacTrack extends Track {

    public FlacTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public FlacTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    public FlacTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public FlacTrack(File dataSource, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public FlacTrack(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public FlacTrack(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    @Override
    public boolean isValidTrack() {
        return true;
    }

    @Override
    protected void loadAudioStream() {
        /*try {
        audioReader = new FlacAudioFileReader();
        FlacDecoder decoder = new FlacDecoder(dataSource);
        if (decoder.isFlac()) {
            decoder.decode();
            decodedStream = decoder.getDecodedStream();
            decoder = null;
            System.out.println("FlacAis: "+decodedStream);
        }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        try {
            audioReader = new FlacAudioFileReader();
            AudioInputStream flacAis = AudioUtil.instanceStream(audioReader, source);
            /*FlacDecoder flacDecoder = new FlacDecoder(dataSource);
            if (flacDecoder.isFlac()) {
                StreamInfo info = flacDecoder.getFlacInfo();
                System.out.println("SoundLenght: "+info.calcLength());
                System.out.println("MinBlockSize: "+info.getMinBlockSize());
                System.out.println("MaxBlockSize: "+info.getMaxBlockSize());
                System.out.println("MinFrameSize: "+info.getMinFrameSize());
                System.out.println("MaxFrameSize: "+info.getMaxFrameSize());
                System.out.println("BitPerSample: "+info.getBitsPerSample());
                System.out.println("Total Samples: "+info.getTotalSamples());
                System.out.println("Sample Rate: "+info.getSampleRate());
                System.out.println("Channels: "+info.getChannels());
            }

            try {
                System.out.println("IsFLacAIS: "+(flacAis instanceof Flac2PcmAudioInputStream));
                Flac2PcmAudioInputStream ais = (Flac2PcmAudioInputStream) flacAis;
            } catch (Exception e) {
                System.out.println("AIS Can't be cast to Flac2PcmAudioInputStream");
            }*/

            final AudioFormat format = flacAis.getFormat();
            final AudioFormat decodedFormat = DecodeManager.getPcmFormatByFlac(format);
            trackStream = new FlacFormatConversionProvider().
                    getAudioInputStream(decodedFormat, flacAis);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
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
    public void seek(double seconds) throws IOException {
        if (seconds == 0)
            return;
        secsSeeked+=seconds;
        final int bytesToSeek = (int) Math.round(convertSecondsToBytes(seconds));
        trackStream.read(new byte[bytesToSeek]);
    }

}
