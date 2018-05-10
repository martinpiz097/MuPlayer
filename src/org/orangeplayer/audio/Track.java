package org.orangeplayer.audio;

import org.aucom.sound.Speaker;
import org.orangeplayer.audio.codec.DecodeManager;
import org.orangeplayer.audio.formats.*;
import org.orangeplayer.audio.interfaces.MusicControls;
import org.orangeplayer.thread.PlayerHandler;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.orangeplayer.audio.AudioExtensions.*;
import static org.orangeplayer.audio.TrackStates.*;

public abstract class Track implements Runnable, MusicControls {
    protected final File ftrack;
    protected Speaker trackLine;
    protected AudioInputStream speakerAis;
    protected AudioFileReader audioReader;

    protected byte state;
    //protected TrackState state;

    public static final int BUFFSIZE = 4096;

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
            else if (trackName.endsWith(WAVE) || trackName.endsWith(AU)
                    || trackName.endsWith(AU) || trackName.endsWith(SND)
                    || trackName.endsWith(AIFF) || trackName.endsWith(AIFC))
                result = new PCMTrack(fSound);
            else if (trackName.endsWith(M4A)){
                System.out.println("Es mp4");
                result = new MP4Track(fSound);
            }
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
                        // Tambien sirve para los mp4
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
        //stateCode = STOPED;
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

    public AudioInputStream getTrackStream() {
        return speakerAis;
    }

    public Speaker getTrackLine() {
        return trackLine;
    }


    public File getTrackFile() {
        return ftrack;
    }

    /*public TrackStates getState() {
        return stateCode;
    }*/

    public String getStateToString() {
        switch (state) {
            case PLAYING:
                return "Playing";
            case PAUSED:
                return "Paused";
            case STOPED:
                return "Stoped";
            case FINISHED:
                return "Finished";
            case KILLED:
                return "Killed";
                default:
                    return "Unknown";
        }
        // else if (state instanceof S)
        //    return "Seeked";
    }

    // Ver opcion de usar archivo temporal y leer desde ahi
    protected abstract void loadAudioStream() throws IOException, UnsupportedAudioFileException;

    protected void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (speakerAis != null)
            speakerAis.close();
        initLine();
    }

    protected void initLine() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        loadAudioStream();
        // Se deja el if porque puede que no se pueda leer el archivo
        // por n razones
        if (speakerAis != null) {
            if (trackLine != null)
                trackLine.close();
            trackLine = new Speaker(speakerAis.getFormat());
            trackLine.open();
        }
    }

    protected void closeLine() {
        if (trackLine != null) {
            trackLine.stop();
            trackLine.close();
            trackLine = null;
        }
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

    protected void closeAll() {
        closeLine();
        // Libero al archivo de audio del bloqueo
        try {
            speakerAis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public boolean isKilled() {
        return state == KILLED;
    }

    public void kill() {
        state = KILLED;
        closeAll();
    }

    @Override
    public void play() {
        //if (stateCode == PAUSED)
        //    ThreadManager.unfreezeThread(Thread.currentThread());
        state = PLAYING;
    }

    @Override
    public void pause() {
        state = PAUSED;
        //ThreadManager.freezeThread(Thread.currentThread());
    }

    @Override
    public void resumeTrack() {
        play();
    }

    @Override
    public synchronized void stopTrack() {
        state = STOPED;
    }

    @Override
    public void finish() {
        state = FINISHED;
        closeAll();
    }

    public abstract void seek(int seconds) throws Exception;
    // -80 to 5.5
    @Override
    public void setGain(float volume) {
        float vol = (float) (-80.0+(0.855*volume));
        trackLine.setGain(vol);
    }

    public AudioFileFormat getFileFormat() throws IOException, UnsupportedAudioFileException {
        return audioReader == null ? null : audioReader.getAudioFileFormat(ftrack);
    }

    // Info

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
            while (!isFinished() && !isKilled()) {
                while (isPlaying()) {
                    try {
                        read = speakerAis.read(audioBuffer);
                        if (read == -1) {
                            finish();
                            break;
                        }
                        trackLine.playAudio(audioBuffer);
                    } catch (IndexOutOfBoundsException e) {
                        finish();
                    }
                }
                if (isStoped()) {
                    resetStream();
                }
            }
            System.out.println("Track completed!");
            if (isFinished() && PlayerHandler.hasInstance()) {
                PlayerHandler.getInstance().playNext();
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}

