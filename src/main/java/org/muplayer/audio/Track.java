package org.muplayer.audio;

import org.aucom.sound.Speaker;
import org.jaudiotagger.tag.FieldKey;
import org.muplayer.info.TrackIO;
import org.muplayer.properties.AudioSupportManager;
import org.muplayer.info.AudioTag;
import org.muplayer.info.PlayerData;
import org.muplayer.info.TrackData;
import org.muplayer.interfaces.MusicControls;
import org.muplayer.interfaces.PlayerControls;
import org.muplayer.model.TrackInfo;
import org.muplayer.util.FileUtil;
import org.muplayer.system.Time;
import org.muplayer.model.trackstates.*;
import org.muplayer.util.AudioUtil;
import org.muplayer.thread.TPlayingTrack;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.muplayer.util.TrackUtil.getTrackFromClass;

public abstract class Track extends Thread implements MusicControls, TrackInfo {
    protected volatile Object dataSource;
    protected volatile AudioTag tagInfo;

    protected volatile TrackIO trackIO;
    protected volatile TrackData trackData;

    protected volatile TrackState state;
    protected volatile TPlayingTrack playingTrack;
    protected final PlayerControls player;
    protected final AudioSupportManager audioSupportManager = AudioSupportManager.getInstance();

    public static Track getTrack(Object dataSource) {
        return getTrack(dataSource, null);
    }

    public static Track getTrack(Object dataSource, PlayerControls player) {
        if (dataSource instanceof File || dataSource instanceof String) {
            File fileSource = dataSource instanceof File ? (File) dataSource : new File((String) dataSource);
            if (!fileSource.exists())
                return null;

            Track result = null;
            final String formatName = FileUtil.getFormatName(fileSource.getName());
            final AudioSupportManager supportManager = AudioSupportManager.getInstance();
            final String formatClass = supportManager.getProperty(formatName);

            // ojo que puede faltar un throws para mas adelante
            // avisando que se intenta cargar un archivo que no es audio
            if (AudioUtil.isSupported(fileSource))
                result = getTrackFromClass(formatClass, fileSource, player);
            return result;
        }
        else if (dataSource instanceof InputStream) {
            InputStream inputStream = (InputStream) dataSource;
            final AudioSupportManager supportManager = AudioSupportManager.getInstance();
            final Set<String> propertyNames = supportManager.getPropertyNames();

            final Optional<Track> optionalTrack = propertyNames.stream()
                    .map(propName -> getTrackFromClass(supportManager.getProperty(propName), inputStream, player))
                    .filter(Objects::nonNull)
                    .findFirst();
            return optionalTrack.orElse(null);
        }
        /*else if (dataSource instanceof URL) {
            // not supported yet
            return null;
        }*/
        else
            return null;

    }

    public static boolean isValidTrack(String trackPath) {
        return isValidTrack(new File(trackPath));
    }

    public static boolean isValidTrack(File track) {
        return AudioUtil.isSupported(track);
    }

