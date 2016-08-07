package io.apptik.comm.jus;


import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.apptik.comm.jus.auth.Authenticator;
import io.apptik.comm.jus.error.AuthError;
import io.apptik.comm.jus.http.HttpUrl;
import io.apptik.comm.jus.mock.MockTokenAuth;
import io.apptik.comm.jus.request.StringRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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
        }).setRetryPolicyFactory(new RetryPolicy.Factory() {
            @Override
            public RetryPolicy get(Request request) {
                return new DefaultRetryPolicy(1000, 0, 1);
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
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("nice :)"));
        String result = queue.add(new StringRequest("POST", server.url("/").toString())
                .setRequestBody("try me!")).getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("nice :)");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");

    }

    @Test
    public void authRetryOn401Fail() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(401)
                .setBody("Invalid token"));
        server.enqueue(new MockResponse().setResponseCode(401)
                .setBody("Invalid token"));
        try {
            queue.add(new StringRequest("POST", server.url("/").toString())
                    .setRequestBody("try me!")).getFuture().get();
            fail("Must not retry 2 times");
        } catch (Exception ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(AuthError.class);
        }
        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");

        server.shutdown();
    }

    @Test
    public void authRetryOn407() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(407)

                .setBody("Invalid token"));
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody("nice :)"));
        String result = queue.add(new StringRequest("POST", server.url("/").toString())
                .setRequestBody("try me!")).getFuture().get();

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("nice :)");

        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");

        request = server.takeRequest();
        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");
    }


    @Test
    public void authRetryOn407Fail() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(407)
                .setBody("Invalid token"));
        server.enqueue(new MockResponse().setResponseCode(407)
                .setBody("Invalid token"));
        try {
            queue.add(new StringRequest("POST", server.url("/").toString())
                    .setRequestBody("try me!")).getFuture().get();
            fail("Must not retry 2 times");
        } catch (Exception ex) {
            assertThat(ex).hasCauseExactlyInstanceOf(AuthError.class);
        }
        RecordedRequest request = server.takeRequest();

        assertThat(request.getBody().readByteString().utf8()).isEqualTo("try me!");

        server.shutdown();
    }

}
