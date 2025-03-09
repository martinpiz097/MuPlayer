package org.muplayer.service;

public interface LogService {
    void errorLog(String message);
    void warningLog(String message);
    void log(String message, String color);
}
