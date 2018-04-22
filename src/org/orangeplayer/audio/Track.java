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

    public static Track getTrack(File fSound){
        Track result;
        try {
            VorbisFile vorbisTest = new VorbisFile(fSound.getCanonicalPath());
            result = new OGGTrack(fSound);
        } catch (JOrbisException e) {
            try {
                result = new MP3Track(fSound);
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e1) {
                //System.out.println(e.getMessage());
                result = null;
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            //System.out.println(e.getMessage());
            result = null;
        }
        return result;
    }

    public static boolean isValidTrack(File fTrack) {
        return getTrack(fTrack) != null;
    }


    protected Track(File ftrack) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException {
        System.out.println("Track: "+ftrack.getPath());
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

    @Override
    public void run() {
        try {
            byte[] audioBuffer = new byte[BUFFSIZE];
            int read;
            play();

            while (!isFinished()) {
                while (isPlaying()) {
                    read = speakerAis.read(audioBuffer);
                    trackLine.playAudio(audioBuffer);
                    if (read == -1)
                        finish();
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

