package io.apptik.comm.jus.converter;


import java.io.IOException;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public final class StringConverter {

    public final static class Factory extends Converter.Factory {

    }

    public final static class RequestConverter implements Converter<String, NetworkRequest> {
        @Override
        public NetworkRequest convert(String value) throws IOException {
            return null;
        }
    }

    public final static class ResponseConverter implements Converter<NetworkResponse, String> {
        @Override
        public String convert(NetworkResponse value) throws IOException {
            return null;
        }
    }
}
