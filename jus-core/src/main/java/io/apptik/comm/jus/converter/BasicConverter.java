package io.apptik.comm.jus.converter;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public class BasicConverter extends Converter.Factory<NetworkRequest, NetworkResponse> {

    @Override
    public Converter<NetworkResponse, NetworkResponse> fromResponseBody(Type type, Annotation[] annotations) {
        return new Converter<NetworkResponse, NetworkResponse>() {
            @Override
            public NetworkResponse convert(NetworkResponse value) throws IOException {
                return value;
            }
        };
    }

    @Override
    public Converter<NetworkRequest, NetworkRequest> toRequestBody(Type type, Annotation[] annotations) {
        return new Converter<NetworkRequest, NetworkRequest>() {
            @Override
            public NetworkRequest convert(NetworkRequest value) throws IOException {
                return value;
            }
        };
    }
}
