package io.apptik.comm.jus.okhttp3;


import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.toolbox.ByteArrayPool;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OkHttpStackTest {

    @Rule
    public final MockWebServer server = new MockWebServer();

    @Test
    public void MarkerInterceptor() throws IOException {
        OkHttpStack okHttpStack = new OkHttpStack(new MarkerInterceptorFactory() {
            @Override
            public AbstractMarkerInterceptor create(Request request) {
                return new AbstractMarkerInterceptor(request) {
                    @Override
                    Object[] getMarkerArgs(Request<?> request, okhttp3.Request okhttpRequest) {
                        return new Object[]{"test marker arg"};
                    }
                };
            }
        });
        server.enqueue(new MockResponse().setBody("Hi"));
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getTag()).thenReturn("mytag");
        when(request.getBody()).thenReturn(null);
        when(request.getUrlString()).thenReturn(server.url("/").toString());

        okHttpStack.performRequest(request, null, new ByteArrayPool(100));

        verify(request).addMarker(AbstractMarkerInterceptor.OKHTTP3_INTERCEPT,"test marker arg");
    }


}
