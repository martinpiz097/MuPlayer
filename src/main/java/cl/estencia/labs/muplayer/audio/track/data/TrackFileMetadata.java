package cl.estencia.labs.muplayer.audio.track.data;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.tag.FieldKey;

import java.util.Map;

@Slf4j
public record TrackFileMetadata(Map<FieldKey, String> tags, Cover cover,
                                double duration, long bitRate) {
    public TrackFileMetadata() {
        this(Map.of(), new Cover(null, null),
                0, -1);
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
