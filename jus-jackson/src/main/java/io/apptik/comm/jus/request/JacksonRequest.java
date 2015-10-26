/*
 * Copyright (C) 2015 AppTik Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apptik.comm.jus.request;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JacksonRequestConverter;
import io.apptik.comm.jus.converter.JacksonResponseConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class JacksonRequest<T> extends Request<T> {

    public JacksonRequest(String method, HttpUrl url, ObjectReader adapter) {
        super(method, url, new JacksonResponseConverter<T>(adapter));
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
        super(method, url, new JacksonResponseConverter<T>(adapter));
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


    public <R> Request<T> setRequestData(R requestData, ObjectWriter adapter) {
        super.setRequestData(requestData, new JacksonRequestConverter<R>(adapter));
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json; charset=UTF-8")
                .build());
        return this;
    }

    public <R> Request<T> setRequestData(R requestData, ObjectMapper objectMapper) {
        return setRequestData(requestData, objectMapper.writerFor(requestData.getClass()));
    }

    public <R> Request<T> setRequestData(R requestData) {
        return setRequestData(requestData, new ObjectMapper());
    }

}
