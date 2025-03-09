package org.muplayer.util;

import java.util.*;

public class CollectionUtil {
    private static final short LIST_MIN_CAPACITY = 1536;
    private static final short SHORT_LIST_MIN_CAPACITY = 256;
    private static final byte MIN_CAPACITY_EXTRA = 10;

    private CollectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> List<T> newFastArrayList() {
        return new ArrayList<>(LIST_MIN_CAPACITY + MIN_CAPACITY_EXTRA);
    }

    public static <T> List<T> newShortFastArrayList() {
        return new ArrayList<>(SHORT_LIST_MIN_CAPACITY + MIN_CAPACITY_EXTRA);
    }

    public static <T> List<T> newLinkedList() {
        return new LinkedList<>();
    }

    public static <T> List<T> newFastList(int size) {
        return new ArrayList<>(size + 10);
    }

    public static <T> List<T> newFastList(Collection<T> collection) {
        return new LinkedList<>(collection);
    }

    public static <K, V> Map<K, V> newFastMap() {
        return new TreeMap<>();
    }
}
