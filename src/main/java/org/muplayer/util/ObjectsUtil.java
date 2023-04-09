package org.muplayer.util;

public class ObjectsUtil {
    public static boolean isEqualsStringsAcceptNull(String str1, String str2) {
        if (str1 == null && str2 == null)
            return true;
        if (str1 == null || str2 == null)
            return false;
        return str1.equals(str2);
    }
}
