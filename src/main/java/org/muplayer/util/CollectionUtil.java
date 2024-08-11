package org.muplayer.util;

import org.mpizutil.electrolist.structure.ElectroList;

import java.util.*;

public class CollectionUtil {

    private CollectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> List<T> newFastList() {
        return new ElectroList<>();
    }

    public static <T> List<T> newFastList(Collection<T> collection) {
        return new ElectroList<>(collection);
    }

    public static <K, V> Map<K, V> newFastMap() {
        return new TreeMap<>();
    }
}
