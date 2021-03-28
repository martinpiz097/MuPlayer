package org.muplayer.audio.util;

public class AudioExtensions {
    public static final String MPEG = "mp3";
    public static final String OGG = "ogg";
    public static final String AAC = "aac";
    //public static final String AC3 = "ac3";
    public static final String FLAC = "flac";
    public static final String WAVE = "wav";
    public static final String M4A = "m4a";
    public static final String AIFF = "aiff";
    public static final String AIFC = "aifc";
    public static final String AU = "au";
    public static final String SND = "snd";
    public static final String SPEEX = "spx";

    public static String getFormatName(String fileName) {
        final String[] split = fileName.trim().split("\\.");
        return split.length > 0
                ? split[split.length-1]
                : "";
    }

}
