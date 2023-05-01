package org.muplayer.system;

import java.util.HashMap;
import java.util.Map;

public class Global {
    private final Map<GlobalVar, Object> mapGlobals;

    private static final Global global = new Global();

    public static Global getInstance() {
        return global;
    }

    public Global() {
        mapGlobals = new HashMap<>();
    }

    public boolean hasVar(GlobalVar globalVar) {
        return mapGlobals.containsKey(globalVar);
    }

    public <T> T getVar(GlobalVar name) {
        return (T) mapGlobals.get(name);
    }

    public void setVar(GlobalVar name, Object value) {
        mapGlobals.put(name, value);
    }

}
