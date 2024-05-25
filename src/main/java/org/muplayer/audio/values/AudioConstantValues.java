package org.muplayer.audio.values;

public class AudioConstantValues {
    public static final float MAX_VOL = 0.855f;
    public static final float MIN_VOL = -80f;

    public static final float DEFAULT_MIN_VOL = 0;
    public static final float DEFAULT_MAX_VOL = 100;
    public static final float DEFAULT_VOL_RANGE = DEFAULT_MAX_VOL - DEFAULT_MIN_VOL;

    public static final float VOL_RANGE = MAX_VOL - MIN_VOL;
    public static final float MiDDLE_VOL = VOL_RANGE / 2;
}
