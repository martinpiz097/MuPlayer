package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.decoder.DefaultAudioDecoder;
import cl.estencia.labs.muplayer.audio.track.data.HeaderData;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import lombok.extern.java.Log;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

@Log
public class MP3Track extends Track {

    public MP3Track(String trackPath, TrackInternalEventNotifier internalEventNotifier) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), internalEventNotifier);
    }

    public MP3Track(File dataSource, TrackInternalEventNotifier internalEventNotifier) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new DefaultAudioDecoder(dataSource), internalEventNotifier);
    }

    private long calculateFrameSize(MP3AudioHeader mp3AudioHeader) {
        final long audioStartByte = mp3AudioHeader.getMp3StartByte();
        final long audioSize = dataSource.length() - audioStartByte;
        final long frameCount = mp3AudioHeader.getNumberOfFrames();
        return audioSize / frameCount;
    }

    private double calculateFrameDurationInSec(MP3AudioHeader mp3AudioHeader) {
        final long frameCount = mp3AudioHeader.getNumberOfFrames();

        return (mp3AudioHeader.getPreciseTrackLength() / frameCount);
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final double frameNeeded = seconds.doubleValue() / headerData.frameDurationInSec();
        return frameNeeded * headerData.frameSize();
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        return (bytes.doubleValue() / headerData.frameSize()) * headerData.frameDurationInSec();
    }

    @Override
    protected HeaderData initHeaderData() {
        try {
            MP3AudioHeader mp3AudioHeader = new MP3AudioHeader(dataSource);
            return new HeaderData(calculateFrameSize(mp3AudioHeader),
                    calculateFrameDurationInSec(mp3AudioHeader));
        } catch (IOException | InvalidAudioFrameException e) {
            return null;
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

}
