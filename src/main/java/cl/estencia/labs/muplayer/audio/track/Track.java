package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.aucom.audio.device.Speaker;
import cl.estencia.labs.aucom.io.AudioDecoder;
import cl.estencia.labs.muplayer.audio.track.state.*;
import cl.estencia.labs.muplayer.interfaces.TrackData;
import cl.estencia.labs.muplayer.listener.TrackEvent;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.jaudiotagger.tag.FieldKey;
import cl.estencia.labs.muplayer.audio.info.AudioTag;
import cl.estencia.labs.muplayer.audio.player.AudioComponent;
import cl.estencia.labs.muplayer.audio.player.Player;
import cl.estencia.labs.muplayer.interfaces.ControllableMusic;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MAX_VOL;
import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MIN_VOL;
import static cl.estencia.labs.aucom.util.DecoderFormatUtil.DEFAULT_VOLUME;

@EqualsAndHashCode(callSuper = true)
@Log
public abstract class Track extends AudioComponent implements Runnable, ControllableMusic, TrackData {
    @Getter protected final File dataSource;
    @Getter protected final AudioDecoder audioDecoder;
    @Getter protected final TrackIO trackIO;
    @Getter protected final Speaker speaker;
    @Getter protected final TrackStatusData trackStatusData;

    protected final List<TrackEvent> listInternalEvents;
    @Getter protected final List<TrackEvent> listUserEvents;

    @Getter @Setter protected volatile AudioTag tagInfo;
    protected volatile TrackState trackState;

    protected final Player player;

    public Track(String trackPath, AudioDecoder audioDecoder) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), audioDecoder, null);
    }

    public Track(File dataSource, AudioDecoder audioDecoder)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(dataSource, audioDecoder, null);
    }

    public Track(String trackPath, AudioDecoder audioDecoder, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), audioDecoder, player);
    }

    public Track(File dataSource, AudioDecoder audioDecoder, Player player)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.dataSource = dataSource;
        this.audioDecoder = audioDecoder;
        this.trackIO = new TrackIO(getAudioFileReader(), audioDecoder.getDecodedStream());
        this.speaker = trackIO.getSpeaker();
        this.trackStatusData = new TrackStatusData();
        this.listInternalEvents = new ArrayList<>();
        this.listUserEvents = new LinkedList<>();
        this.player = player;

        initValues();
    }

    private void initValues() {
        try {
            trackStatusData.setSecsSeeked(0);
            trackStatusData.setBytesPerSecond(0);
            trackStatusData.setVolume(DEFAULT_VOLUME);
            trackStatusData.setMute(false);
            trackStatusData.setCanTrackContinue(true);
            setTagInfo(loadTagInfo(getDataSource()));
        } catch (Exception e) {
            log.log(Level.SEVERE, e.getMessage());
            kill();
        }
    }

    protected abstract AudioFileReader getAudioFileReader();

    protected abstract double convertSecondsToBytes(Number seconds);

    protected abstract double convertBytesToSeconds(Number bytes);

    public void updateIOData() {
        AudioInputStream decodedStream = audioDecoder.getDecodedStream();

        trackIO.setAudioFileReader(getAudioFileReader());
        trackIO.setDecodedInputStream(decodedStream);

        speaker.reopen(decodedStream.getFormat());
    }

    public abstract List<String> getAudioFileExtensions();

    public AudioTag loadTagInfo(File dataSource) {
        try {
            final AudioTag audioTag = new AudioTag(dataSource);
            return audioTag.isValidFile() ? audioTag : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (trackIO.closeStream() && trackIO.closeSpeaker()) {
            updateIOData();
        }
    }

    // Posible motivo de error para mas adelante
    /*protected int getBuffLen() {
        long frameLen = trackStream == null ? BUFFSIZE : trackStream.getFrameLength();
        return frameLen > 0 ? (int) (frameLen / 1024) : BUFFSIZE;
    }*/

    public TrackStateName getStateName() {
        return trackState.getName();
    }

    public void addUserEvent(TrackEvent trackEvent) {
        synchronized (listUserEvents) {
            listUserEvents.add(trackEvent);
        }
    }

    public void removeUserEvent(TrackEvent trackEvent) {
        synchronized (listUserEvents) {
            listUserEvents.remove(trackEvent);
        }
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
        return getStateName() == TrackStateName.PLAYING;
    }

    @Override
    public synchronized boolean isPaused() {
        return getStateName() == TrackStateName.PAUSED;
    }

    @Override
    public synchronized boolean isStopped() {
        return getStateName() == TrackStateName.STOPPED;
    }

    public synchronized boolean isKilled() {
        return getStateName() == TrackStateName.KILLED;
    }

    @Override
    public boolean isMute() {
        return trackStatusData.isMute();
    }

    @Override
    public void play() {
        if (isAlive()) {
            trackState = new PlayingState(player, this, listInternalEvents);
        }
    }

    @Override
    public void pause() {
        if (isPlaying()) {
            trackState = new PausedState(player, this, listInternalEvents);
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
            trackState = new StoppedState(player, this, listInternalEvents);
        }
    }

    @Override
    public void reload() throws Exception {
        //trackState = new ReloadedState(player, this, listInternalEvents);
    }

    public void kill() {
        trackState = new KilledState(player, this, listInternalEvents);
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
            trackState = new ReverberatedState(player, this, second, listInternalEvents);
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
            speaker.setVolume(volume);
            if (trackStatusData.isVolumeZero()) {
                audioHardware.setMuteValue(speaker.getDriver(), true);
            }
        }
    }

    @Override
    public void mute() {
        trackStatusData.setMute(true);
        if (trackIO != null && trackIO.isTrackStreamsOpened()) {
            audioHardware.setMuteValue(speaker.getDriver(), trackStatusData.isMute());
        }
    }

    @Override
    public void unMute() {
        if (trackStatusData.isVolumeZero()) {
            trackStatusData.setVolume(DEFAULT_MAX_VOL);
            if (trackIO != null && trackIO.isTrackStreamsOpened()) {
                speaker.setVolume(trackStatusData.getVolume());
            }
        } else {
            trackStatusData.setMute(false);
        }
        if (trackIO != null && trackIO.isTrackStreamsOpened()) {
            audioHardware.setMuteValue(speaker.getDriver(), false);
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
            trackState = new StartedState(player, this, listInternalEvents);
        }
    }

    @Override
    public void run() {
        trackState = new StartedState(player, this, listInternalEvents);
        while (trackStatusData.canTrackContinue()) {
            trackState.execute();
        }
    }
}