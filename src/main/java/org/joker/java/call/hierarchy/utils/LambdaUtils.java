package org.joker.java.call.hierarchy.utils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LambdaUtils {

    public interface KeyExtract<K, T> {
        K getkey(T t);
    }

    /**
     * 从集合中的元素 转成 map，key需保持唯一
     * @param collection
     * @param extract
     * @param <K>
     * @param <T>
     * @return
     */
    public static <K, T> Map<K, T> toMap(Collection<T> collection, KeyExtract<K, T> extract) {
//        Map<K, T> map = new HashMap<>();
//        for (T c : collection) {
//            K key = extract.getkey(c);
//            if (map.containsKey(key)) {
//                System.out.println(key.toString() + " ----> " +c.toString());
//                System.out.println(key.toString() + " ----> " +map.get(key).toString());
//            }
//            map.put(key, c);
//        }
//        return map;
        return collection.stream().collect(Collectors.toMap(extract::getkey, i -> i));
    }

    /**
     * 从集合中的元素 转成 map，key可以重复
     * @param collection
     * @param extract
     * @param <K>
     * @param <T>
     * @return
     */
    public static <K, T> Map<K, Set<T>> toMultiMap(Collection<T> collection, KeyExtract<K, T> extract) {
        Map<K, Set<T>> map = Maps.newHashMap();
        collection.forEach(i -> {
            K key = extract.getkey(i);
            if (map.containsKey(key)) {
                map.get(key).add(i);
            } else {
                map.put(key, Sets.newHashSet(i));
            }
        });
        return map;
    }

    /**
     * 转list
     * @param collection
     * @param mapper
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> List<R> toList(Collection<T> collection, Function<? super T, ? extends R> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T> List<T> filter(Collection<T> collection, Predicate<? super T> predicate) {
        return collection.stream().filter(predicate).collect(Collectors.toList());
    }
}
