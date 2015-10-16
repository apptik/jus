
package io.apptik.comm.jus.converter;

import org.djodjo.json.JsonArray;
import org.djodjo.json.JsonElement;
import org.djodjo.json.JsonObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

/**
 * A {@linkplain Converter.Factory converter} which uses JustJson.
 * <p/>
 */
public final class JJsonConverterFactory extends Converter.Factory {
    /**
     * Create an instance.
     */
    public static JJsonConverterFactory create() {
        return new JJsonConverterFactory();
    }

    private JJsonConverterFactory() {
    }

    @Override
    public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
        if (JsonElement.class.isAssignableFrom((Class<?>) type)) {
            if (type.equals(JsonObject.class)) {
                return new JJsonObjectResponseBodyConverter();
            } else if (type.equals(JsonArray.class)) {
                return new JJsonArrayResponseBodyConverter();
            }

            return new JJsonResponseBodyConverter();
        }

        return null;
    }

    @Override
    public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
        if (JsonElement.class.isAssignableFrom((Class<?>) type)) {
            return new JJsonRequestBodyConverter();
        }

        return null;
    }
}
