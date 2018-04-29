package org.orangeplayer.audio;

import com.jcraft.jorbis.JOrbisException;
import org.aucom.sound.Speaker;
import org.orangeplayer.audio.codec.DecodeManager;
import org.orangeplayer.audio.interfaces.MusicControls;
import org.orangeplayer.audio.tracksFormats.FlacTrack;
import org.orangeplayer.audio.tracksFormats.MP3Track;
import org.orangeplayer.audio.tracksFormats.OGGTrack;
import org.orangeplayer.audio.tracksFormats.PCMTrack;

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

    // La idea es dejar elalgoritmo antiguo para
    // comparar velocidades

    public static Track getTrack(File fSound){
        if (!fSound.exists())
            return null;
        Track result = null;
        String trackName = fSound.getName();
        try {
            if (trackName.endsWith(MPEG))
                result = new MP3Track(fSound);
            else if (trackName.endsWith(OGG))
                result = new OGGTrack(fSound);
            else if (trackName.endsWith(FLAC))
                result = new FlacTrack(fSound);
            else if (trackName.endsWith(WAVE))
                result = new PCMTrack(fSound);
            // Por si no tiene formato en el nombre
            else {
                AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(fSound);
                // Es ogg, aac o mp3
                if (AudioSystem.isConversionSupported(
                        AudioFormat.Encoding.PCM_SIGNED, fileFormat.getFormat())) {
                    // no es mp3
                    if (DecodeManager.isVorbis(fSound))
                        result = new OGGTrack(fSound);
                    else
                        result = new PCMTrack(fSound);
                } else
                    result = new MP3Track(fSound);

                // Ver si es mp3
                // Podria ser que por reflection revise entre todas las subclases
                // si una es compatible con el archivo en cuestion
            }
        } catch (UnsupportedAudioFileException e) {
            // Es flac
            if (DecodeManager.isFlac(fSound)) {
                try {
                    result = new FlacTrack(fSound);
                    if (!result.isValidTrack())
                        result = null;
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e1) {
                    e1.printStackTrace();
                }

            }
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Track getTrack(String trackPath) {
        return getTrack(new File(trackPath));
    }

    protected Track(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        System.out.println("File: "+ftrack.getPath());
        this.ftrack = ftrack;
        state = STOPED;
        initLine();
    }

    protected Track(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    public boolean isValidTrack() {
        return speakerAis != null;
    }

    public boolean isTrackFinished() throws IOException {
        return speakerAis.read() == -1;
    }

    public AudioInputStream getSpeakerAis() {
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

    protected abstract void getAudioStream() throws IOException, UnsupportedAudioFileException;

    protected void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (speakerAis != null)
            speakerAis.close();
        initLine();
    }

    protected void initLine() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        getAudioStream();
        if (speakerAis != null) {
            if (trackLine != null)
                trackLine.close();
            trackLine = new Speaker(speakerAis.getFormat());
            trackLine.open();
        }
    }

    protected void closeLine() {
        trackLine.stop();
        trackLine.close();
        trackLine = null;
    }

    protected String getProperty(String key) {
        Map<String, Object> formatProper = null;
        try {
            AudioFileFormat fileFormat = getFileFormat();
            if (fileFormat != null) {
                formatProper = getFileFormat().properties();
                if (formatProper != null) {
                    Object get = formatProper.get(key);
                    return get == null ? null : get.toString();
                }
                else
                    return null;
            }
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
        return audioReader == null ? null : audioReader.getAudioFileFormat(ftrack);
    }

    public String getTitle() {
        String proper = getProperty("title");
        return proper == null ? ftrack.getName() : proper;
    }

    public String getAlbum() {
        return getProperty("album");
    }

    public String getArtist() {
        String author = getProperty("author");
        return author == null ? getProperty("artist") : author;
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
        sbInfo.append(getArtist()).append('\n');
        sbInfo.append(getDate()).append('\n');
        sbInfo.append(getDurationAsString()).append('\n');
        sbInfo.append("------------------------");
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

        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, LineUnavailableException, JOrbisException, UnsupportedAudioFileException {
        //File img = new File("/home/martin/AudioTesting/audio/flac.flac");
        File sound = new File("/home/martin/AudioTesting/audio/au.mp3");
        File img = new File("/home/martin/AudioTesting/audio/folder.jpg");

        /*TAudioFileFormat fileFormat = (TAudioFileFormat) AudioSystem.getAudioFileFormat(sound);
        System.out.println(fileFormat.getType());
        System.out.println(fileFormat.getFormat().getEncoding().toString());
*/

        //AudioInputStream speakerAis = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, soundAis);
    }

}

