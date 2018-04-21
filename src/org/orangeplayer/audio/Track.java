package org.orangeplayer.audio;

import org.aucom.sound.Speaker;
import javax.sound.sampled.AudioInputStream;
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


    public Track(File ftrack) {
        this.ftrack = ftrack;
        state = STOPED;
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

    protected abstract void getAudioStream() throws Exception;
    protected void resetStream() throws Exception {
        speakerAis.close();
        getAudioStream();
    };

    @Override
    public boolean isPlaying() {
        return state == PLAYING;
    }

    @Override
    public boolean isPaused() {
        return state == PAUSED;
    }

    @Override
    public boolean isStoped() {
        return state == STOPED;
    }

    @Override
    public boolean isFinished() {
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
    public void resume() {
        play();
    }


    @Override
    public void stop() {
        state = STOPED;
    }

    @Override
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
        }
    }

}

