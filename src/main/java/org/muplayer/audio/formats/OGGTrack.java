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

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        audioReader = new JorbisAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(dataSource);
        trackStream = DecodeManager.decodeToPcm(soundAis);
        //trackStream = DecodeManager.decodeToPcm(soundAis.getFormat(), soundAis);

    }

    @Override
    public void seek(double seconds) throws IOException {
        mute();
        super.seek(seconds);
        unmute();
    }

    public void gotoSecond(double second) throws IOException,
            LineUnavailableException, UnsupportedAudioFileException {
        double progress = getProgress();
        if (second >= progress) {
            int gt = (int) Math.round(second-getProgress());
            seek(gt);
        }
        else if (second < progress) {
            stopTrack();
            resumeTrack();
            secsSeeked = 0;
            seek(second);
        }
    }

}
