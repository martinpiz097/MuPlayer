package cl.estencia.labs.muplayer.audio.common.enums;

import cl.estencia.labs.muplayer.core.util.CollectionUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// luego cargar desde un archivo
public enum SupportedAudioExtension {
    aifc("audio/x-aifc"),
    aiff("audio/aiff"),
    au("audio/basic"),
    flac("audio/flac"),
    mp3("audio/mpeg"),
    ogg("audio/ogg"),
    //    opus("audio/opus"),
//    pcm("audio/pcm"),
    snd("audio/basic"),
    wav("audio/wav");
//    m4a("audio/mp4"),
//    aac("audio/aac");

    private final String mimeType;

    SupportedAudioExtension(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return name();
    }

    public static boolean isSupported(String extension) {
        return fromExtension(extension) != null;
    }

    public static SupportedAudioExtension fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return null;
        }
        mimeType = mimeType.trim();
        for (SupportedAudioExtension format : values()) {
            if (format.mimeType.equalsIgnoreCase(mimeType)) {
                return format;
            }
        }
        return null;
    }

    public static SupportedAudioExtension fromExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return null;
        }
        String ext = extension.startsWith(".") ? extension.substring(1) : extension;
        try {
            return valueOf(ext.toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String[] getMimeTypes() {
        SupportedAudioExtension[] audioExtensions = SupportedAudioExtension.values();
        int extensionCount = audioExtensions.length;
        String[] mimeTypes = new String[extensionCount];

        for (int i = 0; i < extensionCount; i++) {
            mimeTypes[i] = audioExtensions[i].getMimeType();
        }

        return mimeTypes;
    }

}