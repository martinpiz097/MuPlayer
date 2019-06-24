package org.muplayer.audio.util;

import java.util.ArrayList;

import static org.muplayer.system.SysInfo.ISMAC;
import static org.muplayer.system.SysInfo.ISUNIX;

public class AudioExtensions {
    public static final String MPEG = ".mp3";
    public static final String OGG = ".ogg";
    public static final String AAC = ".aac";
    public static final String AC3 = ".ac3";
    public static final String FLAC = ".flac";
    public static final String WAVE = ".wav";
    public static final String M4A = ".m4a";
    public static final String AIFF = ".aiff";
    public static final String AIFC = ".aifc";
    public static final String AU = ".au";
    public static final String SND = ".snd";
    //public static final String SPEEX = ".spx";

    public static String[] SUPPORTEDEXTENSIONS = {};

    static {
        ArrayList<String> listSupportedFormats = new ArrayList<>();
        listSupportedFormats.add(MPEG);
        listSupportedFormats.add(OGG);
        listSupportedFormats.add(AAC);
        listSupportedFormats.add(AC3);
        listSupportedFormats.add(FLAC);
        listSupportedFormats.add(WAVE);
        listSupportedFormats.add(M4A);
        listSupportedFormats.add(SND);
        if (ISUNIX)
            listSupportedFormats.add(AU);
        if (ISMAC) {
            listSupportedFormats.add(AIFF);
            listSupportedFormats.add(AIFC);
        }
        //listSupportedFormats.add(SPEEX);
        int supportedSize = listSupportedFormats.size();
        SUPPORTEDEXTENSIONS = new String[supportedSize];
        for (int i = 0; i < supportedSize; i++)
            SUPPORTEDEXTENSIONS[i] = listSupportedFormats.get(i);
    }

}
