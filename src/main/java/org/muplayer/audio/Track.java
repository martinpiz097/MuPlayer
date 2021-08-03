package org.muplayer.audio;

import org.aucom.sound.Speaker;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.muplayer.audio.format.*;
import org.muplayer.audio.info.AudioTag;
import org.muplayer.audio.interfaces.MusicControls;
import org.muplayer.audio.interfaces.PlayerControls;
import org.muplayer.audio.model.TrackInfo;
import org.muplayer.audio.trackstates.*;
import org.muplayer.audio.util.AudioExtensions;
import org.muplayer.audio.util.TimeFormatter;
import org.muplayer.system.AudioUtil;
import org.muplayer.thread.TPlayingTrack;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public abstract class Track extends Thread implements MusicControls, TrackInfo {
    protected volatile File dataSource;
    protected volatile Speaker trackLine;
    protected volatile AudioInputStream trackStream;
    protected volatile AudioFileReader audioReader;
    protected volatile AudioTag tagInfo;

    protected volatile TrackState state;
    protected volatile double secsSeeked;
    protected volatile double bytesPerSecond;
    protected volatile float volume;
    protected volatile boolean isMute;

    protected volatile TPlayingTrack playingTrack;
    protected Object source;
    protected final PlayerControls player;

    protected final AudioSupportManager audioSupportManager = AudioSupportManager.getInstance();

    private static Constructor<? extends Track> getTrackClassConstructor(String formatClass, Class<?>... paramsClasses) {
        final Class<? extends Track> trackClass;
        try {
            trackClass = (Class<? extends Track>) Class.forName(formatClass);
            return trackClass.getConstructor(paramsClasses);
        } catch (Exception e) {
            return null;
        }
    }

    private static Track getTrackFromClass(String formatClass, File dataSource, PlayerControls player) {
        try {
            final Constructor<? extends Track> constructor
                    = getTrackClassConstructor(formatClass, dataSource.getClass(), PlayerControls.class);
            return constructor != null ? constructor.newInstance(dataSource, player) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Track getTrackFromClass(String formatClass, InputStream dataSource, PlayerControls player) {
        try {
            final Constructor<? extends Track> constructor
                    = getTrackClassConstructor(formatClass, dataSource.getClass(), PlayerControls.class);
            return constructor != null ? constructor.newInstance(dataSource, player) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Track getTrack(File fSound) {
        return getTrack(fSound, null);
    }

    public static Track getTrack(String trackPath) {
        return getTrack(new File(trackPath));
    }

    public static Track getTrack(InputStream inputStream) {
        return getTrack(inputStream, null);
    }

    static Track getTrack(File dataSource, PlayerControls player) {
        if (!dataSource.exists())
            return null;
        Track result = null;
        final String formatName = AudioExtensions.getFormatName(dataSource.getName());
        final AudioSupportManager supportManager = AudioSupportManager.getInstance();
        final String formatClass = supportManager.getProperty(formatName);

        if (formatClass != null)
            result = getTrackFromClass(formatClass, dataSource, player);
        else {
            // eso deberia irse por un throw
            try {
                throw new UnsupportedAudioFileException(formatName);
            } catch (UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    static Track getTrack(String trackPath, PlayerControls player) {
        return getTrack(new File(trackPath), player);
    }

    static Track getTrack(InputStream inputStream, PlayerControls player) {
        final AudioSupportManager supportManager = AudioSupportManager.getInstance();
        final Set<String> propertyNames = supportManager.getPropertyNames();

        final Optional<Track> optionalTrack = propertyNames.stream()
                .map(propName -> getTrackFromClass(supportManager.getProperty(propName), inputStream, player))
                .filter(Objects::nonNull)
                .findFirst();
        return optionalTrack.orElse(null);
    }

    public static boolean isValidTrack(String trackPath) {
        return isValidTrack(new File(trackPath));
    }

    public static boolean isValidTrack(File track) {
        return AudioUtil.isSupported(track) /*&& getTrack(track).isValidTrack()*/;
    }

    public static boolean isValidTrack(Path track) {
        return AudioUtil.isSupported(track) /*&& getTrack(track).isValidTrack()*/;
    }

    // Ir a un segundo especifico de la cancion
    // inhabiliado si aplico freeze al pausar

    protected Track(File dataSource)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(dataSource, null);
    }

    protected Track(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this(inputStream, null);
    }

    protected Track(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), null);
    }

    protected Track(File dataSource, PlayerControls player)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.dataSource = dataSource;
        //this.source = new BufferedInputStream(new FileInputStream(dataSource), (int) dataSource.length());
        this.source = dataSource;
        state = new StoppedState(this);
        secsSeeked = 0;
        volume = Player.DEFAULT_VOLUME;
        this.player = player;
        tagInfo = loadTagInfo(dataSource);
        setPriority(MAX_PRIORITY);
    }

    // ojo con los mp3
    protected Track(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.source = inputStream;
        state = new StoppedState(this);
        secsSeeked = 0;
        volume = Player.DEFAULT_VOLUME;
        this.player = player;
        setPriority(MAX_PRIORITY);
    }

    protected Track(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), player);
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

    protected Speaker createLine() throws LineUnavailableException {
        final Speaker line = new Speaker(trackStream.getFormat());
        line.open();
        setGain(volume);
        return line;
    }

    protected void initLine() throws LineUnavailableException {
        if (trackStream != null) {
            if (trackLine != null) {
                trackLine.stop();
                trackLine.close();
            }
            try {
                this.trackLine = createLine();
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

    protected AudioTag loadTagInfo(File dataSource) {
        try {
            final AudioTag audioTag = new AudioTag(dataSource);
            return audioTag.isValidFile() ? audioTag : null;
        } catch (Exception e) {
            return null;
        }
    }

    protected double getSecondsPosition() {
        if (trackLine == null)
            return 0;
        return ((double)trackLine.getDriver().getMicrosecondPosition()) / 1000000;
    }

    public void setSecsSeeked(double secsSeeked) {
        this.secsSeeked = secsSeeked;
    }

    // Posible motivo de error para mas adelante
    /*protected int getBuffLen() {
        long frameLen = trackStream == null ? BUFFSIZE : trackStream.getFrameLength();
        return frameLen > 0 ? (int) (frameLen / 1024) : BUFFSIZE;
    }*/

    public boolean isValidTrack() {
        return trackStream != null && trackLine != null;
    }

    public void validateTrack() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        if (!isValidTrack()) {
            initAll();
            closeAllStreams();
        }
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

    public TPlayingTrack getPlayingTrack() {
        return playingTrack;
    }

    public void setPlayingTrack(TPlayingTrack playingTrack) {
        this.playingTrack = playingTrack;
    }

    public TrackState getTrackState() {
        return state;
    }

    public String getStateToString() {
        return state.getName();
    }

    public PlayerControls getPlayer() {
        return player;
    }

    public File getDataSource() {
        return dataSource;
    }

    public AudioTag getTagInfo() {
        return tagInfo;
    }

    protected abstract double convertSecondsToBytes(Number seconds);
    protected abstract double convertBytesToSeconds(Number bytes);

    @Override
    public synchronized double getProgress() {
        return getSecondsPosition()+secsSeeked;
    }

    public synchronized String getFormattedProgress() {
        return TimeFormatter.format((long) getProgress());
    }

    public AudioFileFormat getFileFormat() throws IOException, UnsupportedAudioFileException {
        return audioReader == null ? null : audioReader.getAudioFileFormat(dataSource);
    }

    public AudioFormat getAudioFormat() {
        return trackStream.getFormat();
    }

    @Override
    public synchronized boolean isPlaying() {
        return state instanceof PlayingState;
    }

    @Override
    public synchronized boolean isPaused() {
        return state instanceof PausedState;
    }

    @Override
    public synchronized boolean isStopped() {
        return state instanceof StoppedState;
    }

    @Override
    public synchronized boolean isFinished() {
        return state instanceof FinishedState;
    }

    public synchronized boolean isKilled() {
        return state instanceof KilledState;
    }

    @Override
    public boolean isMute() {
        return isMute;
    }

    @Override
    public void play() {
        if (isAlive())
            state = new PlayingState(this);
    }

    @Override
    public void pause() {
        if (isPlaying())
            state = new PausedState(this);
    }

    @Override
    public void resumeTrack() {
        if (isAlive() && (isPaused() || isStopped())) {
            play();
            resume();
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (isAlive() && (isPlaying() || isPaused()))
            state = new StoppedState(this);
    }

    @Override
    public void finish() {
        state = new FinishedState(this);
    }

    public void kill() {
        state = new KilledState(this);
    }

    // en este caso pasan a ser seconds
    @Override
    public synchronized void seek(double seconds)
            throws IOException {
        if (seconds > 0) {
            final long bytesToSeek = Math.round(convertSecondsToBytes(seconds));
            final long skip = trackStream.skip(bytesToSeek);
            final double skippedSeconds = convertBytesToSeconds(skip);

            if (skip > 0)
                secsSeeked+=skippedSeconds;

            // se deben sumar los segundos que realmente se saltaron
            // o saltar bytes hasta completar esos segundos
        }

        else if (seconds < 0) {
            try {
                gotoSecond(getProgress() + seconds);
            } catch (LineUnavailableException | UnsupportedAudioFileException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void gotoSecond(double second) throws
            IOException, LineUnavailableException, UnsupportedAudioFileException {
        final double progress = getProgress();
        if (second >= progress) {
            final int duration = (int) getDuration();
            if (second > duration)
                second = duration;
            final int gt = (int) Math.round(second-getProgress());
            seek(gt);
        }
        else
            state = new ReverberatedState(this, second);
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
        return tagInfo != null && tagInfo.getCover() != null;
    }

    @Override
    public String getProperty(String key) {
        return tagInfo == null ? null : tagInfo.getTag(key);
    }

    @Override
    public String getProperty(FieldKey key) {
        return tagInfo != null ? tagInfo.getTag(key) : null;
    }

    @Override
    public String getTitle() {
        final String titleProper = getProperty(FieldKey.TITLE);

        return (titleProper == null || titleProper.trim().isEmpty())
                && dataSource != null ? dataSource.getName() : titleProper;
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
        return tagInfo != null ? tagInfo.getCoverData() : null;
    }

    @Override
    public long getDuration() {
        if (tagInfo != null)
            return tagInfo.getDuration();
        else {
         //   audioReader.getAudioFileFormat(dataSource).properties()
            return 0;
        }
    }

    @Override
    public String getEncoder() {
        return getProperty(FieldKey.ENCODER);
    }

    @Override
    public String getBitrate() {
        return tagInfo != null && tagInfo.getHeader() != null
                ? tagInfo.getHeader().getBitRate()
                : "Unknown";
    }

    public String getFormat() {
        return trackStream.getFormat().toString();
    }

    public void getLineInfo() {
        final SourceDataLine driver = trackLine.getDriver();
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
        try {
            initAll();
            state = new StartedState(this);
            while (state.canTrackContinue())
                state.execute();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
    }
}

