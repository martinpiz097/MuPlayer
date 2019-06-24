package org.muplayer.audio;

import org.aucom.sound.Speaker;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.muplayer.audio.formats.*;
import org.muplayer.audio.info.AudioTag;
import org.muplayer.audio.interfaces.MusicControls;
import org.muplayer.audio.model.TrackInfo;
import org.muplayer.audio.util.Time;
import org.muplayer.audio.util.TimeFormatter;
import org.muplayer.system.AudioUtil;
import org.orangelogger.sys.Logger;
import org.muplayer.thread.PlayerHandler;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;

import static org.muplayer.audio.util.AudioExtensions.*;
import static org.muplayer.system.TrackStates.*;

public abstract class Track extends Thread implements MusicControls, TrackInfo {
    protected volatile File dataSource;
    protected volatile Speaker trackLine;
    protected volatile AudioInputStream trackStream;
    protected volatile AudioFileReader audioReader;
    protected volatile AudioTag tagInfo;

    protected volatile byte state;
    //protected volatile int available;
    protected volatile double secsSeeked;
    //protected volatile double currentTime;
    //protected volatile long readedBytes;
    protected volatile double bytesPerSecond;
    protected volatile float volume;
    protected volatile boolean isMute;

    //protected ByteBuffer playingBuffer;
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
                    || trackName.endsWith(SND) || trackName.endsWith(AIFF)
                    || trackName.endsWith(AIFC))
                result = new PCMTrack(fSound);
            else if (trackName.endsWith(M4A) || trackName.endsWith(AAC))
                result = new M4ATrack(fSound);
            /*else if (trackName.endsWith(SPEEX))
                result = new SpeexTrack(fSound);*/
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | InvalidAudioFrameException e) {
            /*Logger.getLogger(Track.class,
                    e.getClass().getSimpleName(), e.getMessage()).error();*/
        }

        return result;
    }

    public static Track getTrack(String trackPath) {
        return getTrack(new File(trackPath));
    }

    public static boolean isValidTrack(String trackPath) {
        return isValidTrack(new File(trackPath));
    }

    public static boolean isValidTrack(File track) {
        boolean isSupported = AudioUtil.isSupported(track);
        return isSupported /*&& getTrack(track).isValidTrack()*/;
    }

    protected Track(File dataSource)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.dataSource = dataSource;
        state = STOPPED;
        secsSeeked = 0;
        volume = Player.DEFAULT_VOLUME;
        initAll();
        try {
           if (isValidTrack())
               tagInfo = new AudioTag(dataSource);
        } catch (TagException | ReadOnlyFileException | InvalidAudioFrameException | CannotReadException e) {
            Logger.getLogger(this, e.getClass().getSimpleName(), e.getMessage()).error();
        }
        setPriority(MAX_PRIORITY);
    }

    protected Track(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath));
    }

    // Ver opcion de usar archivo temporal y leer desde ahi
    protected abstract void loadAudioStream() throws IOException, UnsupportedAudioFileException;

    protected void resetLine() throws LineUnavailableException {
        trackLine.stop();
        trackLine.open();
    }

    public void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        closeAllStreams();
        initAll();
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
                trackLine = new Speaker(trackStream.getFormat());
                trackLine.open();
                setGain(volume);
            } catch (IllegalArgumentException e1) {
                System.err.println("Error: "+e1.getMessage());
            }
        }
        else
            System.out.println("TrackStream & TrackLine null");
    }

    protected void initAll() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        loadAudioStream();
        initLine();

    }

    protected void closeLine() {
        if (trackLine != null) {
            trackLine.stop();
            trackLine.close();
            trackLine = null;
        }
    }

    public void closeAllStreams() {
        closeLine();
        secsSeeked = 0;
        // Libero al archivo de audio del bloqueo
        try {
            trackStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*protected short getSecondsByBytes(int readedBytes) {
        long secs = getDuration();
        long fLen = dataSource.length();
        return (short) ((readedBytes * secs) / fLen);
    }*/

    /*protected long transformSecondsInBytes(double seconds) {
        long secToBytes = Math.round(((seconds* dataSource.length()) / getDuration()));
        //System.out.println("SecToBytes: "+secToBytes);
        return secToBytes;
    }*/

    /*protected long transformSecondsInBytes(int seconds, long soundSize) {
        long secToBytes = Math.round((((double)seconds* soundSize) / getDuration()));
        System.out.println("SecToBytes: "+secToBytes);
        return secToBytes;
    }*/

    protected double getSecondsPosition() {
        if (trackLine == null)
            return 0;
        return ((double)trackLine.getDriver().getMicrosecondPosition()) / 1000000;
    }

    // Posible motivo de error para mas adelante
    protected int getBuffLen() {
        long frameLen = trackStream == null ? BUFFSIZE : trackStream.getFrameLength();
        //Logger.getLogger(this, "FrameLenght: "+frameLen).rawInfo();
        return frameLen > 0 ? (int) (frameLen / 1024) : BUFFSIZE;
    }

    public boolean isValidTrack() {
        return trackStream != null && trackLine != null;
    }

    public boolean hasPlayerAssociated() {
        return PlayerHandler.hasInstance();
    }

    public double getBytesPerSecond() {
        return bytesPerSecond;
    }

    public AudioInputStream getDecodedStream() {
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

    public byte getTrackState() {
        return state;
    }

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

    @Override
    public synchronized double getProgress() {
        return getSecondsPosition()+secsSeeked;
    }

    public synchronized String getFormattedProgress() {
        return TimeFormatter.format((long) getProgress());
    }

    // Ir a un segundo especifico de la cancion
    // inhabiliado si aplico freeze al pausar

    public AudioFileFormat getFileFormat() throws IOException, UnsupportedAudioFileException {
        return audioReader == null ? null : audioReader.getAudioFileFormat(dataSource);
    }

    public AudioFormat getAudioFormat() {
        return trackStream.getFormat();
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
    public synchronized boolean isStopped() {
        return state == STOPPED;
    }

    @Override
    public synchronized boolean isFinished() {
        return state == FINISHED;
    }

    public synchronized boolean isKilled() {
        return state == KILLED;
    }

    @Override
    public boolean isMute() {
        return isMute;
    }

    @Override
    public void play() {
        if (isAlive())
            state = PLAYING;
    }

    @Override
    public void pause() {
        if (isPlaying()) {
            suspend();
            state = PAUSED;
        }
    }

    @Override
    public void resumeTrack() {
        if (isAlive() && (isPaused() || isStopped())) {
            resume();
            play();
        }
    }

    @Override
    public synchronized void stopTrack()
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (isAlive() && (isPlaying() || isPaused())) {
            if (isPlaying())
                suspend();
            resetStream();
            state = STOPPED;
            secsSeeked = 0;

        }
    }

    @Override
    public void finish() {
        state = FINISHED;
        closeAllStreams();
    }

    void kill() {
        state = KILLED;
        closeAllStreams();
    }

    // en este caso pasan a ser seconds
    @Override
    public void seek(double seconds)
            throws IOException {
        if (seconds == 0)
            return;
        secsSeeked+=seconds;
        AudioFormat audioFormat = getAudioFormat();
        float frameRate = audioFormat.getFrameRate();
        int frameSize = audioFormat.getFrameSize();
        double framesToSeek = frameRate*seconds;
        long seek = Math.round(framesToSeek*frameSize);
        trackStream.skip(seek);
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
            if (second < 0)
                second = 0;
            stopTrack();
            seek(second);
            resumeTrack();
        }
    }

    @Override
    public float getGain() {
        return isMute ? 0 : volume;
    }

    // -80 to 5.5
    @Override
    public void setGain(float volume) {
        if (isValidTrack()) {
            this.volume = volume > 100 ? 100 : (volume < 0 ? 0 : volume);
            if (trackLine != null)
                trackLine.setGain(AudioUtil.convertVolRangeToLineRange(volume));
            isMute = this.volume == 0;
        }
    }

    @Override
    public void mute() {
        if (isValidTrack() && !isMute) {
            isMute = true;
            if (trackLine != null)
                trackLine.setGain(AudioUtil.convertVolRangeToLineRange(0));
        }
    }

    @Override
    public void unmute() {
        if (isValidTrack() && isMute) {
            isMute = false;
            setGain(volume);
        }
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
        if (tagInfo == null) {
            //System.out.println("Solicitando "+key.name()+" nulo");
            return null;
        }
        return tagInfo.getTag(key);
    }

    @Override
    public String getTitle() {
        String titleProper = getProperty(FieldKey.TITLE);
        return titleProper == null || titleProper.isEmpty() ? dataSource.getName() : titleProper;
    }

    @Override
    public String getAlbum() {
        return getProperty(FieldKey.ALBUM);
    }

    @Override
    public String getArtist() {
        return getProperty(FieldKey.ARTIST);
    }

    @Override
    public String getDate() {
        return getProperty(FieldKey.YEAR);
    }

    @Override
    public byte[] getCoverData() {
        return tagInfo.getCoverData();
    }

    @Override
    public long getDuration() {
        return tagInfo.getDuration();
    }

    @Override
    public String getEncoder() {
        return getProperty(FieldKey.ENCODER);
    }

    public String getFormat() {
        return trackStream.getFormat().toString();
    }

    // Testing
    public String getSongInfo() {
        StringBuilder sbInfo = new StringBuilder();
        String title = getTitle();
        String album = getAlbum();
        String artist = getArtist();
        String date = getDate();
        String duration = getDurationAsString();
        String hasCover = hasCover()?"Si":"No";
        String encoder = getEncoder();

        StringBuilder sbTabs = new StringBuilder();
        String currentLine = "Song: "+title;
        int biggerLenght = currentLine.length();
        sbInfo.append(currentLine).append('\n');

        if (album != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Album: "+album;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (artist != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Artist: "+artist;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (date != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Date: "+date;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (duration != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Duration: "+duration;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (hasCover != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Has Cover: "+hasCover;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }

        if (encoder != null) {
            sbTabs.append("    ");
            currentLine = sbTabs.toString()+"Encoder: "+encoder;
            if (biggerLenght < currentLine.length())
                biggerLenght = currentLine.length();
            sbInfo.append(currentLine).append('\n');
        }
        sbTabs.delete(0, sbTabs.length());

        for (int i = 0; i < biggerLenght; i++)
            sbTabs.append('-');
        sbTabs.append('\n').append(sbInfo.toString());

        for (int i = 0; i < biggerLenght; i++)
            sbTabs.append('-');
        sbTabs.append('\n');

        return sbTabs.toString();
    }

    public void getLineInfo() {
        SourceDataLine driver = trackLine.getDriver();
        System.out.println("Soporte de controles en line");
        System.out.println("---------------");
        System.out.println("Pan: "+
                driver.isControlSupported(FloatControl.Type.PAN));

        System.out.println("AuxReturn: "+
                driver.isControlSupported(FloatControl.Type.AUX_RETURN));

        System.out.println("AuxSend: "+
                driver.isControlSupported(FloatControl.Type.AUX_SEND));

        System.out.println("Balance: "+
                driver.isControlSupported(FloatControl.Type.BALANCE));

        System.out.println("ReverbReturn: "+
                driver.isControlSupported(FloatControl.Type.REVERB_RETURN));

        System.out.println("ReberbSend: "+
                driver.isControlSupported(FloatControl.Type.REVERB_SEND));

        System.out.println("Volume: "+
                driver.isControlSupported(FloatControl.Type.VOLUME));

        System.out.println("SampleRate: "+
                driver.isControlSupported(FloatControl.Type.SAMPLE_RATE));

        System.out.println("MasterGain: "+
                driver.isControlSupported(FloatControl.Type.MASTER_GAIN));
    }

    @Override
    public void run() {
        boolean isPlayerLinked = PlayerHandler.hasInstance();
        byte[] audioBuffer = new byte[4096];
        int read;
        play();
        long ti = Time.getInstance().getTime();

        //Logger logger = Logger.getLogger(this, null);
        //logger.setMsg("Starting Track "+getTitle()+"...");
        //logger.setMsg(getSongInfo());
        //logger.rawInfo();

        while (!isFinished() && !isKilled() && isValidTrack()) {
            try {
                while (isPlaying())
                    try {
                        if (isPaused())
                            break;
                        read = trackStream.read(audioBuffer);

                        if (read == -1) {
                            finish();
                            break;
                        }
                        if (trackLine != null)
                            trackLine.playAudio(audioBuffer);
                        else
                            Logger.getLogger(this, "TrackLineNull").info();
                    } catch (IndexOutOfBoundsException e) {
                        finish();
                    }
                Thread.sleep(50);
            } catch (IOException |
                    InterruptedException e) {
                e.printStackTrace();
            } catch(IllegalArgumentException e) {
                finish();
                Logger.getLogger(this, e.getMessage()).error();
                break;
            }
        }
        //Logger.getLogger(this,
        //        "FinalProgress/Duration: "+getProgress()+"/"+getDuration()).info();
        //Logger.getLogger(this, "Track "+getTitle()+" completed!").rawWarning();
        if (isFinished() && isPlayerLinked && PlayerHandler.hasInstance())
            PlayerHandler.getPlayer().playNext();
    }


}

