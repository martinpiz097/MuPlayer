package org.muplayer.audio.format.io;

import net.sourceforge.jaad.aac.AACException;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;

import java.io.File;
import java.io.IOException;

public class TM4AReader extends Thread{
    private File inputFile;
    private AudioDataOutputStream outputStream;
    private AudioTrack audioTrack;
    private Decoder dec;
    private SampleBuffer buff;
    private Frame frame;

    public TM4AReader(File inputFile, AudioDataOutputStream outputStream, AudioTrack audioTrack) throws AACException {
        this.inputFile = inputFile;
        this.outputStream = outputStream;
        this.audioTrack = audioTrack;
        dec = new Decoder(audioTrack.getDecoderSpecificInfo());
        buff = new SampleBuffer();
        setName("TM4AReader: "+inputFile.getName());
        setPriority(MAX_PRIORITY);
    }

    private byte[] getNextDecodedFrame() throws IOException {
        frame = audioTrack.readNextFrame();
        dec.decodeFrame(frame.getData(), buff);
        return buff.getData();
    }

    @Override
    public void run() {
        while (audioTrack.hasMoreFrames()) {
            try {
                outputStream.write(getNextDecodedFrame());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(getName() +" finished!");
    }

}
