package cl.estencia.labs.muplayer.utils;

import cl.estencia.labs.muplayer.audio.track.Track;
import cl.estencia.labs.muplayer.model.TrackIndexed;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CollectionUtil {
    private static final short LIST_INITIAL_CAPACITY = (short) Math.round(
            Math.pow(2, 10) * 1.5);
    private static final short SHORT_LIST_INITIAL_CAPACITY = (short) Math.round(Math.pow(2, 8));
    private static final short MINIMAL_LIST_INITIAL_CAPACITY = (short) Math.round(Math.pow(2, 7));
    private static final byte MIN_CAPACITY_EXTRA = 10;

    private CollectionUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> List<T> newFastList(int size) {
        return new ArrayList<>(size + MIN_CAPACITY_EXTRA);
    }

    public static <T> List<T> newFastArrayList() {
        return newFastList(LIST_INITIAL_CAPACITY);
    }

    public static <T> List<T> newShortFastArrayList() {
        return newFastList(SHORT_LIST_INITIAL_CAPACITY);
    }

    public static <T> List<T> newMinimalFastArrayList() {
        return newFastList(MINIMAL_LIST_INITIAL_CAPACITY);
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
        return parallel ? Arrays.asList(array).parallelStream() : Arrays.asList(array).stream();
    }

    public static <T> Stream<T> streamOf(T[] array) {
        return streamOf(array, false);
    }

    public static boolean existsFolder(List<File> listFolders, String folderPath) {
        return listFolders.parallelStream()
                .anyMatch(fp -> fp.getPath().equals(folderPath));
    }

    // si incluyo paralelismo en este metodo, debo crear otro o gestionar con parametro boolean,
    // ya que hay algunos casos en los que si necesito secuencialidad
    public TrackIndexed getTrackIndexedFromCondition(List<Track> listTracks, Predicate<Track> filter) {
        int index = 0;

        for (Track track : listTracks) {
            if (filter.test(track)) {
                return new TrackIndexed(track, index);
            }
            index++;
        }
        return null;
    }

}
