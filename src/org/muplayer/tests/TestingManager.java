package org.muplayer.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import static org.muplayer.tests.TestingKeys.TESTINGPATH;

public class TestingManager {
    private Properties props;
    private File fileTestingInfo;

    public TestingManager() throws IOException {
        props = new Properties();
        fileTestingInfo = new File("testingInfo.properties");
        if (fileTestingInfo.exists()) {
            loadData();
        }
        else {
            fileTestingInfo.createNewFile();
            saveDefaultData();
        }
    }

    private void loadData() {
        try {
            props.load(new FileInputStream(fileTestingInfo));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDefaultData() {
        props.setProperty(TESTINGPATH, "/home/martin/AudioTesting/test/");
        saveData();
    }

    private void saveData() {
        try {
            props.store(new FileOutputStream(fileTestingInfo), "Testing Information");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void setProperty(String key, String value) {
        props.setProperty(key, value);
        saveData();
    }

}
