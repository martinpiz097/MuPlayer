package org.muplayer.audio.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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

    public static final List<String> SUPPORTED_EXTENSIONS_LIST = new ArrayList<>();

    static {
        final Field[] fields = AudioExtensions.class.getFields();

        Field field;
        for (int i = 0; i < fields.length; i++) {
            field = fields[i];
            if (field.getType().equals(String.class)) {
                try {
                    SUPPORTED_EXTENSIONS_LIST.add(field.get(null).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getFormatName(String fileName) {
        final String[] split = fileName.trim().split("\\.");
        return split.length > 0
                ? split[split.length-1]
                : "";
    }

}
