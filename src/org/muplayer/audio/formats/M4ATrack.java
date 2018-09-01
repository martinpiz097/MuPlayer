package org.muplayer.audio.formats;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import org.muplayer.audio.Track;
import org.muplayer.audio.formats.io.M4AInputStream;
import org.muplayer.system.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class M4ATrack extends Track {

    private M4AInputStream m4aStream;

    public M4ATrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public M4ATrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        try {
            // Es m4a normal o aac
            audioReader = new AACAudioFileReader();
            trackStream = audioReader.getAudioInputStream(dataSource);

            // Para leer progreso en segundos de vorbis(posible opcion)

            //OggInfoReader info = new OggInfoReader();
            //FlacStreamReader streamReader = new FlacStreamReader();

        } catch (UnsupportedAudioFileException e) {
            Logger.getLogger(this, "File not supported!").rawError();
            e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("LoadAudioStreamIOException: "+e.getMessage());
            trackStream = decodeRandomAccessMP4(dataSource);
            //e.printStackTrace();
        }
        // Probar despues transformando a PCM
        //MPG4Codec codec = new MPG4Codec();
        //MP42Codec c = new MP42Codec();
        //MP4InputStream mp4 = new MP4InputStream();
    }

    /*private AudioInputStream decodeRandomAccessMP4(File inputFile) throws IOException,
            UnsupportedAudioFileException {
        RandomAccessFile randomAccess = new RandomAccessFile(inputFile, "r");
        final MP4Container cont = new MP4Container(randomAccess);
        final List<net.sourceforge.jaad.mp4.api.Track> tracks =
                cont.getMovie().getTracks(AudioTrack.AudioCodec.AAC);
        if (tracks.isEmpty())
            throw new UnsupportedAudioFileException("Movie does not contain any AAC track");

        final AudioTrack track = (AudioTrack) tracks.get(0);
        return new M4AAudioInputStream(inputFile, new AudioDataInputStream(), track);
    }*/

    private AudioTrack getM4ATrack(RandomAccessFile randomAccess) throws IOException, UnsupportedAudioFileException {
        final MP4Container cont = new MP4Container(randomAccess);
        final Movie movie = cont.getMovie();
        final List<net.sourceforge.jaad.mp4.api.Track> tracks =
                movie.getTracks(AudioTrack.AudioCodec.AAC);

        if (tracks.isEmpty())
            throw new UnsupportedAudioFileException("Movie does not contain any AAC track");

        return (AudioTrack) tracks.get(0);
    }

    private AudioInputStream decodeRandomAccessMP4(File inputFile) {
        try {
            final RandomAccessFile randomAccess = new RandomAccessFile(inputFile, "r");
            final AudioTrack track = getM4ATrack(randomAccess);
            final Decoder dec = new Decoder(track.getDecoderSpecificInfo());
            m4aStream = new M4AInputStream(track, dec, randomAccess);
            AudioFormat decFormat = new AudioFormat(track.getSampleRate(),
                    track.getSampleSize(), track.getChannelCount(),
                    true, true);
            return new AudioInputStream(m4aStream, decFormat, inputFile.length());
        } catch (Exception e){
            Logger.getLogger(this, "Exception", e.getMessage()).error();
            return null;
        }
    }

    /*private long getSoundSize() {
        long size = (long) ((decodedStream.getFormat().getSampleRate()/8)*getDuration());
        System.out.println("SoundSize: "+size);
        return size;
    }*/

    @Override
    public void seek(int seconds) throws IOException {
        if (m4aStream == null) {
            //long seek = transformSecondsInBytes(seconds);
            long seek = bytesPerSecond == 0 ? seconds*180000: seconds*bytesPerSecond;
            pause();
            trackStream.skip(seek);
            resumeTrack();
            currentSeconds+=seconds;
            System.out.println("Properties");
        }
        else {
            pause();
            m4aStream.skip(seconds);
            currentSeconds+=seconds;
            resumeTrack();
        }
    }

    @Override
    public int getProgress() {
        return m4aStream == null ? super.getProgress() : m4aStream.getTime();
    }

    // Ver si duracion mostrada es real antes de entregar valor en segundos
    /*@Override
    public long getDuration() {
        String strDuration = getProperty("duration");
        return strDuration == null ? 0 : Long.parseLong(strDuration);
    }

    @Override
    public String getDurationAsString() {
        long sec = getDuration() / 1000/1000;
        long min = sec / 60;
        sec = sec-(min*60);
        return new StringBuilder().append(min)
                .append(':').append(sec < 10 ? '0'+sec:sec).toString();
    }*/

    /*public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        String strTrack = "/home/martin/AudioTesting/music/random.m4a";
        Track track = new M4ATrack(strTrack);
        //String strTrack = "/home/martin/AudioTesting/music/John Petrucci/" +
        //      "When_The_Keyboard_Breaks_Live_In_Chicago/Universal_Mind.m4a";
        //Track track = Track.getTrack(strTrack);
        //Track track = new PCMTrack(strTrack);
        new Thread(track).start();
    }    //System.out.println(track.getInfoSong());
        /*
        ID3TagBox idTag = new ID3TagBox();
        Constructor<? extends MP4InputStream> constructor =
                MP4InputStream.class.getConstructor(RandomAccessFile.class);
        constructor.setAccessible(true);
        idTag.decode(constructor.newInstance(new RandomAccessFile(strTrack, "r")));
        System.out.println(Arrays.toString(idTag.getID3Data()));*/


        /*File file = new File(strTrack);
        AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
        Map properties = baseFileFormat.properties();
        long duration = Long.parseLong(properties.get("duration").toString());
        System.out.println(duration);*/
        /*File m4aFile = new File(strTrack);

        try (InputStream input = new FileInputStream(m4aFile)) {
            AudioTag audioInfo = new M4AInfo(input);
        } catch (Exception e) {
            System.out.println("No sirve");
        }

    }
    */

}
