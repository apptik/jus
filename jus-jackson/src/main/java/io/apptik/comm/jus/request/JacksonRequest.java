package io.apptik.comm.jus.request;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JacksonRequestBodyConverter;
import io.apptik.comm.jus.converter.JacksonResponseBodyConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class JacksonRequest<T> extends Request<T> {

    public JacksonRequest(String method, HttpUrl url, ObjectReader adapter) {
        super(method, url, new JacksonResponseBodyConverter<T>(adapter));
    }

    public JacksonRequest(String method, HttpUrl url, ObjectMapper objectMapper) {
        this(method, url, objectMapper.reader());
    }

    public JacksonRequest(String method, HttpUrl url, ObjectMapper objectMapper, Class<T> tClass) {
        this(method, url, objectMapper.readerFor(tClass));
    }

    public JacksonRequest(String method, HttpUrl url) {
        this(method, url, new ObjectMapper());
    }

    public JacksonRequest(String method, HttpUrl url, Class<T> tClass) {
        this(method, url, new ObjectMapper(), tClass);
    }

    public JacksonRequest(String method, String url, ObjectReader adapter) {
        super(method, url, new JacksonResponseBodyConverter<T>(adapter));
    }

    public JacksonRequest(String method, String url, ObjectMapper objectMapper) {
        this(method, url, objectMapper.reader());
    }

    public JacksonRequest(String method, String url, ObjectMapper objectMapper, Class<T> tClass) {
        this(method, url, objectMapper.readerFor(tClass));
    }

    public JacksonRequest(String method, String url) {
        this(method, url, new ObjectMapper());
    }

    public JacksonRequest(String method, String url, Class<T> tClass) {
        this(method, url, new ObjectMapper(), tClass);
    }


    public <R> Request<T> setObjectRequest(R objectRequest, ObjectWriter adapter) {
        return super.setObjectRequest(objectRequest, new JacksonRequestBodyConverter<R>(adapter));
    }

    public <R> Request<T> setObjectRequest(R objectRequest, ObjectMapper objectMapper) {
        return setObjectRequest(objectRequest, objectMapper.writerFor(objectRequest.getClass()));
    }

    public <R> Request<T> setObjectRequest(R objectRequest) {
        return setObjectRequest(objectRequest, new ObjectMapper());
    }

}
