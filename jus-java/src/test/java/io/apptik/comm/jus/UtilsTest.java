package io.apptik.comm.jus;


import org.junit.Test;

import java.lang.reflect.Type;

import io.apptik.comm.jus.toolbox.Utils;

import static org.assertj.core.api.Assertions.assertThat;

public class UtilsTest {

    @Test
    public void getRespTypeClassNotResolvable() {
        Request request = new SRequest("GET", "test");
        Type t = Utils.tryIdentifyResultType(request);
        assertThat(t).isNull();
    }

    @Test
    public void getRespTypeClass() {
        Request request = new StringRequest("GET", "test");
        Type t = Utils.tryIdentifyResultType(request);
        assertThat(t).isEqualTo(String.class);
    }

    @Test
    public void getRespTypeMethod() {
        Request request = new SmRequest("GET", "test");
        Type t = Utils.tryIdentifyResultType(request);
        assertThat(t).isEqualTo(String.class);
    }


    private class SmRequest extends Request {

        public SmRequest(java.lang.String method, java.lang.String url) {
            super(method, url);
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            return super.parseNetworkResponse(response);
        }
    }

    private class SRequest<T extends String> extends Request<T> {

        public SRequest(java.lang.String method, java.lang.String url) {
            super(method, url);
        }

        @Override
        protected Response<T> parseNetworkResponse(NetworkResponse response) {
            return super.parseNetworkResponse(response);
        }
    }

    private class StringRequest extends Request<String> {

        public StringRequest(String method, String url) {
            super(method, url);
        }
    }
}
