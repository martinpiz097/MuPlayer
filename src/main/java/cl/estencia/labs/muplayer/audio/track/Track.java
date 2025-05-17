package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.aucom.core.device.output.Speaker;
import cl.estencia.labs.aucom.core.io.AudioDecoder;
import cl.estencia.labs.muplayer.audio.info.AudioTag;
import cl.estencia.labs.muplayer.audio.player.AudioComponent;
import cl.estencia.labs.muplayer.audio.track.header.HeaderData;
import cl.estencia.labs.muplayer.audio.track.io.TrackIOUtil;
import cl.estencia.labs.muplayer.audio.track.state.*;
import cl.estencia.labs.muplayer.event.Listenable;
import cl.estencia.labs.muplayer.event.listener.TrackStateListener;
import cl.estencia.labs.muplayer.event.model.TrackEvent;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import cl.estencia.labs.muplayer.interfaces.ControllableMusic;
import cl.estencia.labs.muplayer.interfaces.TrackData;
import cl.estencia.labs.muplayer.event.notifier.user.TrackUserEventNotifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.java.Log;
import org.jaudiotagger.tag.FieldKey;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MAX_VOL;
import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MIN_VOL;
import static cl.estencia.labs.aucom.core.util.AudioDecodingUtil.DEFAULT_VOLUME;

