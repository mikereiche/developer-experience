package com.example.demo.model;

/**
 * A standardized result format for successful responses, that the frontend
 * application can interpret for all endpoints. Allows to contain user-facing
 * data and an array of context strings, eg. N1QL queries, to be displayed in a
 * "learn more" or console kind of UI element on the front end.
 *
 */
public class Result<T> implements IValue {

    private final String prelude;
    private final T data;
    private final String[] context;

    private Result(String prelude, T data, String... contexts) {
        this.prelude = prelude;
        this.data = data;
        this.context = contexts;
    }

    public static <T> Result<T> of(T data, String... contexts) {
        return new Result<T>("", data, contexts);
    }
    public static <T> Result<T> of(String prelude, T data, String... contexts) {
        return new Result<T>(prelude, data, contexts);
    }


    public T getData() {
        return data;
    }

    public String getPrelude() {
        return prelude;
    }
    public String[] getContext() {
        return context;
    }
}
