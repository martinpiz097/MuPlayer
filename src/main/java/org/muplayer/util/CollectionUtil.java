package org.muplayer.util;

import java.util.LinkedList;
import java.util.List;

public class CollectionUtil {
    public static <T> List<T> newFastList() {
        return new LinkedList<>();
    }
}