@EqualsAndHashCode(callSuper = true)
@Log
public abstract class Track extends AudioComponent
        implements Runnable, ControllableMusic, TrackData, Listenable<TrackStateListener, TrackEvent> {
    @Getter protected final File dataSource;
    @Getter protected final AudioDecoder audioDecoder;
    @Getter protected final TrackIOUtil trackIOUtil;
    @Getter protected final Speaker speaker;
    protected final HeaderData headerData;

    @Getter protected final TrackStatusData trackStatusData;
    protected final AudioTag tagInfo;

    protected final TrackInternalEventNotifier internalEventNotifier;
    @Getter protected final TrackUserEventNotifier userEventNotifier;

    protected final AtomicReference<TrackState> trackState;

    public Track(String trackPath, AudioDecoder audioDecoder, TrackInternalEventNotifier internalEventNotifier) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), audioDecoder, internalEventNotifier);
    }

    public Track(File dataSource, AudioDecoder audioDecoder, TrackInternalEventNotifier internalEventNotifier)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        if (internalEventNotifier == null) {
            throw new MuPlayerException("eventNotifier cannot be null");
        }

        this.dataSource = dataSource;
        this.audioDecoder = audioDecoder;
        this.trackIOUtil = new TrackIOUtil();
        this.speaker = new Speaker(audioDecoder.getDecodedStream());
        this.headerData = initHeaderData();
        this.trackStatusData = new TrackStatusData();

        this.internalEventNotifier = internalEventNotifier;
        this.userEventNotifier = new TrackUserEventNotifier();

        this.tagInfo = loadTagInfo(dataSource);
        this.trackState = new AtomicReference<>();

        this.trackState.set(new UnknownState(this, internalEventNotifier, userEventNotifier));
    }

    protected abstract double convertSecondsToBytes(Number seconds);

    protected abstract double convertBytesToSeconds(Number bytes);

    protected HeaderData initHeaderData() {
        return new HeaderData(0L, 0d);
    }

    public AudioTag loadTagInfo(File dataSource) {
        try {
            final AudioTag audioTag = new AudioTag(dataSource);
            return audioTag.isValidFile() ? audioTag : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        audioDecoder.reDecode();
        speaker.reopen(audioDecoder.getDecodedFormat());
    }

    // Posible motivo de error para mas adelante
    /*protected int getBuffLen() {
        long frameLen = trackStream == null ? BUFFSIZE : trackStream.getFrameLength();
        return frameLen > 0 ? (int) (frameLen / 1024) : BUFFSIZE;
    }*/

    public TrackStateName getStateName() {
        return trackState.get().getName();
    }

    @Override
    public long getDuration() {
        return tagInfo != null ? tagInfo.getDuration() : 0;
    }

    @Override
    public synchronized double getProgress() {
        return trackIOUtil.getSecondsPosition(speaker) + trackStatusData.getSecsSeeked();
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

    public synchronized boolean isFinished() {
        return getStateName() == TrackStateName.FINISHED;
    }

    @Override
    public boolean isMute() {
        return trackStatusData.isMute();
    }

    @Override
    public void play() {
        if (isAlive()) {
            trackState.set(new PlayingState(this, internalEventNotifier, userEventNotifier));
        }
    }

    @Override
    public void pause() {
        if (isPlaying()) {
            trackState.set(new PausedState(this, internalEventNotifier, userEventNotifier));
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
            trackState.set(new StoppedState(this, internalEventNotifier, userEventNotifier));
        }
    }

    @Override
    public void reload() throws Exception {
        //trackState = new ReloadedState(this, notifier);
        throw new UnsupportedOperationException("Reload not supported yet!");
    }

    public void finish() {
        trackState.set(new FinishedState(this, internalEventNotifier, userEventNotifier));
    }

    // en este caso pasan a ser seconds
    @Override
    public synchronized void seek(double seconds)
            throws IOException {
        if (getProgress() + seconds > getDuration()) {
            finish();
        }

        if (seconds > 0) {
            final long bytesToSeek = Math.round(convertSecondsToBytes(seconds));
            final long skip = audioDecoder.getDecodedStream().skip(bytesToSeek);
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
            trackState.set(new ReverberatedState(this,
                    internalEventNotifier, userEventNotifier, second));
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
        if (trackIOUtil != null && trackIOUtil.isTrackStreamsOpened(speaker, audioDecoder.getDecodedStream())) {
            speaker.setVolume(volume);
            if (trackStatusData.isVolumeZero()) {
                audioSystemManager.setMuteValue(speaker.getDriver(), true);
            }
        }
    }

    @Override
    public void mute() {
        trackStatusData.setMute(true);
        if (trackIOUtil != null && trackIOUtil.isTrackStreamsOpened(speaker, audioDecoder.getDecodedStream())) {
            audioSystemManager.setMuteValue(speaker.getDriver(), trackStatusData.isMute());
        }
    }

    @Override
    public void unMute() {
        if (trackStatusData.isVolumeZero()) {
            trackStatusData.setVolume(DEFAULT_MAX_VOL);
            if (trackIOUtil != null && trackIOUtil.isTrackStreamsOpened(speaker, audioDecoder.getDecodedStream())) {
                speaker.setVolume(trackStatusData.getVolume());
            }
        } else {
            trackStatusData.setMute(false);
        }
        if (trackIOUtil != null && trackIOUtil.isTrackStreamsOpened(speaker, audioDecoder.getDecodedStream())) {
            audioSystemManager.setMuteValue(speaker.getDriver(), false);
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
        return audioDecoder.getDecodedFormat().toString();
    }

    @Override
    public synchronized void start() {
        if (getState() == State.NEW) {
            super.start();
        } else {
            throw new MuPlayerException("Already started track");
        }
    }

    @Override
    public void addListener(TrackStateListener listener) {
        userEventNotifier.addListener(listener);
    }

    @Override
    public List<TrackStateListener> getAllListeners() {
        return userEventNotifier.getAllListeners();
    }

    @Override
    public void removeListener(TrackStateListener listener) {
        userEventNotifier.removeListener(listener);
    }

    @Override
    public void removeAllListeners() {
        userEventNotifier.removeAllListeners();
    }

    @Override
    public void sendEvent(TrackEvent trackEvent) {
        userEventNotifier.sendEvent(trackEvent);
        internalEventNotifier.sendEvent(trackEvent);
    }

    @Override
    public void run() {
        trackState.set(new StartedState(this, internalEventNotifier, userEventNotifier));
        while (trackStatusData.canTrackContinue()) {
            trackState.get().handle();
            System.out.println("WHILE " + getTitle());
        }

        System.out.println("TRACK CLOSED " + getTitle());
    }
}