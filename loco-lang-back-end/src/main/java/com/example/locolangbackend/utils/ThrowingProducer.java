package com.example.locolangbackend.utils;

@FunctionalInterface
public interface ThrowingProducer<T> {
    T get() throws Exception;
}