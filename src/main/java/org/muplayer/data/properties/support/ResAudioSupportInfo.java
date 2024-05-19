package org.muplayer.data.properties.support;

import org.muplayer.data.properties.ResourceFiles;
import org.muplayer.data.properties.PropertiesInfo;
import org.muplayer.data.properties.StreamPropertiesSource;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ResAudioSupportInfo extends PropertiesInfo<InputStream> {

    public static final String KEY_PREFFIX = "audio.format.class.";
    private static final ResAudioSupportInfo instance = new ResAudioSupportInfo();

    public static ResAudioSupportInfo getInstance() {
        return instance;
    }

    private ResAudioSupportInfo() {
        super(new StreamPropertiesSource(ResourceFiles.AUDIO_SUPPORT_RES_PATH));
    }

    @Override
    public void loadDefaultData() {
        // load model por defecto desde codigo
    }

    public Set<String> getAudioExtensions() {
        return getPropertyNames()
                .parallelStream()
                .map(prop->prop.substring(KEY_PREFFIX.length()))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
