package cl.estencia.labs.muplayer.muplayer.audio.values;

public class AudioConstantValues {

    private AudioConstantValues() {
        throw new IllegalStateException("Utility class");
    }

    public static final float MAX_VOL = 0.855f;
    public static final float MIN_VOL = -80f;

    public static final float DEFAULT_MIN_VOL = 0;
    public static final float DEFAULT_MAX_VOL = 100;
    public static final float DEFAULT_VOL_RANGE = DEFAULT_MAX_VOL - DEFAULT_MIN_VOL;

    public static final float VOL_RANGE = MAX_VOL - MIN_VOL;
    public static final float MIDDLE_VOL = VOL_RANGE / 2;
}
