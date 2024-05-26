package org.muplayer.util;

import java.util.*;

public class CollectionUtil {

    private CollectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> List<T> newFastList() {
        return new LinkedList<>();
    }

    public static <K, V> Map<K, V> newFastMap() {
        return new TreeMap<>();
    }
}
