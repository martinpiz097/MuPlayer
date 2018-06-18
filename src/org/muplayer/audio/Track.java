package org.muplayer.audio;

import org.aucom.sound.Speaker;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.muplayer.audio.codec.DecodeManager;
import org.muplayer.audio.formats.*;
import org.muplayer.audio.interfaces.MusicControls;
import org.muplayer.system.AudioUtil;
import org.muplayer.thread.PlayerHandler;
import org.muplayer.thread.ThreadManager;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static org.muplayer.audio.AudioExtensions.*;
import static org.muplayer.system.TrackStates.*;

public abstract class Track implements Runnable, MusicControls, TrackInfo {
    protected final File dataSource;
    protected Speaker trackLine;
    protected AudioInputStream trackStream;
    protected AudioFileReader audioReader;
    protected AudioTag tagInfo;

    protected byte state;
    protected int available;
    protected int currentSeconds;
    protected float volume;
    protected boolean isMute;
    protected boolean isPlayerLinked;
    //protected TrackState state;

    public static final int BUFFSIZE = 4096;

    public static Track getTrack(File fSound){
        if (!fSound.exists())
            return null;
        Track result = null;
        final String trackName = fSound.getName();
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
            else if (trackName.endsWith(M4A) || trackName.endsWith(AAC)){
                System.out.println("Es mp4");
                result = new M4ATrack(fSound);
            }
            // Por si no tiene formato en el nombre
            /*else {
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
            }*/
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

    public static boolean isValidTrack(String trackPath) {
        Track track = getTrack(trackPath);
        return track != null && track.hasValidTrackLine();
    }

    protected Track(File dataSource)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        System.out.println("Parent: "+dataSource.getParentFile().getName());
        System.out.println("File: "+ dataSource.getName());
        this.dataSource = dataSource;
        //stateCode = STOPPED;
        state = STOPPED;
        currentSeconds = 0;
        initLine();
        isPlayerLinked = PlayerHandler.hasInstance();
        try {
           if (isValidTrack()) {
               byte[] buffer = new byte[(int) dataSource.length()];
               int read = trackStream.read(buffer);
               buffer = null;
               System.out.println("StreamRead: "+read);
               System.out.println("DataSourceLenght: "+dataSource.length());
               initLine();

               System.err.println("TrackAvailable: "+available);
               available = trackStream.available();
               setGain(Player.DEFAULT_VOLUME);
               tagInfo = new AudioTag(dataSource);
           }
           else
               available = -1;
        } catch (TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException e) {
            System.err.println("Problema con caratula en archivo: "+ dataSource.getName());
        }
    }

    protected Track(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    // Ver opcion de usar archivo temporal y leer desde ahi
    protected abstract void loadAudioStream() throws IOException, UnsupportedAudioFileException;

    protected void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (trackStream != null)
            trackStream.close();
        currentSeconds = 0;
        initLine();
    }

