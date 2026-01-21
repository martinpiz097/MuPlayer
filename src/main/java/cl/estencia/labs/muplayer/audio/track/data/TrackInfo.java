package cl.estencia.labs.muplayer.audio.track.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@Slf4j
public class TrackInfo {
    private final Map<FieldKey, String> tags;
    private final byte[] coverData;
    private final double duration;
    private final long bitRate;

    public TrackInfo() {
        this.tags = Map.of();
        this.coverData = null;
        this.duration = 0;
        this.bitRate = -1;
    }

    public boolean hasCover() {
        return coverData != null;
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
