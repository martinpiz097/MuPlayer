package org.orangeplayer.audio.trackstypes;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.orangeplayer.audio.Track;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MP3Track extends Track {

    public MP3Track(File ftrack) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException {
        super(ftrack);
    }

    public MP3Track(String trackPath)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(trackPath);
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException {
        audioReader = new MpegAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(ftrack);
        AudioFormat baseFormat = soundAis.getFormat();
        System.out.println(baseFormat.getSampleRate());
        System.out.println(baseFormat.getFrameRate());
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);

        speakerAis = AudioSystem.getAudioInputStream(decodedFormat, soundAis);
    }

    @Override
    public void seek(int seconds) {
        // Testing skip bytes
        try {
            speakerAis.skip(seconds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Libreria AAC genera problemas con archivos mp3 y ogg
    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File sound = new File("/home/martin/AudioTesting/audio/au.mp3");
        //System.out.println(new MpegAudioFileReader().getAudioFileFormat(sound).getType().toString());
        //System.out.println(new JorbisAudioFileReader().getAudioFileFormat(sound).getType().toString());
        //System.out.println(AudioSystem.getAudioFileFormat(sound).getType().toString());

        Track track = new MP3Track(sound);
        Thread tTrack = new Thread(track);
        tTrack.start();
        //Thread.sleep(3000);
        //track.pause();
        //Thread.sleep(3000);
        //track.resume();
    }


}
