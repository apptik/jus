package io.apptik.comm.jus.converter;


import org.json.JSONObject;

import java.io.IOException;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public final class JSONObjectConverter {

    public final static class Factory extends Converter.Factory {

    }

    public final static class RequestConverter implements Converter<JSONObject, NetworkRequest> {
        @Override
        public NetworkRequest convert(JSONObject value) throws IOException {
            return null;
        }
    }

    public final static class ResponseConverter implements Converter<NetworkResponse, JSONObject> {
        @Override
        public JSONObject convert(NetworkResponse value) throws IOException {
            return null;
        }
    }
}
