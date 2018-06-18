package org.muplayer.audio.formats;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import org.muplayer.audio.Track;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.lang.reflect.Field;
import java.util.List;

public class M4ATrack extends Track {
    public M4ATrack(File ftrack) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(ftrack);
    }

    public M4ATrack(String trackPath) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        super(trackPath);
    }

    @Override
    protected void loadAudioStream() throws IOException, UnsupportedAudioFileException {
        try {
            // Es m4a normal
            audioReader = new AACAudioFileReader();
            trackStream = audioReader.getAudioInputStream(dataSource);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("No soportado");
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

    private AudioInputStream decodeRandomAccessMP4(File inputFile)
            throws UnsupportedAudioFileException, IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        AudioFormat decFormat = null;
        RandomAccessFile randomAccess = null;
        byte[] audioData = null;

        try {
            randomAccess = new RandomAccessFile(inputFile, "r");
            Field fd = FileDescriptor.class
                    .getDeclaredField("fd");
            fd.setAccessible(true);
            System.out.println("Decriptor: "+fd.getInt(randomAccess.getFD()));
            final MP4Container cont = new MP4Container(randomAccess);
            final Movie movie = cont.getMovie();
            final List<net.sourceforge.jaad.mp4.api.Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
            if (tracks.isEmpty())
                throw new UnsupportedAudioFileException("Movie does not contain any AAC track");

            final AudioTrack track = (AudioTrack) tracks.get(0);
            final Decoder dec = new Decoder(track.getDecoderSpecificInfo());
            Frame frame;
            final SampleBuffer buf = new SampleBuffer();

            while (track.hasMoreFrames()) {
                frame = track.readNextFrame();
                dec.decodeFrame(frame.getData(), buf);
                byteOut.write(buf.getData());
            }

            decFormat = new AudioFormat(track.getSampleRate(),
                    track.getSampleSize(), track.getChannelCount(),
                    true, true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            audioData = byteOut.toByteArray();
            byteOut.close();
            randomAccess.close();
        }

       if (audioData != null) {
           ByteArrayInputStream inputStream = new ByteArrayInputStream(audioData);
           return new AudioInputStream(inputStream, decFormat, audioData.length);
       }
       return null;
    }

    @Override
    public void seek(int seconds) throws IOException {
        long seek = transformSecondsInBytes(seconds);
        trackStream.read(new byte[(int) seek]);
        currentSeconds+=seconds;
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

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
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
