package org.orangeplayer.audio;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import org.aucom.sound.Speaker;
import org.orangeplayer.audio.interfaces.MusicControls;
import org.orangeplayer.audio.trackstypes.FlacTrack;
import org.orangeplayer.audio.trackstypes.MP3Track;
import org.orangeplayer.audio.trackstypes.OGGTrack;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.orangeplayer.audio.TrackFormat.*;
import static org.orangeplayer.audio.TrackState.*;

public abstract class Track implements Runnable, MusicControls {
    protected final File ftrack;
    protected Speaker trackLine;
    protected AudioInputStream speakerAis;
    protected AudioFileReader audioReader;
    protected byte state;

    protected static final int BUFFSIZE = 4096;

    public static Track getTrack(File fSound){
        Track result = null;
        final String trackName = fSound.getName();

        try {
            if (trackName.endsWith(MPEG))
                result = new MP3Track(fSound);
            else if (trackName.endsWith(OGG))
                result = new OGGTrack(fSound);
            else if (trackName.endsWith(FLAC))
                result = new FlacTrack(fSound);
            // Por si no tiene formato en el nombre
            else {
                try {
                    VorbisFile vorbisTest = new VorbisFile(fSound.getCanonicalPath());
                    result = new OGGTrack(fSound);
                    System.out.println("OGGAis: "+result.getSpeakerAis());
                    System.out.println("OGGFileFormat: "+result.getFileFormat());
                } catch (JOrbisException e) {
                    try {
                        result = new MP3Track(fSound);
                    } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e1) {
                        //System.out.println(e.getMessage());
                        result = new FlacTrack(fSound);
                        if (result.getSpeakerAis() == null)
                            result = null;
                        System.out.println("SpeakerAis: "+result.getSpeakerAis());
                    }
                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                    //System.out.println(e.getMessage());
                    result = null;
                }
            }
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Track getTrack(String trackPath) {
        return getTrack(new File(trackPath));
    }

    public static boolean isValidTrack(File fTrack) {
        return getTrack(fTrack) != null;
    }


    protected Track(File ftrack) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException {
        System.out.println("File: "+ftrack.getPath());
        this.ftrack = ftrack;
        state = STOPED;
        getAudioStream();
        if (speakerAis != null) {
            trackLine = new Speaker(speakerAis.getFormat());
            trackLine.open();
        }

    }

    protected Track(String trackPath)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this(new File(trackPath));
    }


    public boolean isTrackFinished() throws IOException {
        return speakerAis.read() == -1;
    }

    protected AudioInputStream getSpeakerAis() {
        return speakerAis;
    }

    public File getTrackFile() {
        return ftrack;
    }

    public byte getState() {
        return state;
    }

    public String getStateToString() {
        if (state == PLAYING)
            return "Playing";
        else if (state == PAUSED)
            return "Paused";
        else if (state == STOPED)
            return "Stoped";
        else if (state == SEEKED)
            return "Seeked";
        else
            return "Unknown";
    }

    protected abstract void getAudioStream() throws IOException,
            UnsupportedAudioFileException, LineUnavailableException;
    protected void resetStream()
            throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        speakerAis.close();
        getAudioStream();
    };

    protected void closeLine() {
        trackLine.stop();
        trackLine.close();
        trackLine = null;
    }

    protected String getProperty(String key) {
        Map<String, Object> formatProper = null;
        try {
            formatProper = getFileFormat().properties();
            if (formatProper != null) {
                Object get = formatProper.get(key);
                return get == null ? null : get.toString();
            }
            else
                return null;
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized boolean isPlaying() {
        return state == PLAYING;
    }

    @Override
    public synchronized boolean isPaused() {
        return state == PAUSED;
    }

    @Override
    public synchronized boolean isStoped() {
        return state == STOPED;
    }

    @Override
    public synchronized boolean isFinished() {
        return state == FINISHED;
    }

    @Override
    public void play() {
        state = PLAYING;
    }

    @Override
    public void pause() {
        state = PAUSED;
    }

    @Override
    public void resumeTrack() {
        play();
    }

    @Override
    public void stopTrack() {
        state = STOPED;
    }

    @Override
    public void finish() {
        state = FINISHED;
        closeLine();
    }

    public abstract void seek(int seconds) throws Exception;

    // -80 to 6
    @Override
    public void setGain(float volume) {
        float vol = (float) (-80.0+(0.86*volume));
        trackLine.setGain(vol);
    }

    public AudioFileFormat getFileFormat() throws IOException, UnsupportedAudioFileException {
        return audioReader.getAudioFileFormat(ftrack);
    }

    public String getTitle() {
        return getProperty("title");
    }

    public String getAlbum() {
        return getProperty("album");
    }

    public String getAuthor() {
        return getProperty("author");
    }

    public String getDate() {
        return getProperty("date");
    }

    public long getDuration() {
        String strDuration = getProperty("duration");
        return strDuration == null ? 0 : Long.parseLong(strDuration);
    }

    public String getDurationAsString() {
        long sec = getDuration() / 1000/1000;
        long min = sec / 60;
        sec = sec-(min*60);
        return new StringBuilder().append(min)
                .append(':').append(sec < 10 ? '0'+sec:sec).toString();
    }

    // Testing
    public String getInfoSong() {
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append(getTitle()).append('\n');
        sbInfo.append(getAlbum()).append('\n');
        sbInfo.append(getAuthor()).append('\n');
        sbInfo.append(getDate()).append('\n');
        sbInfo.append(getDurationAsString()).append('\n');
        sbInfo.append("--------------------");
        return sbInfo.toString();
    }

    @Override
    public void run() {
        try {
            byte[] audioBuffer = new byte[BUFFSIZE];
            int read;
            play();

            // For testing
            //speakerAis.skip(8000000);

            while (!isFinished()) {
                while (isPlaying()) {
                    try {
                        read = speakerAis.read(audioBuffer);
                        if (read == -1) {
                            finish();
                            System.out.println("Track finished");
                            break;
                        }
                        trackLine.playAudio(audioBuffer);
                    }catch (IndexOutOfBoundsException e1) {
                        finish();
                        System.out.println("Track finished");
                        break;
                    }
                }
                if (isStoped()) {
                    resetStream();
                    while (isStoped()) {
                    }
                }

                System.out.print("");
            }
            System.out.println("Track completed!");

        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File img = new File("/home/martin/AudioTesting/audio/flac.flac");
    }

}

