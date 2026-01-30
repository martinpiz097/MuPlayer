package cl.estencia.labs.muplayer.audio.common.enums;

import java.util.Arrays;
import java.util.List;

public enum AudioFileExtension {
    aac("audio/aac"),
    aac_plus("audio/aac"),
    ac3("audio/ac3"),
    adts("audio/aac"),
    aif("audio/aiff"),
    aifc("audio/x-aifc"),
    aiff("audio/aiff"),
    alac("audio/mp4"),
    amr("audio/amr"),
    ape("audio/x-ape"),
    asf("audio/x-ms-asf"),
    au("audio/basic"),
    awb("audio/amr-wb"),
    caf("audio/x-caf"),
    cda("audio/x-cda"),
    dff("audio/x-dff"),
    dsd("audio/x-dsd"),
    dsf("audio/x-dsf"),
    dts("audio/vnd.dts"),
    dvf("audio/x-dvf"),
    eac3("audio/eac3"),
    flac("audio/flac"),
    gsm("audio/gsm"),
    it("audio/x-it"),
    kar("audio/midi"),
    m4a("audio/mp4"),
    m4b("audio/mp4"),
    m4p("audio/mp4"),
    m4r("audio/mp4"),
    mid("audio/midi"),
    midi("audio/midi"),
    mka("audio/x-matroska"),
    mod("audio/x-mod"),
    mp1("audio/mpeg"),
    mp2("audio/mpeg"),
    mp3("audio/mpeg"),
    mpa("audio/mpeg"),
    mpc("audio/x-musepack"),
    mpga("audio/mpeg"),
    oga("audio/ogg"),
    ogg("audio/ogg"),
    opus("audio/opus"),
    pcm("audio/pcm"),
    ra("audio/x-realaudio"),
    raw("audio/raw"),
    rm("audio/x-pn-realaudio"),
    s3m("audio/x-s3m"),
    shn("audio/x-shorten"),
    sln("audio/pcm"),
    snd("audio/basic"),
    spx("audio/ogg"),
    tak("audio/x-tak"),
    tta("audio/x-tta"),
    voc("audio/x-voc"),
    vox("audio/x-vox"),
    wav("audio/wav"),
    weba("audio/webm"),
    webm("audio/webm"),
    wma("audio/x-ms-wma"),
    wv("audio/x-wavpack"),
    xm("audio/x-xm");

    private final String mimeType;

    AudioFileExtension(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isLossless() {
        return switch (this) {
            case aiff, aif, aifc, alac, ape, flac, wav, wv, tak, tta, dsd, dsf, dff -> true;
            default -> false;
        };
    }

    public String getExtension() {
        return name();
    }

    public String getDescription() {
        return switch (this) {
            case aac, aac_plus, adts -> "Advanced Audio Coding";
            case ac3 -> "Dolby Digital";
            case aif, aifc, aiff -> "Audio Interchange File Format";
            case alac -> "Apple Lossless Audio Codec";
            case amr -> "Adaptive Multi-Rate";
            case ape -> "Monkey's Audio";
            case asf -> "Advanced Systems Format";
            case au, snd -> "Sun/NeXT Audio";
            case awb -> "AMR-WB Wideband";
            case caf -> "Core Audio Format";
            case cda -> "Compact Disc Audio";
            case dff, dsd, dsf -> "Direct Stream Digital";
            case dts -> "Digital Theater Systems";
            case dvf -> "Sony Digital Voice Format";
            case eac3 -> "Enhanced AC-3";
            case flac -> "Free Lossless Audio Codec";
            case gsm -> "GSM Audio";
            case it -> "Impulse Tracker Module";
            case kar, mid, midi -> "MIDI Audio";
            case m4a, m4b, m4p, m4r -> "MPEG-4 Audio";
            case mka -> "Matroska Audio";
            case mod -> "Module File";
            case mp1, mp2, mp3, mpa, mpga -> "MPEG Audio";
            case mpc -> "Musepack";
            case oga, ogg, spx -> "Ogg Audio";
            case opus -> "Opus Audio";
            case pcm, raw, sln -> "Raw PCM Audio";
            case ra, rm -> "RealAudio";
            case s3m -> "ScreamTracker Module";
            case shn -> "Shorten Audio";
            case tak -> "Tom's Audio Kompressor";
            case tta -> "True Audio";
            case voc -> "Creative Voice";
            case vox -> "Dialogic ADPCM";
            case wav -> "Waveform Audio";
            case weba, webm -> "WebM Audio";
            case wma -> "Windows Media Audio";
            case wv -> "WavPack";
            case xm -> "Extended Module";
        };
    }

    public static AudioFileExtension fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.isBlank()) {
            return null;
        }
        mimeType = mimeType.trim();
        for (AudioFileExtension format : values()) {
            if (format.mimeType.equalsIgnoreCase(mimeType)) {
                return format;
            }
        }
        return null;
    }

    public static AudioFileExtension fromExtension(String extension) {
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

    public static List<String> getMimeTypes() {
        return Arrays.stream(AudioFileExtension.values())
                .map(AudioFileExtension::getMimeType)
                .toList();
    }

}