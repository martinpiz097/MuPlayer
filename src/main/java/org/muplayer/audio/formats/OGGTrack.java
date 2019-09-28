package org.muplayer.audio.formats;

import org.aucom.sound.Speaker;
import org.muplayer.audio.Track;
import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.system.AudioUtil;
import org.tritonus.sampled.file.jorbis.JorbisAudioFileReader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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

    public OGGTrack(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream);
    }

    public OGGTrack(File dataSource, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(dataSource, player);
    }

    public OGGTrack(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        super(inputStream, player);
    }

    public OGGTrack(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath, player);
    }

    private AudioInputStream createAudioStream() throws IOException, UnsupportedAudioFileException {
        audioReader = new JorbisAudioFileReader();
        AudioInputStream soundAis = AudioUtil.instanceStream(audioReader, source);
        return DecodeManager.decodeToPcm(soundAis);
    }

    private Speaker createLine() throws LineUnavailableException {
        Speaker line = new Speaker(trackStream.getFormat());
        line.open();
        setGain(volume);
        return line;
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        trackStream = createAudioStream();
    }

    @Override
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
