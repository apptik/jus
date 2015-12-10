package io.apptik.comm.jus;


import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.apptik.comm.jus.auth.Authenticator;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.mock.MockTokenAuth;
import io.apptik.comm.jus.request.StringRequest;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthTest {

    @Rule
    public final MockWebServer server = new MockWebServer();

    public RequestQueue queue;

    @Before
    public void setup() {
        queue = Jus.newRequestQueue().addAuthenticatorFactory(new Authenticator.Factory() {
            @Override
            public Authenticator forServer(HttpUrl url, NetworkRequest networkRequest) {
                return new MockTokenAuth();
            }

            @Override
            public Authenticator forProxy(HttpUrl url, NetworkRequest networkRequest) {
                return new MockTokenAuth();
            }
        });
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }

    @Test
    public void authRetryOn401() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(401)

                .setBody("Invalid token"));
        server.enqueue(new MockResponse().setResponseCode(401)

                .setBody("Invalid token"));
        server.enqueue(new MockResponse().setResponseCode(401)

                .setBody("Invalid token"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("nice :)"));
        String result = queue.add(new StringRequest("POST", server.url("/").toString())
                .setObjectRequest("try me!")).getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("nice :)");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");

        request = server.takeRequest();
        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
        request = server.takeRequest();
        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
    }
    @Test
    public void authRetryOn407() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(407)

                .setBody("Invalid token"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("nice :)"));
        String result = queue.add(new StringRequest("POST", server.url("/").toString())
                .setObjectRequest("try me!")).getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("nice :)");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");

        request = server.takeRequest();
        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
    }
}
