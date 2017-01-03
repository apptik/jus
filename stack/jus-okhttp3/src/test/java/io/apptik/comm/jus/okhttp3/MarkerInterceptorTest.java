package io.apptik.comm.jus.okhttp3;


import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.toolbox.ByteArrayPool;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MarkerInterceptorTest {

    @Rule
    public final MockWebServer server = new MockWebServer();

    @Test
    public void markerInterceptorOK() throws IOException, InterruptedException {
        OkHttpStack okHttpStack = new OkHttpStack(new MarkerInterceptorFactory.DefaultMIF());
        server.enqueue(new MockResponse().setBody("Hi"));
        Request request = mock(Request.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getTag()).thenReturn("mytag");
        when(request.getBody()).thenReturn(null);
        when(request.getUrlString()).thenReturn(server.url("/").toString());


        okhttp3.Request expected = new okhttp3.Request.Builder()
                .url(server.url("/"))
                .method("GET", null)
                .tag("mytag")
                .addHeader("Host", server.getHostName() + ":" + server.getPort())
                .addHeader("Connection", "Keep-Alive")
                .addHeader("Accept-Encoding", "gzip")
                .addHeader("User-Agent", "okhttp/3.5.0")
                .build();

        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<okhttp3.Request> argument1 = ArgumentCaptor.forClass(okhttp3.Request.class);
        ArgumentCaptor<okhttp3.Headers> argument2 = ArgumentCaptor.forClass(okhttp3.Headers.class);

        okHttpStack.performRequest(request, null, new ByteArrayPool(100));
        verify(request).addMarker(name.capture(),
                argument1.capture(), argument2.capture());

        assertThat(name.getValue()).isEqualTo(AbstractMarkerInterceptor.OKHTTP3_INTERCEPT);
        assertThat(argument1.getAllValues().get(0).toString()).isEqualTo(expected.toString());
        assertThat(argument2.getAllValues().get(0)).isEqualTo(expected.headers());
    }

}
