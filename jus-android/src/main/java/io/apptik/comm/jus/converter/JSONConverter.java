package io.apptik.comm.jus.converter;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public final class JSONConverter {

    public final static class Factory extends Converter.Factory {
        @Override
        public Converter<NetworkResponse, ?> fromResponse(Type type, Annotation[] annotations) {
            if (JSONObject.class.isAssignableFrom((Class<?>) type)) {
                return new JSONObjectResponseConverter();
            }
            if (JSONArray.class.isAssignableFrom((Class<?>) type)) {
                return new JSONArrayResponseConverter();
            }
            return null;
        }

        @Override
        public Converter<?, NetworkRequest> toRequest(Type type, Annotation[] annotations) {
            if (JSONObject.class.isAssignableFrom((Class<?>) type)) {
                return new JSONObjectRequestConverter();
            }
            if (JSONArray.class.isAssignableFrom((Class<?>) type)) {
                return new JSONArrayRequestConverter();
            }
            return null;
        }
    }

    public final static class JSONObjectRequestConverter implements Converter<JSONObject, NetworkRequest> {
        Converters.StringRequestConverter stringRequestConverter =
                new Converters.StringRequestConverter();
        @Override
        public NetworkRequest convert(JSONObject value) throws IOException {
            return stringRequestConverter.convert(value.toString());
        }
    }

    public final static class JSONObjectResponseConverter implements Converter<NetworkResponse, JSONObject> {
        Converters.StringResponseConverter stringResponseConverter =
                new Converters.StringResponseConverter();
        @Override
        public JSONObject convert(NetworkResponse value) throws IOException {
            try {
                return new JSONObject(stringResponseConverter.convert(value));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final static class JSONArrayRequestConverter implements Converter<JSONArray, NetworkRequest> {
        Converters.StringRequestConverter stringRequestConverter =
                new Converters.StringRequestConverter();
        @Override
        public NetworkRequest convert(JSONArray value) throws IOException {
            return stringRequestConverter.convert(value.toString());
        }
    }

    public final static class JSONArrayResponseConverter implements Converter<NetworkResponse, JSONArray> {
        Converters.StringResponseConverter stringResponseConverter =
                new Converters.StringResponseConverter();
        @Override
        public JSONArray convert(NetworkResponse value) throws IOException {
            try {
                return new JSONArray(stringResponseConverter.convert(value));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
