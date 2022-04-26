package org.muplayer.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.muplayer.audio.Track;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioSupport {
    private String extension;
    private Class<? extends Track> audioClass;
}
