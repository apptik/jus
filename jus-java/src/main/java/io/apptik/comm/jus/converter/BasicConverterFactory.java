package io.apptik.comm.jus.converter;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public class BasicConverterFactory extends Converter.Factory {

    @Override
    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
        if (type instanceof Class) {
            if (NetworkResponse.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.NetworkResponseConverter();
            }
            if (String.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.StringResponseConverter();
            }
            if (Void.class.equals(type)) {
                return new Converter<NetworkResponse, Void>() {
                    @Override
                    public Void convert(NetworkResponse value) throws IOException {
                        return null;
                    }
                };
            }
        }

        return null;
    }

    @Override
    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
        if (type instanceof Class) {
            if (NetworkResponse.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.NetworkRequestConverter();
            }
            if (String.class.isAssignableFrom((Class<?>) type)) {
                return new Converters.StringRequestConverter();
            }
        }
        return null;
    }
}
