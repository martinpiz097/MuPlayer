package cl.estencia.labs.muplayer.core.common.enums;

public enum AudioFileExtension {
    aac,    // Advanced Audio Coding
    aac_plus, // AAC+ / HE-AAC
    ac3,    // Dolby Digital
    adts,   // Audio Data Transport Stream
    aif,    // Audio Interchange File Format (variante)
    aifc,   // Compressed AIFF
    aiff,   // Audio Interchange File Format
    alac,   // Apple Lossless Audio Codec
    amr,    // Adaptive Multi-Rate
    ape,    // Monkey's Audio
    asf,    // Advanced Systems Format
    au,     // Sun Audio
    awb,    // AMR-WB (Adaptive Multi-Rate Wideband)
    caf,    // Core Audio Format
    cda,    // Compact Disc Audio
    dff,    // Direct Stream Digital File
    dsd,    // Direct Stream Digital
    dsf,    // DSD Storage Facility
    dts,    // Digital Theater Systems
    dvf,    // Sony Digital Voice Format
    eac3,   // Enhanced AC-3
    flac,   // Free Lossless Audio Codec
    gsm,    // Global System for Mobile Communications
    it,     // Impulse Tracker Module
    kar,    // Karaoke MIDI
    m4a,    // MPEG-4 Audio
    m4b,    // MPEG-4 Audiobook
    m4p,    // MPEG-4 Protected Audio
    m4r,    // iPhone Ringtone
    mid,    // Musical Instrument Digital Interface
    midi,   // Musical Instrument Digital Interface (completo)
    mka,    // Matroska Audio
    mod,    // Module File
    mp1,    // MPEG-1 Audio Layer I
    mp2,    // MPEG-1 Audio Layer II
    mp3,    // MPEG-1/2 Audio Layer III
    mpa,    // MPEG Audio
    mpc,    // Musepack
    mpga,   // MPEG-1 Audio
    oga,    // Ogg Audio
    ogg,    // Ogg Vorbis
    opus,   // Opus Audio Format
    pcm,    // Pulse Code Modulation (raw)
    ra,     // RealAudio
    raw,    // Raw audio data
    rm,     // RealMedia
    s3m,    // ScreamTracker 3 Module
    shn,    // Shorten
    sln,    // Signed Linear PCM
    snd,    // Sound
    spx,    // Speex
    tak,    // Tom's lossless Audio Kompressor
    tta,    // True Audio
    voc,    // Creative Voice
    vox,    // Dialogic ADPCM
    wav,    // Waveform Audio File Format
    weba,   // WebM Audio
    webm,   // Web Media Audio
    wma,    // Windows Media Audio
    wv,     // WavPack
    xm;     // Extended Module

    /**
     * Obtiene la extensión de archivo asociada con este formato de audio.
     * @return La extensión de archivo (sin el punto)
     */
    public String getExtension() {
        return this.name();
    }

    /**
     * Verifica si el formato es de tipo lossless (sin pérdida).
     * @return true si es un formato sin pérdida, false en caso contrario
     */
    public boolean isLossless() {
        return switch(this) {
            case aiff, alac, ape, flac, wav, wv, tak, tta -> true;
            default -> false;
        };
    }

    /**
     * Devuelve el formato de audio correspondiente a una extensión de archivo.
     * @param extension Extensión de archivo (sin el punto)
     * @return El formato de audio correspondiente o null si no se encuentra
     */
    public static AudioFileExtension fromExtension(String extension) {
        if (extension == null) return null;

        String normalizedExtension = extension.toLowerCase().trim();
        if (normalizedExtension.startsWith(".")) {
            normalizedExtension = normalizedExtension.substring(1);
        }

        try {
            return AudioFileExtension.valueOf(normalizedExtension);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Devuelve una descripción legible del formato de audio.
     * @return Descripción del formato
     */
    public String getDescription() {
        return switch(this) {
            case aac -> "Advanced Audio Coding";
            case aiff, aif -> "Audio Interchange File Format";
            case alac -> "Apple Lossless Audio Codec";
            case flac -> "Free Lossless Audio Codec";
            case mp3 -> "MPEG-1/2 Audio Layer III";
            case ogg -> "Ogg Vorbis Audio";
            case wav -> "Waveform Audio File Format";
            case wma -> "Windows Media Audio";
            // ... otros casos
            default -> this.name().toUpperCase();
        };
    }
}