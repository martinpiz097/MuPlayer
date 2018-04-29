package org.orangeplayer.test;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;
import net.sourceforge.jaad.util.wav.WaveFileWriter;
import org.aucom.sound.Speaker;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.util.LinkedList;

public class TestAAC2 {
    public static void main(String[] args) throws IOException, LineUnavailableException {
        File sound = new File("/home/martin/AudioTesting/audio/aac.aac");
        File outFile = new File(sound.getParent(), "waveout.wav");
        outFile.createNewFile();

        String in = sound.getCanonicalPath();
        String out = outFile.getCanonicalPath();

        AudioInputStream result = null;

        /* We cascade different methods here since every one is usable for another type of aac file. */
        try {
            result = new AACAudioFileReader().getAudioInputStream(sound.toURL());
        }
        catch (Exception e) {
            System.out.println("No sirve AACAudioFileReader");
        }

        if (result == null) {
            System.out.println("Resultado nulo");
        }

        WaveFileWriter wav = null;

        final ADTSDemultiplexer adts = new ADTSDemultiplexer(new FileInputStream(in));
        final Decoder dec = new Decoder(adts.getDecoderSpecificInfo());

        final SampleBuffer buf = new SampleBuffer();
        byte[] b;
        LinkedList<byte[]> listData = new LinkedList<>();

        AudioFormat format = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        outputStream.write(adts.getDecoderSpecificInfo());

        while(true) {
            try {
                b = adts.readNextFrame();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            dec.decodeFrame(b, buf);
            if(wav==null) {
                wav = new WaveFileWriter(
                        new File(out), buf.getSampleRate(), buf.getChannels(), buf.getBitsPerSample());
            }
            //b = buf.getData();
            outputStream.write(b);
        }
        format = new AudioFormat(buf.getSampleRate(),
                buf.getBitsPerSample(), buf.getChannels(), true, buf.isBigEndian());

        byte[] outBuff = outputStream.toByteArray();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outBuff);
        AudioInputStream ais = new AudioInputStream(inputStream, format, outBuff.length);

        Speaker speaker = new Speaker(ais.getFormat());
        speaker.open();

        byte[] buffer = new byte[128];

        System.out.println("Playing");
        while(ais.read(buffer) != -1) {
            speaker.playAudio(buffer);
        }

    }
}
