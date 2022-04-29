package org.muplayer.properties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class AppConfigInfo {
    private final File fileProps;
    private Properties props;

    private static AppConfigInfo singleton;

    public static AppConfigInfo getInstance() {
        if (singleton == null)
            singleton = new AppConfigInfo();
        return singleton;
    }

    private AppConfigInfo() {
        /*try {
            System.out.println(new File(".").getCanonicalPath());
            System.out.println(new File("").getCanonicalPath());
            System.out.println(System.getProperty("user.dir"));
            System.out.println(Paths.get("").toAbsolutePath());
            System.out.println(Paths.get(".").toAbsolutePath());
            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            System.out.println(location.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        fileProps = new File(PropertiesFilesInfo.CONFIG_FILE_PATH);
        props = new Properties();

        if (!fileProps.exists()) {
            try {
                fileProps.createNewFile();
                saveData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadData() {
        try {
            props = new Properties();
            props.load(new InputStreamReader(new FileInputStream(fileProps), StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            props.store(new FileOutputStream(fileProps), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        loadData();
        return props.getProperty(key);
    }

    public void set(String key, String value) {
        props.setProperty(key, value);
        saveData();
    }

}
