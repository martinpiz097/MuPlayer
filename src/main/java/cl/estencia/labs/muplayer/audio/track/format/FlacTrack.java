package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.decoder.FlacAudioDecoder;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FlacTrack extends Track {

    public FlacTrack(String trackPath, TrackInternalEventNotifier internalEventNotifier) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), internalEventNotifier);
    }

    public FlacTrack(File dataSource, TrackInternalEventNotifier internalEventNotifier) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new FlacAudioDecoder(dataSource), internalEventNotifier);
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final AudioFormat audioFormat = speaker.getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate * seconds.doubleValue();
        return framesToSeek * frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = speaker.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

    @Override
    public synchronized void seek(double seconds) throws IOException {
       if (seconds != 0) {
           trackStatusData.setSecsSeeked(trackStatusData.getSecsSeeked()+seconds);
           final int bytesToSeek = (int) Math.round(convertSecondsToBytes(seconds));

           AudioInputStream decodedStream = audioDecoder.getDecodedStream();
           if (decodedStream != null) {
               decodedStream.read(new byte[bytesToSeek]);
           }
       }
    }

}
