package com.media.notabadplayer.Utilities;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines commonly used collection methods for copying, wrapping, etc.
 */
public class CollectionUtilities {
    public static <T> List<T> copy(@NonNull List<T> data)
    {
        return new ArrayList<>(data);
    }

    public static <T> Set<T> copy(@NonNull Set<T> data)
    {
        return new HashSet<>(data);
    }

    public static <K, T> Map<K, T> copy(@NonNull Map<K, T> data)
    {
        return new HashMap<K, T>(data);
    }

    public static <T> List<T> copyAsImmutable(@NonNull List<T> data)
    {
        return Collections.unmodifiableList(copy(data));
    }

    public static <T> Set<T> copyAsImmutable(@NonNull Set<T> data)
    {
        return Collections.unmodifiableSet(copy(data));
    }

    public static <K, T> Map<K, T> copyAsImmutable(@NonNull Map<K, T> data)
    {
        return Collections.unmodifiableMap(copy(data));
    }

    public static <T> List<T> copyAsReversed(@NonNull List<T> data)
    {
        List<T> dataCopy = CollectionUtilities.copy(data);
        Collections.reverse(dataCopy);
        return dataCopy;
    }
}
