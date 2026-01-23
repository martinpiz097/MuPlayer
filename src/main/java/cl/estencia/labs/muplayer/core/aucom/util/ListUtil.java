package cl.estencia.labs.muplayer.core.aucom.util;

import java.util.*;
import java.util.stream.Stream;

public class ListUtil {
    private static final short LIST_INITIAL_CAPACITY = (short) Math.round(
            Math.pow(2, 10) * 1.5);
    private static final short SHORT_LIST_INITIAL_CAPACITY = (short) Math.round(Math.pow(2, 8));
    private static final short MINIMAL_LIST_INITIAL_CAPACITY = (short) Math.round(Math.pow(2, 7));
    private static final byte MIN_CAPACITY_EXTRA = 10;

    private ListUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> List<T> newFastList(int size) {
        return new ArrayList<>(size + MIN_CAPACITY_EXTRA);
    }

    public static <T> List<T> newLinkedList() {
        return new LinkedList<>();
    }

    public static <T> List<T> newFastList(Collection<T> collection) {
        return new LinkedList<>(collection);
    }

    public static <K extends Comparable<K>, V> Map<K, V> newFastMap() {
        return new TreeMap<>();
    }

    public static <T> Stream<T> streamOf(T[] array, boolean parallel) {
        return parallel ? Arrays.asList(array).parallelStream() : Arrays.stream(array);
    }

    public static <T> Stream<T> streamOf(T[] array) {
        return streamOf(array, false);
    }

}
