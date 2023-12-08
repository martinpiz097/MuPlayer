package org.muplayer.properties.support;

import lombok.Getter;
import org.muplayer.model.AudioSupport;
import org.muplayer.properties.FilePropertiesSource;
import org.muplayer.properties.PropertiesFiles;
import org.muplayer.properties.PropertiesInfo;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class AudioSupportInfo extends PropertiesInfo<File> {
    @Getter
    private static final AudioSupportInfo instance = new AudioSupportInfo();

    public static final String KEY_PREFFIX = "audio.format.class.";

    private AudioSupportInfo() {
        super(new FilePropertiesSource(PropertiesFiles.AUDIO_SUPPORT_FILE_PATH));
    }

    @Override
    public void loadDefaultData() {
        if (props.isEmpty()) {
            final ResAudioSupportInfo resAudioSupportInfo = ResAudioSupportInfo.getInstance();
            resAudioSupportInfo.getPropertyNames().forEach(
                    name -> {
                        try {
                            setProperty(name, resAudioSupportInfo.getProperty(name));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            try {
                propsSource.saveData("Audio formats support");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getProperty(String key) {
        return super.getProperty(key.startsWith(KEY_PREFFIX) ? key : KEY_PREFFIX.concat(key));
    }

    public Set<String> getAudioExtensions() {
        return getPropertyNames()
                .parallelStream()
                .map(prop->prop.substring(KEY_PREFFIX.length()))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setProperty(AudioSupport audioSupport) throws Exception {
        setProperty(KEY_PREFFIX.concat(audioSupport.getExtension()),
                audioSupport.getAudioClass().getName());
    }

    @Override
    public void setProperty(String key, String value) throws Exception {
        super.setProperty(key.startsWith(KEY_PREFFIX) ? key : KEY_PREFFIX.concat(key), value);
    }
}
