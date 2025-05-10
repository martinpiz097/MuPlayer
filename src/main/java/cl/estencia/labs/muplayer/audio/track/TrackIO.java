package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.aucom.audio.device.Speaker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Log
public class TrackIO {
    private volatile Speaker speaker;
    private volatile AudioInputStream decodedStream;
    private volatile AudioFileReader audioReader;

    public Speaker createSpeaker() {
        final Speaker speaker = new Speaker(decodedStream.getFormat());
        speaker.open();
        return speaker;
    }

    public boolean initSpeaker() {
        if (decodedStream != null) {
            if (speaker != null && speaker.isOpen()) {
                speaker.close();
            }
            try {
                this.speaker = createSpeaker();
                return true;
            } catch (IllegalArgumentException e1) {
                System.err.println("Error: " + e1.getMessage());
                return false;
            }
        } else {
            log.severe("TrackStream & TrackLine null");
            return false;
        }
    }

    public boolean closeSpeaker() {
        boolean existsSpeaker = speaker != null;
        if (existsSpeaker) {
            speaker.close();
            speaker = null;
        }
        return existsSpeaker;
    }

    public boolean closeStream() {
        try {
            boolean streamOpened = decodedStream != null;
            if (streamOpened) {
                decodedStream.close();
            }
            return streamOpened;
        } catch (Exception e) {
            log.severe(e.getMessage());
            return false;
        }
    }

    public boolean resetDecodedStream() {
        try {
            decodedStream.reset();
            return true;
        } catch (IOException e) {
            log.severe(e.getMessage());
            return false;
        }
    }

    public double getSecondsPosition() {
        SourceDataLine driver = speaker.getDriver();
        if (speaker == null || driver == null) {
            return 0;
        }
        return ((double) driver.getMicrosecondPosition()) / 1000000;
    }

    public boolean isTrackStreamsOpened() {
        return decodedStream != null && speaker != null;
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

    public AudioFormat getAudioFormat() {
        return decodedStream.getFormat();
    }

    public float getVolume() {
        return speaker.getVolume();
    }

    public void setVolume(float gain) {
        speaker.setVolume(gain);
    }

    public SourceDataLine getSpeakerDriver() {
        return speaker.getDriver();
    }

    public void playAudio(byte[] audioData) {
        speaker.playAudio(audioData);
    }
}
