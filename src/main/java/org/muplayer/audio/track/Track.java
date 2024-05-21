package org.muplayer.audio.track;

import lombok.Data;
import lombok.extern.java.Log;
import org.jaudiotagger.tag.FieldKey;
import org.muplayer.audio.info.AudioHardware;
import org.muplayer.audio.info.AudioTag;
import org.muplayer.audio.player.Player;
import org.muplayer.audio.track.state.*;
import org.muplayer.exception.MuPlayerException;
import org.muplayer.interfaces.ControllableMusic;
import org.muplayer.interfaces.ReportableTrack;
import org.muplayer.data.properties.support.AudioSupportInfo;
import org.muplayer.util.AudioUtil;
import org.muplayer.util.FileUtil;
import org.muplayer.util.TrackUtil;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

@Data
@Log
public abstract class Track extends Thread implements ControllableMusic, ReportableTrack {
    protected volatile Object dataSource;
    protected volatile AudioTag tagInfo;
    protected volatile TrackIO trackIO;
    protected volatile TrackData trackData;
    protected volatile TrackState trackState;

    protected final Player player;

    protected static final AudioSupportInfo audioSupportInfo = AudioSupportInfo.getInstance();

    public static Track getTrack(Object dataSource) {
        return getTrack(dataSource, null);
    }

    public static Track getTrack(Object dataSource, Player player) {
        if (dataSource != null) {
            final File fileSource;
            if (dataSource instanceof File) {
                fileSource = (File) dataSource;
            }
            else if (dataSource instanceof String) {
                fileSource = new File((String) dataSource);
            }
            else {
                throw new MuPlayerException("The dataSource object is not File or String instance");
            }

            Track result = null;
            if (fileSource.exists()) {
                final String formatName = FileUtil.getFormatName(fileSource.getName());
                final String formatClass = audioSupportInfo.getProperty(formatName);

                if (formatClass != null) {
                    result = TrackUtil.getTrackFromClass(formatClass, fileSource, player);
                }
                else {
                    throw new MuPlayerException("Audio format " + formatName + " not supported!");
                }
            }
            else {
                throw new MuPlayerException("The dataSource file for path "+fileSource.getPath()+" not exists");
            }

            return result;
        }
        else {
            throw new MuPlayerException("The dataSource object is null");
        }
    }

    protected Track(File dataSource)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(dataSource, null);
    }

    protected Track(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), null);
    }

    protected Track(File dataSource, Player player)
            throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this.dataSource = dataSource;
        this.player = player;
        this.trackState = new InitializedState(player, this);
        setPriority(MAX_PRIORITY);
    }

    protected Track(String trackPath, Player player) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        this(new File(trackPath), player);
    }

    // Ver opcion de usar archivo temporal y leer desde ahi
    protected abstract void loadAudioStream() throws IOException, UnsupportedAudioFileException;

    protected abstract double convertSecondsToBytes(Number seconds);

    protected abstract double convertBytesToSeconds(Number bytes);

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
            setVolume(trackData.getVolume());
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

    public File getDataSourceAsFile() {
        return dataSource instanceof File ? (File) dataSource : null;
    }

    /*public InputStream getDataSourceAsStream() {
        return dataSource instanceof InputStream ? (InputStream) dataSource : null;
    }

    public URL getDataSourceAsURL() {
        return dataSource instanceof URL ? (URL) dataSource : null;
    }*/

    @Override
    public long getDuration() {
        return tagInfo != null ? tagInfo.getDuration() : 0;
    }

    @Override
    public synchronized double getProgress() {
        return trackIO.getSecondsPosition() + trackData.getSecsSeeked();
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
        return trackData.isMute();
    }

    @Override
    public void play() {
        if (isAlive())
            trackState = new PlayingState(player, this);
    }

    @Override
    public void pause() {
        if (isPlaying())
            trackState = new PausedState(player, this);
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
        if (isAlive() && (isPlaying() || isPaused()))
            trackState = new StoppedState(player, this);
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
            final long skip = trackIO.getDecodedStream().skip(bytesToSeek);
            final double skippedSeconds = convertBytesToSeconds(skip);

            if (skip > 0)
                trackData.setSecsSeeked(trackData.getSecsSeeked() + skippedSeconds);

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
            if (second > duration)
                second = duration;
            final int gt = (int) Math.round(second - getProgress());
            seek(gt);
        } else
            trackState = new ReverberatedState(player, this, second);
    }

    @Override
    public float getVolume() {
        return trackData.isMute() ? 0 : trackData.getVolume();
    }

    // -80 to 5.5
    @Override
    public void setVolume(float volume) {
        trackData.setVolume(volume);
        if (trackIO != null && trackIO.isTrackStreamsOpened()) {
            trackIO.setGain(AudioUtil.convertVolRangeToLineRange(volume));
            if (trackData.isVolumeZero())
                AudioHardware.setMuteValue(trackIO.getSpeakerDriver(), true);
        }
    }

    @Override
    public void mute() {
        trackData.setMute(true);
        if (trackIO != null && trackIO.isTrackStreamsOpened())
            AudioHardware.setMuteValue(trackIO.getSpeakerDriver(), trackData.isMute());
    }

    @Override
    public void unMute() {
        if (trackData.isVolumeZero()) {
            trackData.setVolume(100);
            if (trackIO != null && trackIO.isTrackStreamsOpened())
                trackIO.setGain(AudioUtil.convertVolRangeToLineRange(trackData.getVolume()));
        } else
            trackData.setMute(false);
        if (trackIO != null && trackIO.isTrackStreamsOpened())
            AudioHardware.setMuteValue(trackIO.getSpeakerDriver(), false);
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
        return trackIO.getDecodedStream().getFormat().toString();
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
        while (trackData.canTrackContinue()) {
            trackState.handle();
        }
    }
}