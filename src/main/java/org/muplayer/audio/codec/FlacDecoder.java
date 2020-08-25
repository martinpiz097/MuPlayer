

package org.muplayer.audio.codec;


import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;
import org.jflac.util.WavWriter;

import javax.sound.sampled.AudioInputStream;
import java.io.*;

/**
 * Decode FLAC file to WAV file application.
 * @author kc7bfi
 */
public class FlacDecoder implements PCMProcessor {
    private final WavWriter wav;
    private final FileInputStream inputFile;
    private final ByteArrayOutputStream outStream;
    private AudioInputStream decodedAis;
    private ByteArrayInputStream audioIn;
    private StreamInfo flacInfo;

    public FlacDecoder(File inFile) throws FileNotFoundException {
        inputFile = new FileInputStream(inFile);
        outStream = new ByteArrayOutputStream();
        wav = new WavWriter(outStream);
    }

    public boolean isFlac() {
        try {
            FLACDecoder decoder = new FLACDecoder(inputFile);
            decoder.addPCMProcessor(this);
            decoder.decode();

            audioIn = new ByteArrayInputStream(outStream.toByteArray());
            flacInfo = decoder.getStreamInfo();
            return flacInfo != null;
        } catch (IOException e) {
            return false;
        }
    }

    public StreamInfo getFlacInfo() {
        return flacInfo;
    }

    public AudioInputStream getDecodedStream() {
        return decodedAis;
    }

    public void decode() {
        if (flacInfo != null) {
            // La otra opcion para no tener que usar tanta ram
            // seria escribir los datos en un archivo y despues ir leyendolo para al final borrarlo
            decodedAis = new AudioInputStream(audioIn, flacInfo.getAudioFormat(), audioIn.available());
        }
    }

    public void processStreamInfo(StreamInfo info) {
        try {
            wav.writeHeader(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processPCM(ByteData pcm) {
        try {
            wav.writePCM(pcm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
