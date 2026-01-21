package cl.estencia.labs.muplayer.audio.track;

import cl.estencia.labs.aucom.core.device.output.Speaker;
import cl.estencia.labs.aucom.core.io.AudioDecoder;
import cl.estencia.labs.aucom.core.util.AudioSystemManager;
import cl.estencia.labs.muplayer.audio.interfaces.ControllableMusic;
import cl.estencia.labs.muplayer.audio.interfaces.TrackData;
import cl.estencia.labs.muplayer.audio.model.TrackStatusData;
import cl.estencia.labs.muplayer.audio.track.data.AudioTag;
import cl.estencia.labs.muplayer.audio.track.data.HeaderData;
import cl.estencia.labs.muplayer.audio.util.AudioDriverUtil;
import cl.estencia.labs.muplayer.audio.track.state.*;
import cl.estencia.labs.muplayer.core.exception.MuPlayerException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MAX_VOL;
import static cl.estencia.labs.aucom.common.AudioConstants.DEFAULT_MIN_VOL;

@EqualsAndHashCode(callSuper = true)
@Slf4j
public abstract class Track extends Thread
        implements Runnable, ControllableMusic, TrackData {
    @Getter protected final File dataSource;
    @Getter protected final AudioDecoder audioDecoder;
    @Getter protected final Speaker speaker;
    protected final HeaderData headerData;

    @Getter protected final TrackStatusData trackStatusData;
    protected final AudioTag tagInfo;

    protected volatile TrackState trackState;

    protected final AudioSystemManager audioSystemManager;

    public Track(String trackPath, AudioDecoder audioDecoder) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), audioDecoder);
    }

    public Track(File dataSource, AudioDecoder audioDecoder)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.dataSource = dataSource;
        this.audioDecoder = audioDecoder;
        this.speaker = new Speaker(audioDecoder.getDecodedAudioStream());
        this.headerData = initHeaderData();
        this.trackStatusData = new TrackStatusData();

        this.tagInfo = loadTagInfo(dataSource);
        this.trackState = new UnknownState(this);
        this.audioSystemManager = new AudioSystemManager();
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
        return trackState.getName();
    }

    @Override
    public long getDuration() {
        return tagInfo != null ? tagInfo.getDuration() : 0;
    }

    @Override
    public String getGenre() {
        return getProperty(FieldKey.GENRE);
    }

    @Override
    public synchronized double getProgress() {
        return AudioDriverUtil.getSecondsPosition(speaker) + trackStatusData.getSecsSeeked();
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

    public synchronized boolean isKilled() {
        return getStateName() == TrackStateName.KILLED;
    }

    public boolean isActive() {
        return isAlive() && (!isFinished() && !isKilled());
    }

    @Override
    public boolean isMute() {
        return trackStatusData.isMute();
    }

    @Override
    public void play() {
        if (isAlive()) {
            trackState = new PlayingState(this);
        }
    }

    @Override
    public void pause() {
        if (isPlaying()) {
            trackState = new PausedState(this);
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
            trackState = new StoppedState(this);
        }
    }

    @Override
    public void reload() throws Exception {
        //trackState = new ReloadedState(this, notifier);
        throw new UnsupportedOperationException("Reload not supported yet!");
    }

    public void finish() {
        trackState = new FinishedState(this);
    }

    public void kill() {
        trackState = new KilledState(this);
    }

    // en este caso pasan a ser seconds
    @Override
    public synchronized void seek(double seconds)
            throws IOException {
        if (seconds == 0) {
            return;
        }

        if (getProgress() + seconds > getDuration()) {
            finish();
        }

        if (seconds > 0) {
            final long bytesToSeek = Math.round(convertSecondsToBytes(seconds));
            final long skip = audioDecoder.getDecodedAudioStream().skip(bytesToSeek);
            final double skippedSeconds = convertBytesToSeconds(skip);

            if (skip > 0) {
                trackStatusData.setSecsSeeked(trackStatusData.getSecsSeeked() + skippedSeconds);
            }
            // se deben sumar los segundos que realmente se saltaron
            // o saltar bytes hasta completar esos segundos
        } else {
            try {
                gotoSecond(getProgress() + seconds);
            } catch (LineUnavailableException | UnsupportedAudioFileException e) {
                log.error(e.getMessage(), e);
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
            trackState = new ReverberatedState(this, second);
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
        if (!AudioDriverUtil.isTrackStreamsOpened(speaker, audioDecoder.getDecodedAudioStream())) {
            return;
        }

        speaker.setVolume(volume);
        audioSystemManager.setMuteValue(speaker.getDriver(), trackStatusData.isMute());
    }

    @Override
    public void mute() {
        trackStatusData.setMute(true);
        if (!AudioDriverUtil.isTrackStreamsOpened(speaker, audioDecoder.getDecodedAudioStream())) {
            return;
        }

        audioSystemManager.setMuteValue(speaker.getDriver(), true);
    }

    @Override
    public void unMute() {
        trackStatusData.setMute(false);
        if (trackStatusData.isVolumeZero()) {
            trackStatusData.setVolume(DEFAULT_MAX_VOL);
        }

        if (!AudioDriverUtil.isTrackStreamsOpened(speaker, audioDecoder.getDecodedAudioStream())) {
            return;
        }

        speaker.setVolume(trackStatusData.getVolume());
        audioSystemManager.setMuteValue(speaker.getDriver(), false);
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

        return titleProper != null && !titleProper.isBlank()
                ? titleProper
                : (dataSource != null ? dataSource.getName() : "");
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
    public String getYear() {
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
        if (getState() != State.NEW) {
            throw new MuPlayerException("Already started track");
        }

        super.start();
    }

    @Override
    public void run() {
        trackState = new StartedState(this);
        while (trackStatusData.canTrackContinue()) {
            trackState.handle();
        }
    }

}