package io.apptik.comm.jus.converter;


import java.io.IOException;
import java.nio.charset.Charset;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.http.HTTP;
import io.apptik.comm.jus.http.MediaType;

public class Converters {

    public static final class NetworkResponseConverter implements Converter<NetworkResponse, NetworkResponse> {
        @Override
        public NetworkResponse convert(NetworkResponse value) throws IOException {
            return value;
        }
    }

    public static final class NetworkRequestConverter implements Converter<NetworkRequest, NetworkRequest> {
        @Override
        public NetworkRequest convert(NetworkRequest value) throws IOException {
            return value;
        }
    }

    public static final class StringResponseConverter implements Converter<NetworkResponse, String> {
        @Override
        public String convert(NetworkResponse value) throws IOException {
            String parsed;
            if(value.data == null) {
                return null;
            }
            try {
                Charset charset = Charset.forName(HTTP.UTF_8);
                if (value.headers.get(HTTP.CONTENT_TYPE) != null) {
                    charset = MediaType.parse(value.headers.get(HTTP.CONTENT_TYPE)).charset(charset);
                }
                parsed = new String(value.data, charset);
            } catch (Exception e) {
                parsed = new String(value.data);
            }
            return parsed;
        }
    }

    public static final class StringRequestConverter implements Converter<String, NetworkRequest> {

        private static final MediaType MEDIA_TYPE = MediaType.parse("text/plain; charset=UTF-8");
        private final MediaType mediaType;

        public StringRequestConverter() {
            this.mediaType = MEDIA_TYPE;
        }
        public StringRequestConverter(String mediaType) {
            this.mediaType = MediaType.parse(mediaType);
        }
        public StringRequestConverter(MediaType mediaType) {
            this.mediaType = mediaType;
        }
        @Override
        public NetworkRequest convert(String value) throws IOException {
            Charset charset = Charset.forName(HTTP.UTF_8);
            charset = mediaType.charset(charset);
            return new NetworkRequest.Builder()
                    .setContentType(mediaType)
                    .setBody(value.getBytes(charset))
                    .build();
        }
    }
}
