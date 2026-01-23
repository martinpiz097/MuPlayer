package cl.estencia.labs.muplayer.core.aucom.common;

public class AudioConstants {
    public static final float DEFAULT_MAX_LINE_VOL = 0.855f;
    public static final float DEFAULT_MIN_LINE_VOL = -80f;

    public static final float DEFAULT_MIN_VOL = 0;
    public static final float DEFAULT_MAX_VOL = 100;
    public static final float DEFAULT_VOL_RANGE = DEFAULT_MAX_VOL - DEFAULT_MIN_VOL;

    public static final float VOL_RANGE = DEFAULT_MAX_LINE_VOL - DEFAULT_MIN_LINE_VOL;
    public static final float MIDDLE_VOL = VOL_RANGE / 2;
}
