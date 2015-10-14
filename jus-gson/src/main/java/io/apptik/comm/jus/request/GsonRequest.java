package io.apptik.comm.jus.request;


import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import io.apptik.comm.jus.converter.GsonRequestBodyConverter;
import io.apptik.comm.jus.converter.GsonResponseBodyConverter;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.HttpUrl;

public class GsonRequest<T> extends Request<T> {

    public GsonRequest(String method, HttpUrl url, TypeAdapter<T> typeAdapter) {
        super(method, url, new GsonResponseBodyConverter<>(typeAdapter));
    }

    public GsonRequest(String method, HttpUrl url, Class<T> tClass, Gson gson) {
        this(method, url, gson.getAdapter(tClass));
    }

    public GsonRequest(String method, HttpUrl url, Class<T> tClass) {
        this(method, url, tClass, new Gson());
    }

    public GsonRequest(String method, String url, TypeAdapter<T> typeAdapter) {
        super(method, url, new GsonResponseBodyConverter<>(typeAdapter));
    }

    public GsonRequest(String method, String url, Class<T> tClass, Gson gson) {
        this(method, url, gson.getAdapter(tClass));
    }

    public GsonRequest(String method, String url, Class<T> tClass) {
        this(method, url, tClass, new Gson());
    }

    public <R> Request<T> setObjectRequest(R objectRequest, Gson gson, TypeAdapter<R> adapter) {
        return super.setObjectRequest(objectRequest, new GsonRequestBodyConverter<>(gson, adapter));
    }

    public <R> Request<T> setObjectRequest(R objectRequest, Gson gson) {
        return setObjectRequest(objectRequest, gson, gson.getAdapter((Class<R>) objectRequest.getClass()));
    }

    public <R> Request<T> setObjectRequest(R objectRequest) {
        return setObjectRequest(objectRequest, new Gson());
    }

}
