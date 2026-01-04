package cl.estencia.labs.muplayer.console.util;

import cl.estencia.labs.muplayer.audio.model.PlayerStatusData;
import lombok.extern.slf4j.Slf4j;

import static cl.estencia.labs.muplayer.console.common.constants.ConsoleSymbols.*;

@Slf4j
public class ConsolePainter {

    private static String paintVolumeIcon(PlayerStatusData playerStatusData) {
        if (playerStatusData.isMute()) {
            return MUTED;
        }

        int volumeInt = (int) playerStatusData.getVolume();
        if (volumeInt >= 80) {
            return HIGH_VOLUME;
        }
        if (volumeInt >= 40) {
            return MED_VOLUME;
        }
        else if (volumeInt > 0) {
            return LOW_VOLUME;
        }
        else {
            return MUTED;
        }
    }

    private static String paintVolumePercent(Number volume) {
        return volume.intValue() + "%";
    }

    public static String paintVolumeStatus(PlayerStatusData playerStatusData) {
        String icon = paintVolumeIcon(playerStatusData);
        String percentage = paintVolumePercent(playerStatusData.getVolume());

        return icon + SPACE + percentage;
    }

    public static String paintVolumeBar(Number volume, int scale) {
        final StringBuilder sbVolume = new StringBuilder();

//        █████░░░ 60%

        // por si por error tengo volumenes mayores a 100
        final int adjustedVol = Math.min(volume.intValue(), 100);
        final int scaledVol = Math.toIntExact(Math.round(((float) (scale * adjustedVol)) / 100));

        for (int i = 0; i < scaledVol; i++) {
            sbVolume.append(COMPLETE_BLOCK);
        }

        for (int i = scaledVol; i < scale; i++) {
            sbVolume.append(EMPTY_BLOCK);
        }

        return sbVolume.toString();
    }

    public static String paintMusicPlayerIcons(boolean isPlaying) {
        return new StringBuilder()
                .append('(')
                .append(PREV).append(SPACE)
                .append(isPlaying ? PAUSE : PLAY)
                .append(SPACE).append(NEXT)
                .append(')')
                .toString();
    }

    public static String paintBatteryStatus() {
        String icon = SystemCommandExecutor.isChargerConnected() ? PLUGGED : BATTERY;
        String percentage = SystemCommandExecutor.getBatteryPercentage() + "%";

        return icon + SPACE + percentage;
    }

}
