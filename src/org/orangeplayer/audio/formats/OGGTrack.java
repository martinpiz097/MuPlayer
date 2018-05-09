package org.orangeplayer.audio.formats;

import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import org.orangeplayer.audio.Track;
import org.orangeplayer.audio.codec.DecodeManager;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class OGGTrack extends Track {

    private VorbisFile infoFile;
    private Comment soundComments;

    public OGGTrack(File ftrack)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(ftrack);
        try {
            infoFile = new VorbisFile(ftrack.getCanonicalPath());
            soundComments = infoFile.getComment(0);
        } catch (JOrbisException e) {
            System.out.println("Excepcion vorbis");
        } catch (IOException e) {
            System.out.println("Excepcion IO");
        }
    }

    public OGGTrack(String trackPath) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    public boolean isValidTrack() {
        return speakerAis != null;
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        audioReader = new JorbisAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(ftrack);
        speakerAis = DecodeManager.decodeToPcm(soundAis);
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

    @Override
    protected String getProperty(String key) {
        return soundComments == null ? null : soundComments.query(key);
    }

    public static void main(String[] args) throws
            IOException, LineUnavailableException, JOrbisException, UnsupportedAudioFileException {
        File sound = new File("/home/martin/AudioTesting/audio/sound.ogg");
        Track track = new OGGTrack(sound);
        Thread tTrack = new Thread(track);
        //tTrack.start();
        System.out.println(track.getInfoSong());
        Comment comment = new VorbisFile(sound.getCanonicalPath()).getComment(0);
    }


}
