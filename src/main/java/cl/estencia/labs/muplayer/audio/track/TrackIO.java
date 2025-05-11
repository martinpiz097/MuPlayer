package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.aucom.audio.device.Speaker;
import lombok.Data;
import lombok.extern.java.Log;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.IOException;

@Data
@Log
public class TrackIO {
    private final Speaker speaker;
    private volatile AudioFileReader audioFileReader;
    private volatile AudioInputStream decodedInputStream;

    public TrackIO(AudioFileReader audioFileReader, AudioInputStream decodedInputStream) {
        this.speaker = initSpeaker(decodedInputStream.getFormat());
        this.audioFileReader = audioFileReader;
        this.decodedInputStream = decodedInputStream;
    }

    private Speaker initSpeaker(AudioFormat format) {
        final Speaker speaker = new Speaker(format);
        speaker.open();
        return speaker;
    }

    public boolean isTrackStreamsOpened() {
        return decodedInputStream != null && speaker.isOpen();
    }

    public boolean closeSpeaker() {
        return speaker.close();
    }

    public boolean closeStream() {
        try {
            boolean streamOpened = decodedInputStream != null;
            if (streamOpened) {
                decodedInputStream.close();
            }
            return streamOpened;
        } catch (Exception e) {
            log.severe(e.getMessage());
            return false;
        }
    }

    public double getSecondsPosition() {
        SourceDataLine driver = speaker.getDriver();
        if (driver == null) {
            return 0;
        }
        return ((double) driver.getMicrosecondPosition()) / 1000000;
    }

//    public AudioFileFormat getAudioFileFormat(Object dataSource) throws IOException, UnsupportedAudioFileException {
//        if (audioReader != null && dataSource != null) {
//            if (dataSource instanceof File) {
//                return audioReader.getAudioFileFormat((File) dataSource);
//            } else if (dataSource instanceof InputStream) {
//                return audioReader.getAudioFileFormat((InputStream) dataSource);
//            } else {
//                return audioReader.getAudioFileFormat((URL) dataSource);
//            }
//        }
//        return null;
//    }

}
