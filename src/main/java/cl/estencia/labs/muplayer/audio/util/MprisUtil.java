package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.unix.dbus.mpris.Mpris;
import cl.estencia.labs.muplayer.unix.dbus.mpris.MprisPublisher;
import cl.estencia.labs.muplayer.unix.dbus.mpris.common.PlaybackStatus;

import static cl.estencia.labs.muplayer.core.util.NumberUtil.secondsToMicroSecs;

public class MprisUtil {
    private final Mpris mpris;
    private final MprisPublisher mprisPublisher;

    public MprisUtil(Mpris mpris) {
        this.mpris = mpris;
        this.mprisPublisher = mpris != null
                ? mpris.getPublisher()
                : null;
    }

    public boolean isMprisConnected() {
        return mpris == null || !mpris.getConnection().isConnected();
    }

    public boolean isMprisActive() {
        return isMprisConnected() && mpris.getPublisher().isAlive();
    }

    public void setPlaybackStatus(PlaybackStatus playbackStatus) {
        if (!isMprisConnected()) {
            return;
        }

        mpris.setPlaybackStatus(playbackStatus);
    }

    public void sendSeekedSignal(double seconds) {
        if (!isMprisConnected()) {
            return;
        }

        mprisPublisher.sendSeekedSignal(secondsToMicroSecs(seconds));
    }

    public void sendPropertiesChangedEvent(String name, Object value) {
        if (!isMprisConnected()) {
            return;
        }

        mprisPublisher.sendPropertiesChangedEvent(name, value);
    }

}
