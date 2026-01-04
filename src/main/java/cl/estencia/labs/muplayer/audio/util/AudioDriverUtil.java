package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.aucom.core.device.output.Speaker;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;

@Getter
@Setter
@Slf4j
public class AudioDriverUtil {
    public static boolean isTrackStreamsOpened(Speaker speaker, AudioInputStream decodedInputStream) {
        return decodedInputStream != null && speaker.isOpen();
    }

    public static boolean closeStream(AudioInputStream decodedInputStream) {
        try {
            boolean streamOpened = decodedInputStream != null;
            if (streamOpened) {
                decodedInputStream.close();
            }
            return streamOpened;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public static double getSecondsPosition(Speaker speaker) {
        SourceDataLine driver = speaker.getDriver();
        if (driver == null) {
            return 0;
        }

        return ((double) driver.getMicrosecondPosition()) / 1000000;
    }

}
