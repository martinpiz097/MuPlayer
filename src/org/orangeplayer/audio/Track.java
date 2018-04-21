package org.orangeplayer.audio;

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

    public static Track getTrack(){return null;}


    public Track(File ftrack) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException {
        this.ftrack = ftrack;
        state = STOPED;
        getAudioStream();
        trackLine = new Speaker(speakerAis.getFormat());
        trackLine.open();
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

