package cl.estencia.labs.muplayer.audio.track.data;

import cl.estencia.labs.muplayer.audio.common.enums.ImageFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;

import java.util.Map;

@Getter
@Setter
@Slf4j
public class TrackFileMetadata {
    private final Map<FieldKey, String> tags;
    private final Cover cover;
    private final double duration;
    private final long bitRate;

    public TrackFileMetadata() {
        this(Map.of(), null, 0, -1);
    }

    public TrackFileMetadata(Map<FieldKey, String> tags, byte[] coverData, double duration, long bitRate) {
        this.tags = tags;
        this.cover = new Cover(coverData, readCoverArtFormat());
        this.duration = duration;
        this.bitRate = bitRate;
    }

    private String readCoverArtTag() {
        String coverArt = tags.get(FieldKey.COVER_ART);
        if (coverArt == null || coverArt.isBlank()) {
            return null;
        }
        if (coverArt.contains("::")) {
            coverArt = coverArt.split("::")[0];
        }

        return coverArt.trim();
    }

    private ImageFormat readCoverArtFormat() {
        return ImageFormat.fromMimeType(readCoverArtTag());
    }

    public boolean hasCover() {
        return cover.hasData();
    }

    public String getTag(FieldKey fieldKey) {
        if (fieldKey == null || tags.isEmpty()) {
            return null;
        }

        final String tagValue = tags.get(fieldKey);
        return tagValue != null && !tagValue.isBlank() ? tagValue.trim() : null;
    }

    public String getTag(String tagName) {
        return getTag(FieldKey.valueOf(tagName.toUpperCase()));
    }

}
