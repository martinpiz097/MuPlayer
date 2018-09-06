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
import org.muplayer.audio.model.TrackInfo;
import org.muplayer.audio.util.Time;
import org.muplayer.system.AudioUtil;
import org.muplayer.system.Logger;
import org.muplayer.thread.PlayerHandler;
import org.muplayer.thread.ThreadManager;

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
    protected volatile int available;
    protected volatile int currentSeconds;
    protected volatile long currentTime;
    protected volatile long readedBytes;
    protected volatile long bytesPerSecond;
    protected volatile float volume;
    protected volatile boolean isMute;
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
        } catch (InvalidAudioFrameException e) {
            e.printStackTrace();
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
        Logger.getLogger(this, "TrackFile: "+ dataSource.getParentFile().getName()+'/'+dataSource.getName())
                .rawInfo();
        this.dataSource = dataSource;
        state = STOPPED;
        currentSeconds = 0;
        initLine();
        try {
           if (isValidTrack()) {
               /*byte[] buffer = new byte[(int) dataSource.length()];
               int read = trackStream.read(buffer);
               buffer = null;
               System.out.println("StreamRead: "+read);
               System.out.println("DataSourceLenght: "+dataSource.length());*/
               initLine();

               available = trackStream.available();
               System.err.println("TrackAvailable: "+available);
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

    public void resetStream() throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        if (trackStream != null)
            trackStream.close();
        currentSeconds = 0;
        initLine();
        setGain(volume);
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
        else
            System.out.println("TrackStream null por tanto line is null");
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
        currentSeconds = 0;
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

    protected long transformSecondsInBytes(int seconds) {
        long secToBytes = Math.round((((double)seconds* dataSource.length()) / getDuration()));
        System.out.println("SecToBytes: "+secToBytes);
        return (long) (secToBytes);
    }

    protected long transformSecondsInBytes(int seconds, long soundSize) {
        long secToBytes = Math.round((((double)seconds* soundSize) / getDuration()));
        System.out.println("SecToBytes: "+secToBytes);
        return secToBytes;
    }

    public boolean isValidTrack() {
        return trackStream != null && trackLine != null;
    }

    /*public boolean isTrackFinished() throws IOException {
        return trackStream.read() == -1;
    }*/

    /*public boolean isPlayerLinked() {
        return PlayerHandler.hasInstance();
    }*/

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

    public synchronized int getProgress() {
        return currentSeconds;
    }

    // Ir a un segundo especifico de la cancion
    // inhabiliado si aplico freeze al pausar
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
        long skip = trackStream.skip(gtBytes);
        System.out.println("gtBytes: "+gtBytes);
        System.out.println("Skip: "+skip);

        currentSeconds = second;
    }

    public AudioFileFormat getFileFormat() throws IOException, UnsupportedAudioFileException {
        return audioReader == null ? null : audioReader.getAudioFileFormat(dataSource);
    }

    // Posible motivo de error para mas adelante
    public int getBuffLen() {
        long frameLen = trackStream == null ? BUFFSIZE : trackStream.getFrameLength();
        //Logger.getLogger(this, "FrameLenght: "+frameLen).rawInfo();
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
        /*if (state == PAUSED)
            this.interrupt();*/
        state = PLAYING;
    }

    @Override
    public void pause() {
        state = PAUSED;
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
        closeAllStreams();
    }

    void kill() {
        state = KILLED;
        closeAllStreams();
    }

    // en este caso pasan a ser bytes
    @Override
    public void seek(int bytes)
            throws IOException {
        long totalSkipped = 0;
        long skipped = 0;
        int SKIP_INACCURACY_SIZE = 1200;
        System.out.println("SecsInBytes: "+transformSecondsInBytes(bytes));
        while (totalSkipped < (bytes - SKIP_INACCURACY_SIZE)) {
            skipped = trackStream.skip(bytes - totalSkipped);
            System.out.println("Skipped: "+skipped);
            if (skipped == 0)
                break;
            totalSkipped +=skipped;
            System.out.println("InternalTotalSkipped: "+totalSkipped);
            if (totalSkipped == -1) {
                break;
            }
        }
        System.out.println("TotalSkipped: "+totalSkipped);

    }

    @Override
    public float getGain() {
        return isMute ? 0 : volume;
    }

    // -80 to 5.5
    @Override
    public void setGain(float volume) {
        this.volume = volume > 100 ? 100 : (volume < 0 ? 0 : volume);
        if (trackLine != null)
            trackLine.setGain(AudioUtil.convertVolRangeToLineRange(volume));
    }

    @Override
    public void mute() {
        if (!isMute) {
            isMute = true;
            setGain(0);
        }
    }

    @Override
    public void unmute() {
        if (isMute) {
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
            System.out.println("Solicitando "+key.name()+" nulo");
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
        String prop = getProperty(FieldKey.ALBUM);
        return prop == null ? null : prop;
    }

    @Override
    public String getArtist() {
        String prop = getProperty(FieldKey.ARTIST);
        return prop == null ? null : prop;
    }

    @Override
    public String getDate() {
        String prop = getProperty(FieldKey.YEAR);
        return prop == null ? null : prop;
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
    public String getInfoSong() {
        StringBuilder sbInfo = new StringBuilder();
        sbInfo.append("Title: ").append(getTitle()).append('\n');
        sbInfo.append("Album: ").append(getAlbum()).append('\n');
        sbInfo.append("Artist: ").append(getArtist()).append('\n');
        sbInfo.append("Date: ").append(getDate()).append('\n');
        sbInfo.append("Duration: ").append(getDurationAsString()).append('\n');
        sbInfo.append("Tiene carátula: ").append(hasCover()?"Si":"No").append('\n');
        sbInfo.append("Encoder: ").append(getEncoder()).append('\n');
        sbInfo.append("------------------------");
        /*File fileCover = new File("/home/martin/AudioTesting/test/trackCover.png");
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
        }*/

        return sbInfo.toString();
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
        try {
            boolean isPlayerLinked = PlayerHandler.hasInstance();
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
            long ti = Time.getInstance().getTime();
            readedBytes = 0;

            Logger logger = Logger.getLogger(this, null);
            //logger.setMsg(getInfoSong());
            logger.setMsg("CurrentTrack: "+getTitle());
            logger.rawWarning();

            logger.setMsg("Track Started!");
            logger.rawInfo();

            long tiAux = 0;

            //setGain(0);
            while (!isFinished() && !isKilled() && isValidTrack()) {
                while (isPlaying())
                    try {
                        read = trackStream.read(audioBuffer);
                        readedBytes+=read;
                        if (ThreadManager.hasOneSecond(ti)) {
                            //currentSeconds=(getSecondsByBytes(readedBytes));
                            currentSeconds++;
                            //ti = System.currentTimeMillis();
                            tiAux = ti;
                            ti = Time.getInstance().getTime();
                            currentTime += (ti-tiAux);
                            bytesPerSecond = readedBytes/currentSeconds;
                            // Rescatar time desde calendar
                            //System.out.println("CurrentTime: "+currentTime);
                            if (currentTime/1000 != currentSeconds) {
                                //System.out.println("Tiempos diferentes");
                                currentSeconds = (int) (currentTime / 1000);
                            }
                        }
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
                if (isStopped())
                    resetStream();
                Thread.sleep(10);
            }
            Logger.getLogger(this, "Track completed!").info();
            if (isFinished() && (PlayerHandler.hasInstance() && isPlayerLinked))
                PlayerHandler.getPlayer().playNext();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}

