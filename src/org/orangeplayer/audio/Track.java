package org.orangeplayer.audio;

import org.aucom.sound.Speaker;
import org.orangeplayer.audio.org.orangeplayer.audio.interfaces.TrackSetup;

import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;

public abstract class Track implements Runnable, TrackSetup {
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

}

