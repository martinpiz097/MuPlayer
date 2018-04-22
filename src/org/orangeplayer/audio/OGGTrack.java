package org.orangeplayer.audio;

import com.jcraft.jorbis.JOrbisException;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class OGGTrack extends Track {

    public OGGTrack(File ftrack) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(ftrack);
    }

    public OGGTrack(String trackPath)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(trackPath);
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException {
        JorbisAudioFileReader reader = new JorbisAudioFileReader();
        AudioInputStream soundAis = reader.getAudioInputStream(ftrack);
        speakerAis = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis);
    }

    @Override
    public void seek(int seconds) throws Exception {
        // Testing skip bytes
        try {
            speakerAis.skip(seconds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws
            InterruptedException, UnsupportedAudioFileException,
            IOException, LineUnavailableException, JOrbisException {
        File sound = new File("/home/martin/AudioTesting/audio/sound.ogg");
        Track track = new OGGTrack(sound);
        Thread tTrack = new Thread(track);
        tTrack.start();
    }


}
