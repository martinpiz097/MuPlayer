package org.muplayer.audio.formats;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.muplayer.audio.Track;
import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.system.AudioUtil;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MP3Track extends Track {

    private volatile MP3AudioHeader audioHeader;

    private volatile long audioStartByte;
    private volatile long audioSize;
    private volatile long frameCount;
    private volatile long frameSize;
    private volatile double frameDurationInSec;


    public MP3Track(File ftrack) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InvalidAudioFrameException {
        super(ftrack);
        audioHeader = new MP3AudioHeader(ftrack);
        audioStartByte = audioHeader.getMp3StartByte();
        audioSize = dataSource.length() - audioStartByte;
        frameCount = audioHeader.getNumberOfFrames();
        frameSize = audioSize / frameCount;

        frameDurationInSec = (audioHeader.getPreciseTrackLength() / (double) frameCount);
        System.out.println("AudioHeader: FrameSize: "+frameSize);
        System.out.println("AudioHeader: FrameCount: "+frameCount);
        System.out.println("AudioHeader: AudioSize: "+frameSize*frameCount);
        System.out.println("AudioHeader: FrameDuration: "+frameDurationInSec);
        System.out.println("AudioHeader: FrameDurationXframeCount: "+frameDurationInSec*frameCount);
        System.out.println("AudioHeader: StartByte: "+audioStartByte);
        System.out.println("AudioHeader: Mp3StartByte: "+audioHeader.getMp3StartByte());
        System.out.println("AudioHeader: TrackLenght: "+audioHeader.getTrackLength());
        System.out.println("AudioHeader: PreciseTrackLenght: "+audioHeader.getPreciseTrackLength());
        System.out.println("AudioHeader: AudioDataLenght: "+audioHeader.getTrackLengthAsString());
        System.out.println("AudioHeader: LenghtAsString: "+audioHeader.getTrackLengthAsString());
        System.out.println("AudioInputStream: FrameLenght: "+trackStream.getFrameLength());
        System.out.println("AudioInputStream: FrameSize: "+trackStream.getFormat().getFrameSize());
        System.out.println("File: AudioSize: "+audioSize);
        System.out.println("----------------------------------------");
    }

    public MP3Track(String trackPath)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InvalidAudioFrameException {
        this(new File(trackPath));
    }

    public MP3Track(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        // Ver si se escucha mejor en ogg utilizando la logica de mp3
        audioReader = new MpegAudioFileReader();
        AudioInputStream soundAis = AudioUtil.instanceStream(audioReader, source);
        AudioFormat baseFormat = soundAis.getFormat();
        if (trackStream != null)
            trackStream.close();
        trackStream = DecodeManager.decodeToPcm(baseFormat, soundAis);
    }

    private long getBytesToSeek(double sec) {
        double frameNeeded = sec / frameDurationInSec;
        long toSkip = Math.round(frameNeeded * frameSize);
        System.out.println("ToSkip: "+toSkip);
        return toSkip;
    }

    @Override
    public void seek(double seconds)
            throws IOException {
        if (seconds == 0)
            return;
        secsSeeked+=seconds;
        long bytesToSeek = getBytesToSeek(seconds);
        long skip = -2;
        try {
            skip = trackStream.skip(bytesToSeek);
            System.out.println("Skipped: "+skip+"/BytesToSkip: "+bytesToSeek);
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
    }

    // bytesLeidos -> bytesTotales
    // segundosLeidos(x) -> segundosTotales
    // bytesLeidos*segundosTotales
    // ---------------------------
    //     bytesTotales

    // framesLeidos(x) --> framesTotales
    // bytesLeidos-startByte --> bytesTotales
    //
    // --------------------------------------
    //
    // framesLeidos --> framesTotales
    // secsLeidos(x) --> secsTotales

    // Despues probar con duracion de frames en segundos y antes
    // de eso con la duracion precisa

    // nombres de variables dados por iniciales
    /*private int getSecondsFromBytes(long bytes) {
        bytes -= audioStartByte;
        long btxft = bytes*frameCount;
        long framesReaded = btxft/audioSize;
        long frxst = (long) (framesReaded*audioHeader.getPreciseTrackLength());
        return (int) (frxst / frameCount);
    }*/

    /*public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException, URISyntaxException {
        URL url = new URL("http://localhost/au.mp3");
        File dataSource = new File(url.toString());
        MpegAudioFileReader audioReader = new MpegAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(dataSource);
        AudioFormat baseFormat = soundAis.getFormat();
        AudioInputStream trackStream = DecodeManager.decodeToPcm(baseFormat, soundAis);
        Speaker speaker = new Speaker(trackStream.getFormat());
        speaker.open();

        byte[] buffer = new byte[4096];
        while (true) {
            trackStream.read(buffer);
            speaker.playAudio(buffer);
        }

    }*/

}
