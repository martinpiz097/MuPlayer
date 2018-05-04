package org.orangeplayer.audio.tracksFormats;

import com.sun.media.sound.AiffFileReader;
import com.sun.media.sound.AuFileReader;
import com.sun.media.sound.WaveFileReader;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static org.orangeplayer.audio.AudioExtensions.AIFC;
import static org.orangeplayer.audio.AudioExtensions.AIFF;
import static org.orangeplayer.audio.AudioExtensions.WAVE;

public class PCMTrack extends Track {

    public PCMTrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public PCMTrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        final AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(ftrack);
        final String extension = fileFormat.getType().getExtension();
        switch (extension) {
            case WAVE:
                audioReader = new WaveFileReader();
                break;
            case AIFF:
                audioReader = new AiffFileReader();
                break;
            case AIFC:
                audioReader = new AiffFileReader();
                break;
            default:
                audioReader = new AuFileReader();
                break;
        }
        speakerAis = audioReader.getAudioInputStream(ftrack);
    }

    @Override
    public void seek(int seconds) {

    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        Track track = new PCMTrack("/home/martin/AudioTesting/audio/wav.wav");
        new Thread(track).start();
    }

}
