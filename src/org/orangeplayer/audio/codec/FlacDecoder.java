

package org.orangeplayer.audio.codec;

import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import org.kc7bfi.jflac.util.WavWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.*;

/**
 * Decode FLAC file to WAV file application.
 * @author kc7bfi
 */
public class FlacDecoder implements PCMProcessor {
    private WavWriter wav;
    private AudioInputStream decodedAis;
    private FileInputStream inputFile;
    private ByteArrayOutputStream outStream;
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
            //e.printStackTrace();
            return false;
        }
    }

    public AudioInputStream getDecodedStream() {
        return decodedAis;
    }

    public void decode() {
        if (flacInfo != null) {
            AudioFormat outFormat = new AudioFormat(flacInfo.getSampleRate(),
                    flacInfo.getBitsPerSample(), flacInfo.getChannels(), true, false);

            // La otra opcion para no tener que usar tanta ram
            // seria escribir los datos en un archivo y despues ir leyendolo para al final borrarlo
            decodedAis = new AudioInputStream(audioIn, outFormat, audioIn.available());
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
