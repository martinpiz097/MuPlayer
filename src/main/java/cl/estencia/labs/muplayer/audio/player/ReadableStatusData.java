package cl.estencia.labs.muplayer.audio.player;

public record ReadableStatusData(
        int currentTrackIndex,
        int newTrackIndex,
        float volume,
        boolean on,
        boolean isMute
) {}
