package org.muplayer.system;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Global {
    private final Map<GlobalVar, Object> mapGlobals;

    private static final Global global = new Global();

    public static Global getInstance() {
        return global;
    }

    public Global() {
        mapGlobals = new TreeMap<>();
    }

    public synchronized boolean hasVar(GlobalVar globalVar) {
        return mapGlobals.containsKey(globalVar);
    }

    public synchronized <T> T getVar(GlobalVar name) {
        return (T) mapGlobals.get(name);
    }

    public synchronized void setVar(GlobalVar name, Object value) {
        mapGlobals.put(name, value);
    }

}
