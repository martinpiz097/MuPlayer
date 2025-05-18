package cl.estencia.labs.muplayer.audio.track.factory;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.audio.track.format.FlacTrack;
import cl.estencia.labs.muplayer.audio.track.format.MP3Track;
import cl.estencia.labs.muplayer.audio.track.format.OGGTrack;
import cl.estencia.labs.muplayer.audio.track.format.PCMTrack;
import cl.estencia.labs.muplayer.core.exception.FormatNotSupportedException;
import cl.estencia.labs.muplayer.event.notifier.internal.TrackInternalEventNotifier;
import cl.estencia.labs.muplayer.core.common.enums.AudioFileExtension;
import cl.estencia.labs.muplayer.core.util.FileUtil;
import cl.estencia.labs.muplayer.core.util.LogUtil;
import lombok.extern.java.Log;

import java.io.File;

@Log
public class StandardTrackFactory implements TrackFactory {

    @Override
    public Track getTrack(File dataSource, TrackInternalEventNotifier internalEventNotifier) throws FormatNotSupportedException {
        try {
            String fileFormatName = FileUtil.getFileFormatName(dataSource);
            AudioFileExtension audioFileExtension = AudioFileExtension.valueOf(fileFormatName);

            return switch (audioFileExtension) {
                case au, wav, aiff, aifc, snd -> new PCMTrack(dataSource, internalEventNotifier);
                case mp3 -> new MP3Track(dataSource, internalEventNotifier);
                case flac -> new FlacTrack(dataSource, internalEventNotifier);
                case ogg -> new OGGTrack(dataSource, internalEventNotifier);
                default -> throw new FormatNotSupportedException(fileFormatName);
            };

        } catch (Exception e) {
            log.severe(LogUtil.getExceptionMsg(e, "getTrack"));
            return null;
        }
    }
}
