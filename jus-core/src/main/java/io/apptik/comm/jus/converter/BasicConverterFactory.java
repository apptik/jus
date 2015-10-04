package io.apptik.comm.jus.converter;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public class BasicConverterFactory extends Converter.Factory {

    @Override
    public Converter<NetworkResponse, ?> fromResponseBody(Type type, Annotation[] annotations) {
        if (type instanceof Class && NetworkResponse.class.isAssignableFrom((Class<?>) type)) {
            return new Converters.NetworkResponseConverter();
        }
        return null;
    }

    @Override
    public Converter<?, NetworkRequest> toRequestBody(Type type, Annotation[] annotations) {
        if (type instanceof Class && NetworkResponse.class.isAssignableFrom((Class<?>) type)) {
            return new Converters.NetworkRequestConverter();
        }
        return null;
    }
}
