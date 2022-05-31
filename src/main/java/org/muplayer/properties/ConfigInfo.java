package org.muplayer.properties;

import org.muplayer.util.IOUtil;

import java.io.*;
import java.util.Properties;

public class ConfigInfo {
    private final File fileProps;
    private Properties props;

    private static ConfigInfo singleton;

    public static ConfigInfo getInstance() {
        if (singleton == null)
            singleton = new ConfigInfo();
        return singleton;
    }

    private ConfigInfo() {
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
        else
            loadData();
    }

    private void loadData() {
        try {
            props = new Properties();
            props.load(IOUtil.getBufferedReader(fileProps));
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
        return props.getProperty(key);
    }

}

