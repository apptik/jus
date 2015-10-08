package io.apptik.comm.jus.converter;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public class BasicConverterFactory extends Converter.Factory {

    @Override
    public Converter<NetworkResponse, ?> fromResponseBody(Type type, Annotation[] annotations) {
        if (type instanceof Class) {
            if (NetworkResponse.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.NetworkResponseConverter();
            }
            if(String.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.StringResponseConverter();
            }
        }
        return null;
    }

    @Override
    public Converter<?, NetworkRequest> toRequestBody(Type type, Annotation[] annotations) {
        if (type instanceof Class) {
            if (NetworkResponse.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.NetworkRequestConverter();
            }
            if(String.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.StringRequestConverter();
            }
        }
        return null;
    }
}
