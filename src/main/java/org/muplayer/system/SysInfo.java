package org.muplayer.system;

public class SysInfo {
    public static final String OSNAME = System.getProperty("os.name").toLowerCase();
    public static final boolean ISWINDOWS = OSNAME.contains("windows");
    public static final boolean ISLINUX = OSNAME.contains("linux");
    public static final boolean ISMAC = (!ISWINDOWS && !ISLINUX) &&
            (OSNAME.contains("mac") || OSNAME.contains("osx"));

    public static final boolean ISUNIX = ISLINUX || ISMAC;

    public static final String USERNAME = System.getProperty("user.name");

}
