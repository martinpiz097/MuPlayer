package org.orangeplayer.audio;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class OGGTrack extends Track {

    public OGGTrack(File ftrack) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(ftrack);
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException {
        AudioInputStream soundAis = AudioSystem.getAudioInputStream(ftrack);
        speakerAis = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis);
    }

    @Override
    public void seek(int seconds) throws Exception {

    }

    public static void main(String[] args) throws InterruptedException, UnsupportedAudioFileException, IOException, LineUnavailableException {
        File sound = new File("/home/martin/AudioTesting/sounds/sound.ogg");
        Track track = new OGGTrack(sound);
        Thread tTrack = new Thread(track);
        tTrack.start();
        Thread.sleep(3000);
        track.pause();
        Thread.sleep(3000);
        track.resume();
    }

}
