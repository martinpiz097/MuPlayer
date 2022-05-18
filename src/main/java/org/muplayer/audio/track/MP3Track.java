package org.muplayer.audio.track;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.muplayer.audio.player.Player;
import org.muplayer.util.AudioUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MP3Track extends Track {
    private final long frameSize;
    private final double frameDurationInSec;

    public MP3Track(File dataSource) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InvalidAudioFrameException {
        this(dataSource, null);
    }

    public MP3Track(File dataSource, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException, InvalidAudioFrameException {
        super(dataSource, player);
        final MP3AudioHeader audioHeader = new MP3AudioHeader(dataSource);
        final long audioStartByte = audioHeader.getMp3StartByte();
        final long audioSize = dataSource.length() - audioStartByte;
        final long frameCount = audioHeader.getNumberOfFrames();
        this.frameSize = audioSize / frameCount;
        this.frameDurationInSec = (audioHeader.getPreciseTrackLength() / (double) frameCount);
    }

    public MP3Track(String trackPath)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InvalidAudioFrameException {
        this(new File(trackPath), null);
    }

    public MP3Track(String trackPath, Player player) throws LineUnavailableException, IOException,
            UnsupportedAudioFileException, InvalidAudioFrameException {
        this(new File(trackPath), player);
    }

    public MP3Track(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this(inputStream, null);
    }

    public MP3Track(InputStream inputStream, Player player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
        this.frameSize = 0;
        this.frameDurationInSec = 0;
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        // Ver si se escucha mejor en ogg utilizando la logica de mp3
        trackIO = new TrackIO();
        trackIO.setAudioReader(new MpegAudioFileReader());
        final AudioInputStream soundAis = AudioUtil.instanceStream(trackIO.getAudioReader(), dataSource);
        final AudioFormat baseFormat = soundAis.getFormat();

        final AudioInputStream trackStream = trackIO.getDecodedStream();
        if (trackStream != null)
            trackStream.close();
        trackIO.setDecodedStream(AudioUtil.decodeToPcm(baseFormat, soundAis));
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final double frameNeeded = seconds.doubleValue() / frameDurationInSec;
        return frameNeeded * frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        return (bytes.doubleValue() / frameSize) * frameDurationInSec;
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

}
