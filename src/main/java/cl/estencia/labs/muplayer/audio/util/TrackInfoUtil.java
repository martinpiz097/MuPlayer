package cl.estencia.labs.muplayer.audio.util;

import cl.estencia.labs.muplayer.audio.track.data.TrackFileMetadata;
import cl.estencia.labs.muplayer.core.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import java.io.File;
import java.util.Map;

@Slf4j
public class TrackInfoUtil {
    private static Map<FieldKey, String> readTags(AudioFile audioFile) {
        Tag tag = audioFile != null ? audioFile.getTag() : null;
        if (tag == null) {
            return Map.of();
        }

        Map<FieldKey, String> mapTags = CollectionUtil.newFastMap();
        FieldKey[] fieldKeys = FieldKey.values();
        int fieldKeysCount = fieldKeys.length;
        FieldKey fieldKey;
        String tagValue;
        for (int i = 0; i < fieldKeysCount; i++) {
            try {
                fieldKey = fieldKeys[i];
                tagValue = tag.getFirst(fieldKey);
                if (tagValue != null && !tagValue.isBlank()) {
                    mapTags.put(fieldKey, tagValue);
                }
            } catch (Exception ignored) {
                // saltamos al siguiente tag que se puede obtener
            }
        }

        return mapTags;
    }

    private static byte[] readCoverData(AudioFile audioFile) {
        Tag tag = audioFile != null ? audioFile.getTag() : null;
        if (tag == null) {
            return null;
        }

        Artwork firstArtwork = tag.getFirstArtwork();
        return firstArtwork != null ? firstArtwork.getBinaryData() : null;
    }

    private static double readDuration(AudioFile audioFile) {
        if (audioFile == null) {
            return 0;
        }

        AudioHeader header = audioFile.getAudioHeader();
        return header != null ? header.getPreciseTrackLength() : 0d;
    }

    private static long readBitRate(AudioFile audioFile) {
        if (audioFile == null) {
            return 0;
        }

        AudioHeader header = audioFile.getAudioHeader();
        return header != null ? header.getBitRateAsNumber() : 0;
    }

    public static TrackFileMetadata loadTrackInfo(File trackFile) {
        if (trackFile == null) {
            return new TrackFileMetadata();
        }

        try {
            AudioFile audioFile = AudioFileIO.read(trackFile);
            Map<FieldKey, String> tags = readTags(audioFile);
            byte[] coverData = readCoverData(audioFile);
            double duration = readDuration(audioFile);
            long bitRate = readBitRate(audioFile);

            return new TrackFileMetadata(tags, coverData, duration, bitRate);
        } catch (Exception e) {
//            log.error(e.getMessage(), e);
            return new TrackFileMetadata();
        }
    }

}
