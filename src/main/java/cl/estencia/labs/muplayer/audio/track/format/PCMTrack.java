package cl.estencia.labs.muplayer.audio.track.format;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.aucom.io.AudioDecoder;

import javax.sound.sampled.AudioFormat;
import java.io.File;

public class PCMTrack extends Track {

    public PCMTrack(String trackPath, AudioDecoder audioDecoder) {
        super(trackPath, audioDecoder);
    }

    public PCMTrack(File dataSource, AudioDecoder audioDecoder) {
        super(dataSource, audioDecoder);
    }

    @Override
    protected double convertSecondsToBytes(Number seconds) {
        final AudioFormat audioFormat = speaker.getAudioFormat();
        final float frameRate = audioFormat.getFrameRate();
        final int frameSize = audioFormat.getFrameSize();
        final double framesToSeek = frameRate*seconds.doubleValue();

        return framesToSeek*frameSize;
    }

    @Override
    protected double convertBytesToSeconds(Number bytes) {
        final AudioFormat audioFormat = speaker.getAudioFormat();
        return bytes.doubleValue() / audioFormat.getFrameSize() / audioFormat.getFrameRate();
    }

}
