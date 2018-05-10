package org.orangeplayer.audio.formats;

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
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        // Ver si se escucha mejor en ogg utilizando la logica de mp3
        audioReader = new MpegAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(ftrack);
        AudioFormat baseFormat = soundAis.getFormat();
        speakerAis = DecodeManager.decodeMpegToPcm(baseFormat, soundAis);
    }

    // Una vez obtenidas todas las duraciones por formato
    // el metodo seek sera universal
    @Override
    public void seek(int seconds) {
        long secs = getDuration() / 1000 / 1000;
        long fLen = ftrack.length();
        long seekLen = (seconds * fLen) / secs;

        try {
            speakerAis.skip(seekLen);
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
