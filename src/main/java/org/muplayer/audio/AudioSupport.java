package org.muplayer.audio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AudioSupport {
    private String extension;
    private Class<? extends Track> audioClass;
}
