package io.apptik.comm.jus.converter;


import java.io.IOException;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;

public class Converters {
    static final class NetworkResponseConverter implements Converter<NetworkResponse,NetworkResponse> {

        @Override
        public NetworkResponse convert(NetworkResponse value) throws IOException {
            return value;
        }
    }
    static final class NetworkRequestConverter implements Converter<NetworkRequest,NetworkRequest> {

        @Override
        public NetworkRequest convert(NetworkRequest value) throws IOException {
            return value;
        }
    }
}
