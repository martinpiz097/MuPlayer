package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;

import java.io.File;

public interface TrackFactory {
    default Track getTrack(String dataSource, TrackInternalEventNotifier internalEventNotifier) {
        return getTrack(new File(dataSource), internalEventNotifier);
    }
    Track getTrack(File dataSource, TrackInternalEventNotifier internalEventNotifier) throws FormatNotSupportedException;
}
