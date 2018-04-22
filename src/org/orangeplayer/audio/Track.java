package org.orangeplayer.audio;

import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import org.aucom.sound.Speaker;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public abstract class Track implements Runnable {
    protected File ftrack;
    protected Speaker trackLine;
    protected AudioInputStream speakerAis;
    protected byte state;

    // states
    public static byte PLAYING = 1;
    public static byte PAUSED = 2;
    public static byte STOPED = 3;
    public static byte SEEKED = 4;
    public static byte FINISHED = 5;

    protected static int BUFFSIZE = 4096;


    private static final String MPEG = ".mp3";
    private static final String OGG = ".ogg";
    private static final String AAC = ".aac";
    private static final String FLAC = ".flac";
    //private static final String M4A = ".m4a";

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
                } catch (JOrbisException e) {
                    try {
                        result = new MP3Track(fSound);
                    } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e1) {
                        //System.out.println(e.getMessage());
                        result = new FlacTrack(fSound);
                        if (result.getSpeakerAis() == null)
                            result = null;
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
        trackLine = new Speaker(speakerAis.getFormat());
        trackLine.open();

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
    protected void resetStream() throws Exception {
        speakerAis.close();
        getAudioStream();
    };

    public boolean isPlaying() {
        return state == PLAYING;
    }

    public boolean isPaused() {
        return state == PAUSED;
    }

    public boolean isStoped() {
        return state == STOPED;
    }

    public boolean isFinished() {
        return state == FINISHED;
    }

    public void play() {
        state = PLAYING;
    }

    public void pause() {
        state = PAUSED;
    }

    public void resume() {
        play();
    }

    public void stop() {
        state = STOPED;
    }

    public void finish() {
        state = FINISHED;
    }

    public abstract void seek(int seconds) throws Exception;

    public void setGain(float volume) {
        trackLine.setGain(volume);
    }

    @Override
    public void run() {
        try {
            byte[] audioBuffer = new byte[BUFFSIZE];
            int read;
            play();

            while (!isFinished()) {
                while (isPlaying()) {
                    read = speakerAis.read(audioBuffer);
                    if (read == -1) {
                        finish();
                        System.out.println("Track finished");
                        break;
                    }
                    trackLine.playAudio(audioBuffer);
                }
                if (isStoped()) {
                    resetStream();
                    while (isStoped()) {
                    }
                }

                System.out.print("");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

