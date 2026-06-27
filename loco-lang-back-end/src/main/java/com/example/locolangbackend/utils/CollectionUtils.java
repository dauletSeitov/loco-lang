package com.example.locolangbackend.utils;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {

    public static <T> List<T> concat(List<T> list1, List<T> list2) {
        List<T> result = new ArrayList<>(list1);
        result.addAll(list2);
        return result;
    }
    public static <T> List<T> concat(T itm, List<T> list2) {
        List<T> result = new ArrayList<>();
        result.add(itm);
        result.addAll(list2);
        return result;
    }
}
