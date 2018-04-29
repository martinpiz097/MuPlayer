package org.orangeplayer.audio.tracksFormats;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.orangeplayer.audio.Track;
import org.orangeplayer.audio.codec.DecodeManager;

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

    public boolean isValidTrack() {
        return speakerAis != null;
    }

    @Override
    protected void getAudioStream() throws IOException, UnsupportedAudioFileException {
        // Ver si se escucha mejor en ogg utilizando la logica de mp3
        audioReader = new MpegAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(ftrack);
        AudioFormat baseFormat = soundAis.getFormat();
        speakerAis = DecodeManager.decodeMpegToPcm(baseFormat, soundAis);
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

        System.out.println(AudioSystem.getAudioFileFormat(sound).toString());

        Track track = new MP3Track(sound);
        Thread tTrack = new Thread(track);
        //tTrack.start();
        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sound);
        System.out.println(AudioSystem.isConversionSupported(
                AudioFormat.Encoding.PCM_SIGNED, fileFormat.getFormat()));
        //Thread.sleep(3000);
        //track.pause();
        //Thread.sleep(3000);
        //track.resume();
    }


}