    public static boolean isValidTrack(Path track) {
        return AudioUtil.isSupported(track);
    }

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
        trackIO = new TrackIO();
        state = new StoppedState(this);
        this.trackData = new TrackData(0, 0, PlayerData.DEFAULT_VOLUME, false);
        this.player = player;
        tagInfo = loadTagInfo(dataSource);
        setPriority(MAX_PRIORITY);
    }

    // ojo con los mp3
    protected Track(InputStream inputStream, PlayerControls player) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.dataSource = inputStream;
        trackIO = new TrackIO();
        state = new StoppedState(this);
        this.trackData = new TrackData(0, 0, PlayerData.DEFAULT_VOLUME, false);
        this.player = player;
        setPriority(MAX_PRIORITY);
    }

    protected Track(String trackPath, PlayerControls player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), player);
    }

    // Ver opcion de usar archivo temporal y leer desde ahi
    protected abstract void loadAudioStream() throws IOException, UnsupportedAudioFileException;

    public Speaker createLine() throws LineUnavailableException {
        Speaker line = trackIO.createLine();
        setGain(trackData.getVolume());
        return line;
    }

    public void initLine() throws LineUnavailableException {
        final boolean initialized = trackIO.initLine();
        if (initialized)
            setGain(trackData.getVolume());
    }

    public void closeAllStreams() {
        final boolean closed = trackIO.closeAllStreams();
        if (closed)
            trackData.setSecsSeeked(0);
    }

    public void initAll() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        loadAudioStream();
        initLine();
    }

    public void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        closeAllStreams();
        initAll();
    }

    public void validateTrack() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        if (!trackIO.isTrackStreamsOpened()) {
            initAll();
            closeAllStreams();
        }
    }

    protected AudioTag loadTagInfo(File dataSource) {
        try {
            final AudioTag audioTag = new AudioTag(dataSource);
            return audioTag.isValidFile() ? audioTag : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Posible motivo de error para mas adelante
    /*protected int getBuffLen() {
        long frameLen = trackStream == null ? BUFFSIZE : trackStream.getFrameLength();
        return frameLen > 0 ? (int) (frameLen / 1024) : BUFFSIZE;
    }*/

    public TrackIO getTrackIO() {
        return trackIO;
    }

    public TrackData getTrackData() {
        return trackData;
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

    public File getDataSourceAsFile() {
        return dataSource instanceof File ? (File) dataSource : null;
    }

    public InputStream getDataSourceAsStream() {
        return dataSource instanceof InputStream ? (InputStream) dataSource : null;
    }

    public URL getDataSourceAsURL() {
        return dataSource instanceof URL ? (URL) dataSource : null;
    }

    public AudioTag getTagInfo() {
        return tagInfo;
    }

    protected abstract double convertSecondsToBytes(Number seconds);
    protected abstract double convertBytesToSeconds(Number bytes);

    @Override
    public synchronized double getProgress() {
        return trackIO.getSecondsPosition()+trackData.getSecsSeeked();
    }

    public synchronized String getFormattedProgress() {
        final String progress = Time.getInstance().getTimeFormatter().format((long) getProgress());
        final String duration = getFormattedDuration();
        return progress+"/"+duration;
    }

    public AudioFileFormat getFileFormat() throws IOException, UnsupportedAudioFileException {
        return trackIO.getAudioFileFormat(dataSource);
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
        return trackData.isMute();
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
            final long skip = trackIO.getDecodedStream().skip(bytesToSeek);
            final double skippedSeconds = convertBytesToSeconds(skip);

            if (skip > 0)
                trackData.setSecsSeeked(trackData.getSecsSeeked()+skippedSeconds);

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
        return trackData.isMute() ? 0 : trackData.getVolume();
    }

    // -80 to 5.5
    @Override
    public void setGain(float volume) {
        if (trackIO.isTrackStreamsOpened()) {
            trackData.setVolume(volume > 100 ? 100 : (volume < 0 ? 0 : volume));
            if (trackIO.getTrackLine() != null)
                trackIO.getTrackLine().setGain(AudioUtil.convertVolRangeToLineRange(volume));
            trackData.setMute(trackData.getVolume() == 0);
        }
    }

    @Override
    public void mute() {
        if (trackIO.isTrackStreamsOpened() && !trackData.isMute()) {
            trackData.setMute(true);
            if (trackIO.getTrackLine() != null)
                trackIO.getTrackLine().setGain(AudioUtil.convertVolRangeToLineRange(0));
        }
    }

    @Override
    public void unMute() {
        if (trackIO.isTrackStreamsOpened() && trackData.isMute()) {
            trackData.setMute(false);
            setGain(trackData.getVolume());
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
        final File fileSource = dataSource != null && dataSource instanceof File ? (File) dataSource : null;

        return (titleProper == null || titleProper.trim().isEmpty())
                && fileSource != null ? fileSource.getName() : titleProper;
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
        //   audioReader.getAudioFileFormat(dataSource).properties()
        return tagInfo != null ? tagInfo.getDuration() : 0;
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
        return trackIO.getDecodedStream().getFormat().toString();
    }

    public void getLineInfo() {
        final SourceDataLine driver = trackIO.getTrackLine().getDriver();
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