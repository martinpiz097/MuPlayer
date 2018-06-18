package org.muplayer.audio.formats;

import org.muplayer.audio.Track;
import org.muplayer.audio.codec.DecodeManager;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class OGGTrack extends Track {

    //private VorbisFile infoFile;
    //private Comment soundComments;

    public OGGTrack(File ftrack)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(ftrack);
        //infoFile = new VorbisFile(ftrack.getCanonicalPath());
        //soundComments = infoFile.getComment(0);
    }

    public OGGTrack(String trackPath) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    /*public boolean isValidTrack() {
        return trackStream != null;
    }*/

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        audioReader = new JorbisAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(dataSource);
        trackStream = DecodeManager.decodeToPcm(soundAis);
        //trackStream = DecodeManager.decodeToPcm(soundAis.getFormat(), soundAis);
    }

    /*@Override
    public long getDuration() {
        try {
            return (long) new VorbisFile(dataSource.getCanonicalPath()).time_total(0);
        } catch (JOrbisException | IOException e) {
            return -1;
        }
    }

    @Override
    public String getDurationAsString() {
        long sec = getDuration();
        long min = sec / 60;
        sec = sec-(min*60);
        return new StringBuilder().append(min)
                .append(':').append(sec < 10 ? '0'+sec:sec).toString();
    }*/

    /*@Override
    protected String getProperty(String key) {
        return soundComments == null ? null : soundComments.query(key);
    }*/

    /*public static void main(String[] args) throws
            IOException, LineUnavailableException, JOrbisException, UnsupportedAudioFileException {
        File sound = new File("/home/martin/AudioTesting/audio/sound.ogg");
        Track track = new OGGTrack(sound);
        Thread tTrack = new Thread(track);
        tTrack.start();
        System.out.println(track.getInfoSong());
        /*VorbisFile vorbisFile = new VorbisFile(sound.getCanonicalPath());
        Comment comment = vorbisFile.getComment(0);
        System.out.println("Bitrate: "+vorbisFile.bitrate(0));
        System.out.println("TimeTotal: "+vorbisFile.time_total(0));
        System.out.println("PCMTotal: "+vorbisFile.pcm_total(0));
        System.out.println("RawTotal: "+vorbisFile.raw_total(0));
        System.out.println("Size: "+sound.length());
        System.out.println(vorbisFile.time_total(0)%60);
    }
    */

}
