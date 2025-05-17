package cl.estencia.labs.muplayer.audio.track.io;

import cl.estencia.labs.aucom.core.device.output.Speaker;
import lombok.Data;
import lombok.extern.java.Log;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

@Data
@Log
public class TrackIOUtil {

    public boolean isTrackStreamsOpened(Speaker speaker, AudioInputStream decodedInputStream) {
        return decodedInputStream != null && speaker.isOpen();
    }

    public boolean closeStream(AudioInputStream decodedInputStream) {
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

    public double getSecondsPosition(Speaker speaker) {
        SourceDataLine driver = speaker.getDriver();
        if (driver == null) {
            return 0;
        }
        return ((double) driver.getMicrosecondPosition()) / 1000000;
    }

}
