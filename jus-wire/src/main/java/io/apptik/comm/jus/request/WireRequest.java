package io.apptik.comm.jus.request;


import com.squareup.wire.Message;
import com.squareup.wire.Wire;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.WireRequestBodyConverter;
import io.apptik.comm.jus.converter.WireResponseBodyConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class WireRequest<T extends Message> extends Request<T> {

    public WireRequest(String method, HttpUrl url, Wire wire, Class<T> cls) {
        super(method, url, new WireResponseBodyConverter<T>(wire, cls));
    }

    public WireRequest(String method, HttpUrl url, Class<T> cls) {
        this(method, url, new Wire(), cls);
    }

    public WireRequest<T> setObjectRequest(T objectRequest) {
        super.setObjectRequest(objectRequest, new WireRequestBodyConverter<T>());
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/x-protobuf")
                .build());
        return this;
    }

}
