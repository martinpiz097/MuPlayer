

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

    public AudioInputStream getDecodedStream() {
        return decodedAis;
    }

    public void decode(File inFile) {
        //System.out.println("Decode [" + inFileName + "][" + outFileName + "]");
        ByteArrayInputStream bais = null;
        StreamInfo flacInfo = null;
        int dataLen = 0;

        try {
            FileInputStream fileIn = new FileInputStream(inFile);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            wav = new WavWriter(os);

            FLACDecoder decoder = new FLACDecoder(fileIn);
            decoder.addPCMProcessor(this);
            decoder.decode();

            byte[] audioData = os.toByteArray();
            bais = new ByteArrayInputStream(audioData);
            dataLen = audioData.length;
            audioData = null;

            flacInfo = decoder.getStreamInfo();

        }catch (IOException e) {
            //e.printStackTrace();
        }

        AudioFormat outFormat = new AudioFormat(flacInfo.getSampleRate(), flacInfo.getBitsPerSample(),
                flacInfo.getChannels(), true, false);

        // La otra opcion para no tener que usar tanta ram
        // seria escribir los datos en un archivo y despues ir leyendolo para al final borrarlo
        decodedAis = new AudioInputStream(bais, outFormat, dataLen);
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
