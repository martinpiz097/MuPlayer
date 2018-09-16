package org.muplayer.audio.formats;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.muplayer.audio.Track;
import org.muplayer.audio.codec.DecodeManager;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class MP3Track extends Track {

    private volatile MP3AudioHeader audioHeader;

    private volatile long audioStartByte;
    private volatile long audioSize;
    private volatile long frameCount;
    private volatile long frameSize;
    private volatile double frameDurationInSec;


    public MP3Track(File ftrack) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InvalidAudioFrameException {
        super(ftrack);
        audioHeader = new MP3AudioHeader(ftrack);
        audioStartByte = audioHeader.getMp3StartByte();
        audioSize = dataSource.length() - audioStartByte;
        frameCount = audioHeader.getNumberOfFrames();
        frameSize = audioSize / frameCount;
        System.out.println("MP3FrameSize: "+frameSize);
        System.out.println("TrackFrameSize: "+getAudioFormat().getFrameSize());

        frameDurationInSec = (audioHeader.getPreciseTrackLength() / (double) frameCount);
        /*System.out.println("AISFrameSize: "+trackStream.getFormat().getFrameSize());
        System.out.println("FrameSize: "+frameSize);
        System.out.println("FrameLenght: "+trackStream.getFrameLength());
        System.out.println("FrameDuration: "+frameDurationInSec);*/
        /*System.out.println("StartByte: "+audioStartByte);
        System.out.println("Mp3StartByte: "+audioHeader.getMp3StartByte());
        System.out.println("AudioSize: "+audioSize);
        System.out.println("AudioSize2: "+frameSize*frameCount);
        System.out.println("FrameCount: "+frameCount);
        System.out.println("FrameSize: "+frameSize);
        System.out.println("FrameDuration: "+frameDurationInSec);
        System.out.println("TrackLenght: "+audioHeader.getTrackLength());
        System.out.println("PreciseTrackLenght: "+audioHeader.getPreciseTrackLength());
        System.out.println("AudioDataLenght: "+audioHeader.getAudioDataLength());
        System.out.println("LenghtAsString: "+audioHeader.getTrackLengthAsString());*/

    }

    public MP3Track(String trackPath)
            throws UnsupportedAudioFileException, IOException,
            LineUnavailableException, InvalidAudioFrameException {
        this(new File(trackPath));
    }

    public boolean isValidTrack() {
        return trackStream != null;
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        // Ver si se escucha mejor en ogg utilizando la logica de mp3
        audioReader = new MpegAudioFileReader();
        AudioInputStream soundAis = audioReader.getAudioInputStream(dataSource);
        AudioFormat baseFormat = soundAis.getFormat();
        if (trackStream != null)
            trackStream.close();
        trackStream = DecodeManager.decodeToPcm(baseFormat, soundAis);
    }

    private long getBytesToSeek(double sec) {
        double frameNeeded = sec / frameDurationInSec;
        return (long) (frameNeeded*frameSize);
    }

    // bytesLeidos -> bytesTotales
    // segundosLeidos(x) -> segundosTotales
    // bytesLeidos*segundosTotales
    // ---------------------------
    //     bytesTotales

    // framesLeidos(x) --> framesTotales
    // bytesLeidos-startByte --> bytesTotales
    //
    // --------------------------------------
    //
    // framesLeidos --> framesTotales
    // secsLeidos(x) --> secsTotales

    // Despues probar con duracion de frames en segundos y antes
    // de eso con la duracion precisa

    // nombres de variables dados por iniciales
    private int getSecondsFromBytes(long bytes) {
        bytes -= audioStartByte;
        long btxft = bytes*frameCount;
        long framesReaded = btxft/audioSize;
        long frxst = (long) (framesReaded*audioHeader.getPreciseTrackLength());
        return (int) (frxst / frameCount);
    }

    @Override
    public void seek(double seconds) throws IOException {
        //double framesForSecs = seconds / frameDurationInSec;
        //long bytePositionForSec = (long) (audioStartByte + (framesForSecs * frameSize));
        long seek = getBytesToSeek(seconds);
        mute();
        trackStream.skip(seek);
        unmute();
        secsSeeked+=seconds;
        readedBytes+=seek;
    }

    @Override
    public void gotoSecond(double second) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        double progress = getProgress();
        if (second >= progress) {
            int gt = (int) Math.round(second-getProgress());
            seek(gt);
        }
        else if (second < progress) {
            stopTrack();
            resumeTrack();
            seek(second);
        }
    }

    /*@Override
    public int getProgress() {
        return (int) getSecondsFromBytes(readedBytes);
    }*/

    /*@Override
    public long getDuration() {
        String strDuration = getProperty("duration");
        return strDuration == null ? 0 :
                Long.parseLong(strDuration) / 1000 / 1000;
    }

    @Override
    public String getDurationAsString() {
        long sec = getDuration();
        long min = sec / 60;
        sec = sec-(min*60);
        return new StringBuilder().append(min)
                .append(':').append(sec < 10 ? '0'+sec:sec).toString();
    }*/

    // Una vez obtenidas todas las duraciones por formato
    // el metodo seek sera universal

    // Libreria AAC genera problemas con archivos mp3 y ogg
    /*public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
        File sound = new File("/home/martin/AudioTesting/audio/au.mp3");
        //System.out.println(new MpegAudioFileReader().getAudioFileFormat(sound).getType().toString());
        //System.out.println(new JorbisAudioFileReader().getAudioFileFormat(sound).getType().toString());
        //System.out.println(AudioSystem.getAudioFileFormat(sound).getType().toString());

        System.out.println(AudioSystem.getAudioFileFormat(sound).toString());

        Track track = new MP3Track(sound);
        Thread tTrack = new Thread(track);
        tTrack.start();

        Thread.sleep(300000);

        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(sound);
        System.out.println(AudioSystem.isConversionSupported(
                AudioFormat.Encoding.PCM_SIGNED, fileFormat.getFormat()));
        //Thread.sleep(3000);
        //track.pause();
        //Thread.sleep(3000);
        //track.resume();
    }
    */

    /*@Override
    public void run() {
        try {
            setGain(0);
            boolean isPlayerLinked = PlayerHandler.hasInstance();
            byte[] audioBuffer = new byte[getBuffLen()];
            int read;
            play();

            long ti = System.currentTimeMillis();
            readedBytes = 0;

            frameSize = trackStream.getFormat().getFrameSize();
            long secs = 0;
            while (!isFinished() && !isKilled() && isValidTrack()) {
                while (isPlaying())
                    try {
                        read = trackStream.read(audioBuffer);
                        readedBytes+=read;
                        if (ThreadManager.hasOneSecond(ti)) {
                            int readedFrames = (int) (readedBytes/frameSize);
                            secsSeeked++;
                            ti = System.currentTimeMillis();
                            bytesPerSecond = readedBytes/secsSeeked;
                            System.out.println("BytesReaded: "+readedBytes);
                            System.out.println("FrameDuration: "+frameDurationInSec);
                            System.out.println("FrameSize: "+frameSize);
                            System.out.println("ReadedFrames: "+readedFrames);
                            secs = ((long)(frameDurationInSec*readedFrames))/1000;
                            System.out.println("Seconds: "+secs);
                            System.out.println("CurrentSeconds: "+secsSeeked);
                            System.out.println("----------------------------");
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
*/
}
