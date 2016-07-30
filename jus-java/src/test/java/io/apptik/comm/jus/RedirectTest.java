package io.apptik.comm.jus;


import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.apptik.comm.jus.mock.CustomHttpStack;
import io.apptik.comm.jus.request.StringRequest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class RedirectTest {

    @Rule
    public final MockWebServer server = new MockWebServer();

    public RequestQueue queue;

    @Before
    public void setup() {
        HttpURLConnection.setFollowRedirects(false);
        queue = Jus.newRequestQueue(null, new CustomHttpStack());
        JusLog.MarkerLog.on();
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }

    //10.3.1 300 Multiple Choices
    @Test
    public void redirectOn300() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(300)
                .addHeader("Location: /page2")
                .setBody("moved to Page 2"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("Page 2"));
        String result = queue.add(new StringRequest("POST", server.url("/page1").toString())
                .setObjectRequest("try me!")
                .setRedirectPolicy(new RedirectPolicy.DefaultRedirectPolicy()))
                .getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Page 2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page1");

        request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page2");

    }

    //10.3.2 301 Moved Permanently
    @Test
    public void redirectOn301() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(301)
                .addHeader("Location: /page2")
                .setBody("moved to Page 2"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("Page 2"));
        String result = queue.add(new StringRequest("POST", server.url("/page1").toString())
                .setObjectRequest("try me!")
                .setRedirectPolicy(new RedirectPolicy.DefaultRedirectPolicy()))
                .getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Page 2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page1");

        request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page2");

    }
    //10.3.3 302 Found
    @Test
    public void redirectOn302() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302)
                .addHeader("Location: /page2")
                .setBody("moved to Page 2"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("Page 2"));
        String result = queue.add(new StringRequest("POST", server.url("/page1").toString())
                .setObjectRequest("try me!")
                .setRedirectPolicy(new RedirectPolicy.DefaultRedirectPolicy()))
                .getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Page 2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page1");

        request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page2");

    }
    //10.3.4 303 See Other
    @Test
    public void redirectOn303() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(303)
                .addHeader("Location: /page2")
                .setBody("moved to Page 2"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("Page 2"));
        String result = queue.add(new StringRequest("POST", server.url("/page1").toString())
                .setObjectRequest("try me!")
                .setRedirectPolicy(new RedirectPolicy.DefaultRedirectPolicy()))
                .getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Page 2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page1");

        request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getPath()).endsWith("/page2");

    }
    //10.3.8 307 Temporary Redirect
    @Test
    public void redirectOn307() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(307)
                .addHeader("Location: /page2")
                .setBody("moved to Page 2"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("Page 2"));
        String result = queue.add(new StringRequest("POST", server.url("/page1").toString())
                .setObjectRequest("try me!")
                .setRedirectPolicy(new RedirectPolicy.DefaultRedirectPolicy()))
                .getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Page 2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page1");

        request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page2");

    }

    //https://tools.ietf.org/html/rfc7538 - Status Code 308 (Permanent Redirect)
    @Test
    public void redirectOn308() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(308)
                .addHeader("Location: /page2")
                .setBody("moved to Page 2"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("Page 2"));
        String result = queue.add(new StringRequest("POST", server.url("/page1").toString())
                .setObjectRequest("try me!")
                .setRedirectPolicy(new RedirectPolicy.DefaultRedirectPolicy()))
                .getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Page 2");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page1");

        request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).endsWith("/page2");

    }

    @Test
    public void allMarkersAreSentToOrigRequest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(308)
                .addHeader("Location: /page2")
                .setBody("moved to Page 2"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("Page 2"));

        final AtomicReference<Marker> markerRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(2);
        String result = queue.add(new StringRequest("POST", server.url("/page1").toString())
                .setObjectRequest("try me!")
                .setRedirectPolicy(new RedirectPolicy.DefaultRedirectPolicy()))
                .addMarkerListener(new RequestListener.MarkerListener() {
                    @Override
                    public void onMarker(Marker marker, Object... args) {

                        if (CustomHttpStack.MY_CUSTOM_MARKER.equals(marker.name)) {
                            markerRef.set(marker);
                            latch.countDown();
                        }
                    }
                })
                .getFuture().get();


        assertTrue(latch.await(2, SECONDS));
        assertThat(markerRef.get().name).isEqualTo(CustomHttpStack.MY_CUSTOM_MARKER);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("Page 2");



    }


}
