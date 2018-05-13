package org.orangeplayer.ontesting;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;

import javax.sound.sampled.*;
import java.io.*;
import java.util.List;

public class TestAAC2 {
    public static void main(String[] args) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        //URL url = new URL("https://cdn.online-convert.com/example-file/audio/aac/example.aac");
        //InputStream is = url.openStream();

        String strTrack =
            //"/home/martin/AudioTesting/music/John Petrucci/When_The_Keyboard_Breaks_Live_In_Chicago/Universal_Mind.m4a";
            "/home/martin/AudioTesting/audio/aac3.aac";
        //Toolkit toolkit = Toolkit.getPCMConvertedAudioInputStream();
        File inputFile = new File(strTrack);
        //sound.createNewFile();

        //ByteBuffer buff = new ByteBuffer();
        //while(is.available() > 0) {
        //    buff.add(is.read());
        //}
        //Files.write(sound.toPath(),
        //        buff.toArray(), StandardOpenOption.TRUNCATE_EXISTING);

        AudioInputStream ais = decodeAAC(inputFile);
        System.out.println("Decoded");
        /*Speaker speaker = new Speaker(ais.getFormat());
        speaker.open();

        System.out.println(ais.getFrameLength());
        byte[] buff = new byte[1024];

        while (ais.read(buff) != -1)
            speaker.playAudio(buff);
*/
        File outFile = new File("/home/martin/AudioTesting/audio/waveout.wav");
        outFile.createNewFile();
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outFile);
    }

    private static AudioInputStream decodeAAC(File inputFile) throws AACException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AudioFormat audioFormat = null;

        try {
            final ADTSDemultiplexer adts = new ADTSDemultiplexer(new FileInputStream(inputFile));
            final Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
            final SampleBuffer buf = new SampleBuffer();
            byte[] b;
            while (true) {
                try {
                    b = adts.readNextFrame();
                }
                catch (Exception e) {
                    break;
                }

                dec.decodeFrame(b, buf);
                outputStream.write(buf.getData());
            }

            audioFormat = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, buf.isBigEndian());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // nop
        }

        byte[] outputStreamByteArray = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStreamByteArray);

        return new AudioInputStream(inputStream, audioFormat, outputStreamByteArray.length);
    }

    private static AudioInputStream decodeMP4(File inputFile) throws UnsupportedAudioFileException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        AudioFormat audioFormat = null;
        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(inputFile, "r");
            final MP4Container cont = new MP4Container(randomAccessFile);
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
                outputStream.write(buf.getData());
            }

            audioFormat = new AudioFormat(track.getSampleRate(), track.getSampleSize(), track.getChannelCount(), true, true);
        } finally {
            randomAccessFile.close();
        }

        byte[] outputStreamByteArray = outputStream.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStreamByteArray);

        return new AudioInputStream(inputStream, audioFormat, outputStreamByteArray.length);
    }

}