    protected void initLine() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        loadAudioStream();
        // Se deja el if porque puede que no se pueda leer el archivo
        // por n razones
        if (trackStream != null) {
            if (trackLine != null)
                trackLine.close();
            try {
                trackLine = new Speaker(trackStream.getFormat());
                trackLine.open();
            } catch (IllegalArgumentException e1) {
                System.err.println("Error: "+e1.getMessage());
            }
        }
        else {
            System.out.println("TrackStream null por tanto line is null");
        }
    }

    protected void closeLine() {
        if (trackLine != null) {
            trackLine.stop();
            trackLine.close();
            trackLine = null;
        }
    }

    protected void closeAll() {
        closeLine();
        currentSeconds = 0;
        // Libero al archivo de audio del bloqueo
        try {
            trackStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected short getSecondsByBytes(int readedBytes) {
        long secs = getDuration();
        long fLen = dataSource.length();
        return (short) ((readedBytes * secs) / fLen);
    }

    protected long transformSecondsInBytes(int seconds) {
        return Math.round((((double)seconds* dataSource.length()) / getDuration()));
    }

    void linkPlayer() {
        isPlayerLinked = true;
    }

    void unlinkPlayer() {
        isPlayerLinked = false;
    }

    public boolean isValidTrack() {
        return trackStream != null && trackLine != null;
    }

    public boolean isTrackFinished() throws IOException {
        return trackStream.read() == -1;
    }

    public AudioInputStream getTrackStream() {
        return trackStream;
    }

    public synchronized boolean hasValidTrackLine() {
        return trackLine != null;
    }

    public synchronized Speaker getTrackLine() {
        return trackLine;
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
            case STOPPED:
                return "Stopped";
            case FINISHED:
                return "Finished";
            case KILLED:
                return "Killed";
            default:
                return "Unknown";
        }

    }

    public File getDataSource() {
        return dataSource;
    }

    public AudioTag getTagInfo() {
        return tagInfo;
    }

    public synchronized int getProgress() {
        return currentSeconds;
    }

    // Ir a un segundo especifico de la cancion
    public void gotoSecond(int second) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        long gtBytes = transformSecondsInBytes(second);
        System.out.println("GoToBytes: "+gtBytes);
        System.out.println("SoundBytes: "+transformSecondsInBytes((int) getDuration()));
        float currentVolume = trackLine.getControl(
                FloatControl.Type.MASTER_GAIN).getValue();
        if (gtBytes > dataSource.length())
            gtBytes = dataSource.length();

        pause();
        resetStream();
        trackLine.setGain(currentVolume);
        play();
        trackStream.skip(gtBytes);
        currentSeconds = second;
    }

    public AudioFileFormat getFileFormat() throws IOException, UnsupportedAudioFileException {
        return audioReader == null ? null : audioReader.getAudioFileFormat(dataSource);
    }

    // Posible motivo de error para mas adelante
    public int getBuffLen() {
        long frameLen = trackStream.getFrameLength();
        return frameLen > 0 ? (int) (frameLen / 1024) : BUFFSIZE;
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
        return state == STOPPED;
    }

    @Override
    public synchronized boolean isFinished() {
        return state == FINISHED;
    }

    @Override
    public boolean isMute() {
        return isMute;
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
        state = STOPPED;
    }

    @Override
    public void finish() {
        state = FINISHED;
        closeAll();
    }

    @Override
    public void seek(int seconds)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException {
        gotoSecond(seconds+getProgress());
        //trackStream.skip(transformSecondsInBytes(seconds));
    }

    @Override
    public float getGain() {
        return volume;
    }

    // -80 to 5.5
    @Override
    public void setGain(float volume) {
        this.volume = volume > 100 ? 100 : (volume < 0 ? 0 : volume);
        trackLine.setGain(AudioUtil.convertVolRangeToLineRange(volume));
    }

    @Override
    public void mute() {
        if (!isMute) {
            isMute = true;
            volume = AudioUtil.convertLineRangeToVolRange(
                    trackLine.getControl(FloatControl.Type.MASTER_GAIN).getValue());
            setGain(0);
        }
    }

    @Override
    public void unmute() {
        if (isMute)
            setGain(volume);
    }

    @Override
    public boolean hasCover() {
        return tagInfo.getCover() != null;
    }

    @Override
    public String getProperty(String key) {
        return tagInfo == null ? null : tagInfo.getTag(key);
    }

    @Override
    public String getProperty(FieldKey key) {
        if (tagInfo == null)
            return null;
        String tag = tagInfo.getTag(key).trim();
        return tag.isEmpty() ? null : tag;
    }

    @Override
    public String getTitle() {
        return getProperty(FieldKey.TITLE);
    }

    @Override
    public String getAlbum() {
        return getProperty(FieldKey.ALBUM);
    }

    @Override
    public String getArtist() {
        //return author == null ? getProperty("artist") : author;
        return getProperty(FieldKey.ARTIST);
    }

    @Override
    public String getDate() {
        return getProperty(FieldKey.YEAR);
    }

    @Override
    public byte[] getCoverData() {
        return hasCover() ? tagInfo.getCover().getBinaryData() : null;
    }

    @Override
    public long getDuration() {
        return tagInfo.getDuration();
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
        File fileCover = new File("/home/martin/AudioTesting/test/cover.jpg");
        try {
            if (fileCover.exists())
                fileCover.delete();
            byte[] coverData = getCoverData();
            if (coverData != null) {
                fileCover.createNewFile();
                Files.write(fileCover.toPath(), coverData, StandardOpenOption.WRITE);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sbInfo.toString();
    }

    @Override
    public void run() {
        try {
            isPlayerLinked = PlayerHandler.hasInstance();
            /*setGain(0);
            System.err.println("---------------------");
            System.err.println("Available: "+trackStream.available());
            System.err.println("FileSize: "+dataSource.length());
            System.err.println("FrameLen: "+trackStream.getFrameLength());
            System.err.println("FrameSize: "+trackStream.getFormat().getFrameSize());
            System.err.println("FrameRate: "+trackStream.getFormat().getFrameRate());
            */
             //System.out.println("FrameSize: "+trackStream.getFrameLength());
            //System.out.println("FrameSize/1024: "+trackStream.getFrameLength()/1024);
            byte[] audioBuffer = new byte[getBuffLen()];
            int read;
            play();

            long ti = System.currentTimeMillis();
            int readedBytes = 0;

            System.out.println("TotalLen: "+ dataSource.length());
            System.out.println("TotalDuration: "+getDuration());

            while (!isFinished()) {
                while (isPlaying()) {
                    try {
                        read = trackStream.read(audioBuffer);
                        readedBytes+=read;
                        if (ThreadManager.hasOneSecond(ti)) {
                            //currentSeconds=(getSecondsByBytes(readedBytes));
                            currentSeconds++;
                            ti = System.currentTimeMillis();
                        }
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
            if (isFinished() && PlayerHandler.hasInstance() && isPlayerLinked)
                PlayerHandler.getPlayer().loadNextTrack();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }


}

