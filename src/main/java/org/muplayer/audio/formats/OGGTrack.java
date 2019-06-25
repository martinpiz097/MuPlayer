package org.muplayer.audio.formats;

import org.aucom.sound.Speaker;
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

    private AudioInputStream createAudioStream() throws IOException, UnsupportedAudioFileException {
        audioReader = new JorbisAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(dataSource);
        return DecodeManager.decodeToPcm(soundAis);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        trackStream = createAudioStream();
    }

    private Speaker createLine() throws LineUnavailableException {
        Speaker line = new Speaker(trackStream.getFormat());
        line.open();
        setGain(volume);
        return line;
    }

    protected void initLine() throws LineUnavailableException {
        // Se deja el if porque puede que no se pueda leer el archivo
        // por n razones
        if (trackStream != null) {
            if (trackLine != null) {
                trackLine.stop();
                trackLine.close();
            }
            try {
                trackLine = createLine();
            } catch (IllegalArgumentException e1) {
                System.err.println("Error: "+e1.getMessage());
            }
        }
        else
            System.out.println("TrackStream & TrackLine null");
    }

    @Override
    public void seek(double seconds) throws IOException {
        pause();
        super.seek(seconds);
        resumeTrack();
    }

    @Override
    public void gotoSecond(double second) throws
            IOException, LineUnavailableException, UnsupportedAudioFileException {
        double progress = getProgress();
        if (second >= progress) {
            int duration = (int) getDuration();
            if (second > duration)
                second = duration;
            int gt = (int) Math.round(second-getProgress());
            seek(gt);
        }
        else if (second < progress) {
            if (isPlaying()) {
                if (second < 0)
                    second = 0;
                AudioInputStream newStream = createAudioStream();
                Speaker newLine = createLine();
                suspend();
                closeAllStreams();
                trackStream = newStream;
                trackLine = newLine;
                seek(second);
                resumeTrack();
            }
        }
    }

}
