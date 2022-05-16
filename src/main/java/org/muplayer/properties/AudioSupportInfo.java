package org.muplayer.properties;

import lombok.Getter;
import org.muplayer.model.AudioSupport;
import org.muplayer.util.DataUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class AudioSupportInfo {
    @Getter
    private final File supportFile;
    private final Properties properties;

    public static final String KEY_PREFFIX = "audio.format.class.";
    private static final AudioSupportInfo singleton = new AudioSupportInfo();

    public static AudioSupportInfo getInstance() {
        return singleton;
    }

    protected AudioSupportInfo() {
        supportFile = new File("./", PropertiesFilesInfo.AUDIO_SUPPORT_FILE_NAME);
        properties = new Properties();
        loadDefaultData();
    }

    protected AudioSupportInfo(File supportFile) {
        this.supportFile = supportFile;
        properties = new Properties();
        loadData();
    }

    private void validateFile() throws IOException {
        if (!supportFile.exists())
            supportFile.createNewFile();
    }

    private AudioSupportInfo createTempManager() throws IOException {
        final File tempFile = new File(System.currentTimeMillis()
                + ".properties");

        tempFile.createNewFile();
        final String dataFromStream = DataUtil.getDataFromStream(
                AudioSupportInfo.class.getResourceAsStream("/"+ PropertiesFilesInfo.AUDIO_SUPPORT_FILE_NAME));
        Files.write(tempFile.toPath(), dataFromStream.getBytes(StandardCharsets.UTF_8),
                TRUNCATE_EXISTING);
        return new AudioSupportInfo(tempFile);
    }

    private void loadDefaultData() {
        try {
            if (supportFile.exists()) {
                loadData();
                final AudioSupportInfo tempManager = createTempManager();
                final Set<String> propertyKeys = tempManager.getPropertyNames();

                propertyKeys.forEach(key->{
                    final String property = getProperty(key);
                    if (property == null || property.trim().isEmpty())
                        setProperty(key, tempManager.getProperty(key));
                });
                Files.deleteIfExists(tempManager.supportFile.toPath());
            }
            else {
                final String supportData = DataUtil.getDataFromResource("/audio-support.properties");
                supportFile.createNewFile();
                Files.write(supportFile.toPath(), supportData.getBytes(StandardCharsets.UTF_8), TRUNCATE_EXISTING);
                loadData();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadData() {
        try {
            validateFile();
            properties.load(new FileInputStream(supportFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveData() {
        try {
            properties.store(new FileOutputStream(supportFile), "Audio formats support");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key.startsWith(KEY_PREFFIX) ? key : KEY_PREFFIX.concat(key));
    }

    public Set<String> getPropertyNames() {
        return properties.stringPropertyNames();
    }

    public Set<String> getAudioExtensions() {
        return getPropertyNames()
                .parallelStream()
                .map(prop->prop.substring(KEY_PREFFIX.length()))
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public void setProperty(AudioSupport audioSupport) {
        setProperty(KEY_PREFFIX.concat(audioSupport.getExtension()),
                audioSupport.getAudioClass().getName());
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key.startsWith(KEY_PREFFIX) ? key : KEY_PREFFIX.concat(key), value);
        saveData();
    }
}
