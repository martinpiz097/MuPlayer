package org.muplayer.audio.formats;

import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jflac.sound.spi.FlacAudioFileReader;
import org.jflac.sound.spi.FlacFormatConversionProvider;
import org.muplayer.audio.Track;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class FlacTrack extends Track {

    public FlacTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
        MP3AudioHeader audioHeader = null;
        try {
            audioHeader = new MP3AudioHeader(dataSource);
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
        }
    }

    public FlacTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
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
            AudioInputStream flacAis = audioReader.getAudioInputStream(dataSource);
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

            AudioFormat format = flacAis.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                    format.getSampleSizeInBits(), format.getChannels(),
                    format.getChannels() * 2, format.getSampleRate(),
                    format.isBigEndian());

            trackStream = new FlacFormatConversionProvider().
                    getAudioInputStream(decodedFormat, flacAis);


        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void seek(double seconds) throws IOException {
        if (seconds == 0)
            return;
        secsSeeked+=seconds;
        AudioFormat audioFormat = getAudioFormat();
        float frameRate = audioFormat.getFrameRate();
        int frameSize = audioFormat.getFrameSize();
        double framesToSeek = frameRate*seconds;
        long seek = Math.round(framesToSeek*frameSize);
        trackStream.read(new byte[(int) seek]);
    }

    @Override
    public void gotoSecond(double second) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        double progress = getProgress();
        if (second >= progress) {
            int gt = (int) Math.round(second-getProgress());
            seek(gt);
        }
        else if (second < progress) {
            stopTrack();
            resumeTrack();
            seek(second);
        }
    }

}
