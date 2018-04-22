package org.orangeplayer.audio;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;

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
        AudioInputStream soundAis = AudioSystem.getAudioInputStream(ftrack);
        speakerAis = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis);
    }

    @Override
    public void seek(int seconds) throws Exception {

    }

    public static void main(String[] args) throws
            InterruptedException, UnsupportedAudioFileException,
            IOException, LineUnavailableException, JOrbisException {
        File mp3 = new File("/home/martin/AudioTesting/audio/au.mp3");
        File ogg = new File("/home/martin/AudioTesting/audio/sound.ogg");
        VorbisFile file1 = new VorbisFile(mp3.getCanonicalPath());
        VorbisFile file2 = new VorbisFile(ogg.getCanonicalPath());
    }


}
