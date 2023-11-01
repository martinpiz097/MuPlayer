package org.muplayer.properties;

import lombok.Getter;
import org.muplayer.model.AudioSupport;
import org.muplayer.util.DataUtil;
import org.orangelogger.sys.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class AudioSupportInfo extends PropertiesInfo<File>{
    @Getter
    private final File supportFile;
    private final Properties properties;

    public static final String KEY_PREFFIX = "audio.format.class.";
    private static final AudioSupportInfo singleton = new AudioSupportInfo();

    public static AudioSupportInfo getInstance() {
        return singleton;
    }

    private AudioSupportInfo() {
        supportFile = new File(PropertiesFiles.AUDIO_SUPPORT_FILE_PATH);
        try {
            System.out.println("Support File Path: " +supportFile.getCanonicalPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        properties = new Properties();
        loadDefaultData();
    }

    private AudioSupportInfo(File supportFile) {
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
        final String dataFromStream = DataUtil.getDataFromResource(PropertiesFiles.AUDIO_SUPPORT_RES_PATH);
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
                final String supportData = DataUtil.getDataFromResource(PropertiesFiles.AUDIO_SUPPORT_RES_PATH);
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
            if (properties.isEmpty()) {
                Logger.getLogger(this, MessagesInfo.getInstance()
                        .getProperty(MessagesInfoKeys.AUDIO_SUPPORT_FILE_ERROR_MSG)).rawError();
                System.exit(0);
            }
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
