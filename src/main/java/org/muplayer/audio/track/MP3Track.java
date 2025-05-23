package org.muplayer.audio.track;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.muplayer.audio.player.Player;
import org.muplayer.util.AudioUtil;
import org.orangelogger.sys.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.FileOutputStream;
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

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        // Ver si se escucha mejor en ogg utilizando la logica de mp3
        trackIO = new TrackIO();
        trackIO.setAudioReader(new MpegAudioFileReader());
        AudioInputStream encodedAudioStream = AudioUtil.instanceStream(trackIO.getAudioReader(), dataSource);
        AudioFormat baseFormat = encodedAudioStream.getFormat();

        AudioInputStream trackStream = trackIO.getDecodedStream();
        if (trackStream != null)
            trackStream.close();
        trackIO.setDecodedStream(AudioUtil.decodeToPcm(baseFormat, encodedAudioStream));
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
