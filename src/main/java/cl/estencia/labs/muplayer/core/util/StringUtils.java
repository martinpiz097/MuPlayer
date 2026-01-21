package cl.estencia.labs.muplayer.core.util;

public class StringUtils {
    public static final String SHORT_VALUE_SUFFIX = "...";

    public static String reduceString(String strValue, int contentSizeLimit) {
        return strValue.length() > (contentSizeLimit + SHORT_VALUE_SUFFIX.length())
                ? strValue.substring(0, contentSizeLimit) + SHORT_VALUE_SUFFIX
                : strValue;
    }
}
