package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.decoder.DefaultAudioDecoder;
import cl.estencia.labs.muplayer.audio.track.data.HeaderData;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class OGGTrack extends Track {

    public OGGTrack(String trackPath, TrackInternalEventNotifier internalEventNotifier) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), internalEventNotifier);
    }

    public OGGTrack(File dataSource, TrackInternalEventNotifier internalEventNotifier) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, new DefaultAudioDecoder(dataSource), internalEventNotifier);
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
    protected HeaderData initHeaderData() {
        return new HeaderData(0L, 0d);
    }

}
