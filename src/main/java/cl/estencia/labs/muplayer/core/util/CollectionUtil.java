package cl.estencia.labs.muplayer.core.util;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

public class CollectionUtil {
    private static final short DEFAULT_LIST_INITIAL_CAPACITY = (short) Math.powExact(2, 7);
    private static final short LIST_MINIMAL_INITIAL_CAPACITY = (short) Math.powExact(2, 4);
    private static final short LIST_SHORT_INITIAL_CAPACITY = (short) Math.powExact(2, 9);
    private static final short LIST_BIG_INITIAL_CAPACITY = (short) Math.powExact(2, 11);
    private static final byte MIN_CAPACITY_EXTRA = 10;

    private CollectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> List<T> newList(int size) {
        return new ArrayList<>(size + MIN_CAPACITY_EXTRA);
    }

    public static <T> List<T> newList() {
        return newList(DEFAULT_LIST_INITIAL_CAPACITY);
    }

    public static <T> List<T> newMiniList() {
        return newList(LIST_MINIMAL_INITIAL_CAPACITY);
    }

    public static <T> List<T> newShortList() {
        return newList(LIST_SHORT_INITIAL_CAPACITY);
    }

    public static <T> List<T> newBigList() {
        return newList(LIST_BIG_INITIAL_CAPACITY);
    }

    public static <T> List<T> newList(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return newList(10);
        }

        List<T> newList = newList(collection.size());
        newList.addAll(collection);

        return newList;
    }

    public static <T> List<T> newLinkedList() {
        return new LinkedList<>();
    }

    public static <K extends Comparable<K>, V> Map<K, V> newMap() {
        return new TreeMap<>();
    }

    // no usar hashset porque cambia el orden de los elementos
    // linkedHashSet no hace eso
    public static <T> Set<T> newSet() {
        return new LinkedHashSet<>();
    }

    public static <T> Stream<T> streamOf(T[] array, boolean parallel) {
        return parallel ? Arrays.asList(array).parallelStream() : Arrays.asList(array).stream();
    }

    public static <T> Stream<T> streamOf(T[] array) {
        return streamOf(array, false);
    }

    public static boolean existsFolder(List<File> listFolders, String folderPath) {
        return listFolders.parallelStream()
                .anyMatch(fp -> fp.getPath().equals(folderPath));
    }

}
