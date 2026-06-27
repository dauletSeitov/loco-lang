package com.example.locolangbackend.utils;

public class ExceptionUtils {

    public static <R> R unchecked(ThrowingProducer<R> produces) {

        try {
            return produces.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}


