package org.muplayer.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.aucom.sound.Speaker;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackIO {
    private volatile Speaker trackLine;
    private volatile AudioInputStream decodedStream;
    private volatile AudioFileReader audioReader;

    public synchronized boolean hasValidTrackLine() {
        return trackLine != null;
    }

    public void resetLine() throws LineUnavailableException {
        trackLine.stop();
        trackLine.open();
    }

    public Speaker createLine() throws LineUnavailableException {
        final Speaker line = new Speaker(decodedStream.getFormat());
        line.open();
        return line;
    }

    public boolean initLine() throws LineUnavailableException {
        if (decodedStream != null) {
            if (trackLine != null) {
                trackLine.stop();
                trackLine.close();
            }
            try {
                this.trackLine = createLine();
                return true;
            } catch (IllegalArgumentException e1) {
                System.err.println("Error: "+e1.getMessage());
                return false;
            }
        }
        else {
            System.out.println("TrackStream & TrackLine null");
            return false;
        }
    }

    public void closeLine() {
        if (trackLine != null) {
            trackLine.stop();
            trackLine.close();
            trackLine = null;
        }
    }

    public boolean closeAllStreams() {
        closeLine();
        try {
            decodedStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getSecondsPosition() {
        if (trackLine == null)
            return 0;
        return ((double)trackLine.getDriver().getMicrosecondPosition()) / 1000000;
    }

    public boolean isTrackStreamsOpened() {
        return decodedStream != null && trackLine != null;
    }

    public AudioFileFormat getAudioFileFormat(Object dataSource) throws IOException, UnsupportedAudioFileException {
        if (audioReader != null && dataSource != null) {
            if (dataSource instanceof File)
                return audioReader.getAudioFileFormat((File) dataSource);
            else if (dataSource instanceof InputStream)
                return audioReader.getAudioFileFormat((InputStream) dataSource);
            else
                return audioReader.getAudioFileFormat((URL) dataSource);
        }
        return null;
    }

    public AudioFormat getAudioFormat() {
        return decodedStream.getFormat();
    }
}
