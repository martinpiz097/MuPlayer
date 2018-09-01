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

    private MP3AudioHeader audioHeader;

    private long audioStartByte;
    private long audioSize;
    private long frameCount;
    private long frameSize;
    double frameDurationInSec;


    public MP3Track(File ftrack) throws IOException,
            UnsupportedAudioFileException, LineUnavailableException, InvalidAudioFrameException {
        super(ftrack);
        audioHeader = new MP3AudioHeader(ftrack);
        audioStartByte = audioHeader.getMp3StartByte();
        audioSize = dataSource.length() - audioStartByte;
        frameCount = audioHeader.getNumberOfFrames();
        frameSize = audioSize / frameCount;
        frameDurationInSec = (audioHeader.getPreciseTrackLength() / (double) frameCount);
        System.out.println("StartByte: "+audioStartByte);
        System.out.println("Mp3StartByte: "+audioHeader.getMp3StartByte());
        System.out.println("AudioSize: "+audioSize);
        System.out.println("FrameCount: "+frameCount);
        System.out.println("FrameSize: "+frameSize);
        System.out.println("FrameDuration: "+frameDurationInSec);
        System.out.println("TrackLenght: "+audioHeader.getTrackLength());
        System.out.println("PreciseTrackLenght: "+audioHeader.getPreciseTrackLength());
        System.out.println("AudioDataLenght: "+audioHeader.getAudioDataLength());
        System.out.println("LenghtAsString"+audioHeader.getTrackLengthAsString());

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

    private long getBytesToSeek(int sec) {
        int frameNeeded = (int) (sec / frameDurationInSec);
        return frameNeeded*frameSize;
    }

    private int getSecondsFromBytes(long bytes) {
        int frames = (int) (bytes / frameSize);
        return (int) Math.round(frames*frameDurationInSec);
    }

    @Override
    public void seek(int seconds) throws IOException {
        //double framesForSecs = seconds / frameDurationInSec;
        //long bytePositionForSec = (long) (audioStartByte + (framesForSecs * frameSize));
        long seek = getBytesToSeek(seconds);
        trackStream.skip(seek);
        currentSeconds+=seconds;
        readedBytes+=seek;
        System.out.println("AudioDataStart: "+audioHeader.getAudioDataStartPosition());
        System.out.println("AudioDataEnd: "+audioHeader.getAudioDataEndPosition());
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

}
