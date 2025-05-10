package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.muplayer.audio.track.state.*;
import lombok.Data;
import lombok.extern.java.Log;
import org.jaudiotagger.tag.FieldKey;
import cl.estencia.labs.muplayer.audio.info.AudioTag;
import cl.estencia.labs.muplayer.audio.player.AudioComponent;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.interfaces.ControllableMusic;
import cl.estencia.labs.muplayer.interfaces.ReportableTrack;
import cl.estencia.labs.muplayer.model.MuPlayerAudioFormat;
import cl.estencia.labs.muplayer.audio.io.AudioIO;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MAX_VOL;
import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MIN_VOL;

@Data
@Log
public abstract class Track extends AudioComponent implements Runnable, ControllableMusic, ReportableTrack {
    protected final File dataSource;
    protected volatile AudioTag tagInfo;
    protected volatile TrackIO trackIO;
    protected final TrackStatusData trackStatusData;
    protected volatile TrackState trackState;
    protected final AudioIO audioIO;

    protected final Player player;

    public Track(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), null);
    }

    public Track(File dataSource)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(dataSource, null);
    }

    public Track(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), player);
    }

    public Track(File dataSource, Player player)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.dataSource = dataSource;
        this.player = player;
        this.trackStatusData = new TrackStatusData();
        this.trackState = new InitializedState(player, this);
        this.audioIO = initAudioIO();
    }

    // Ver opcion de usar archivo temporal y leer desde ahi
    protected abstract void loadAudioStream() throws IOException, UnsupportedAudioFileException;

    protected abstract double convertSecondsToBytes(Number seconds);

    protected abstract double convertBytesToSeconds(Number bytes);

    protected abstract AudioIO initAudioIO();

    public abstract MuPlayerAudioFormat[] getAudioFileFormats();

    public AudioTag loadTagInfo(File dataSource) {
        try {
            final AudioTag audioTag = new AudioTag(dataSource);
            return audioTag.isValidFile() ? audioTag : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void initSpeaker() throws LineUnavailableException {
        if (trackIO.initSpeaker()) {
            setVolume(trackStatusData.getVolume());
        }
    }

    public void initStreamAndLine() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        loadAudioStream();
        initSpeaker();
    }

    public void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (trackIO.closeStream() && trackIO.closeSpeaker()) {
            initStreamAndLine();
        }
    }

    // Posible motivo de error para mas adelante
    /*protected int getBuffLen() {
        long frameLen = trackStream == null ? BUFFSIZE : trackStream.getFrameLength();
        return frameLen > 0 ? (int) (frameLen / 1024) : BUFFSIZE;
    }*/

    public String getStateToString() {
        return trackState.getName();
    }

    @Override
    public long getDuration() {
        return tagInfo != null ? tagInfo.getDuration() : 0;
    }

    @Override
    public synchronized double getProgress() {
        return trackIO.getSecondsPosition() + trackStatusData.getSecsSeeked();
    }

    @Override
    public synchronized boolean isPlaying() {
        return trackState instanceof PlayingState;
    }

    @Override
    public synchronized boolean isPaused() {
        return trackState instanceof PausedState;
    }

    @Override
    public synchronized boolean isStopped() {
        return trackState instanceof StoppedState;
    }

    public synchronized boolean isKilled() {
        return trackState instanceof KilledState;
    }

    @Override
    public boolean isMute() {
        return trackStatusData.isMute();
    }

    @Override
    public void play() {
        if (isAlive()) {
            trackState = new PlayingState(player, this);
        }
    }

    @Override
    public void pause() {
        if (isPlaying()) {
            trackState = new PausedState(player, this);
        }
    }

    @Override
    public void resumeTrack() {
        if (isAlive() && (isPaused() || isStopped())) {
            play();
            synchronized (this) {
                notify();
            }
        }
    }

    @Override
    public synchronized void stopTrack() {
        if (isAlive() && (isPlaying() || isPaused())) {
            trackState = new StoppedState(player, this);
        }
    }

    @Override
    public void reload() throws Exception {
        //trackState = new ReloadedState(player, this);
    }

    public void kill() {
        trackState = new KilledState(player, this);
    }

    // en este caso pasan a ser seconds
    @Override
    public synchronized void seek(double seconds)
            throws IOException {
        if (seconds > 0) {
            final long bytesToSeek = Math.round(convertSecondsToBytes(seconds));
            final long skip = trackIO.getDecodedInputStream().skip(bytesToSeek);
            final double skippedSeconds = convertBytesToSeconds(skip);

            if (skip > 0) {
                trackStatusData.setSecsSeeked(trackStatusData.getSecsSeeked() + skippedSeconds);
            }
            // se deben sumar los segundos que realmente se saltaron
            // o saltar bytes hasta completar esos segundos
        } else if (seconds < 0) {
            try {
                gotoSecond(getProgress() + seconds);
            } catch (LineUnavailableException | UnsupportedAudioFileException e) {
                log.severe(e.getMessage());
            }
        }

    }

    @Override
    public void gotoSecond(double second) throws
            IOException, LineUnavailableException, UnsupportedAudioFileException {
        final double progress = getProgress();
        if (second >= progress) {
            final int duration = (int) getDuration();
            if (second > duration) {
                second = duration;
            }
            final int gotoValue = (int) Math.round(second - getProgress());
            seek(gotoValue);
        } else {
            trackState = new ReverberatedState(player, this, second);
        }
    }

    @Override
    public float getVolume() {
        return trackStatusData.isMute() ? DEFAULT_MIN_VOL : trackStatusData.getVolume();
    }

    // -80 to 5.5
    @Override
    public void setVolume(float volume) {
        trackStatusData.setVolume(volume);
        if (trackIO != null && trackIO.isTrackStreamsOpened()) {
            trackIO.setVolume(volume);
            if (trackStatusData.isVolumeZero()) {
                audioHardware.setMuteValue(trackIO.getSpeakerDriver(), true);
            }
        }
    }

    @Override
    public void mute() {
        trackStatusData.setMute(true);
        if (trackIO != null && trackIO.isTrackStreamsOpened()) {
            audioHardware.setMuteValue(trackIO.getSpeakerDriver(), trackStatusData.isMute());
        }
    }

    @Override
    public void unMute() {
        if (trackStatusData.isVolumeZero()) {
            trackStatusData.setVolume(DEFAULT_MAX_VOL);
            if (trackIO != null && trackIO.isTrackStreamsOpened()) {
                trackIO.setVolume(trackStatusData.getVolume());
            }
        } else {
            trackStatusData.setMute(false);
        }
        if (trackIO != null && trackIO.isTrackStreamsOpened()) {
            audioHardware.setMuteValue(trackIO.getSpeakerDriver(), false);
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
    public String getEncoder() {
        return getProperty(FieldKey.ENCODER);
    }

    @Override
    public String getBitrate() {
        return tagInfo != null && tagInfo.getHeader() != null
                ? tagInfo.getHeader().getBitRate()
                : "Unknown";
    }

    @Override
    public String getFormat() {
        return trackIO.getDecodedInputStream().getFormat().toString();
    }

    @Override
    public synchronized void start() {
        if (getState() == State.NEW) {
            super.start();
        } else {
            trackState = new StartedState(player, this);
        }
    }

    @Override
    public void run() {
        trackState = new StartedState(player, this);
        while (trackStatusData.canTrackContinue()) {
            trackState.handle();
        }
    }
}