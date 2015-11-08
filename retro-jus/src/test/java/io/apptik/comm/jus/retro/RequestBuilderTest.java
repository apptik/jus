// Copyright 2013 Square, Inc.
package io.apptik.comm.jus.retro;

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.MediaType;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.DELETE;
import io.apptik.comm.jus.retro.http.Field;
import io.apptik.comm.jus.retro.http.FieldMap;
import io.apptik.comm.jus.retro.http.FormUrlEncoded;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.HEAD;
import io.apptik.comm.jus.retro.http.HTTP;
import io.apptik.comm.jus.retro.http.Header;
import io.apptik.comm.jus.retro.http.Headers;
import io.apptik.comm.jus.retro.http.Multipart;
import io.apptik.comm.jus.retro.http.PATCH;
import io.apptik.comm.jus.retro.http.POST;
import io.apptik.comm.jus.retro.http.PUT;
import io.apptik.comm.jus.retro.http.Part;
import io.apptik.comm.jus.retro.http.PartMap;
import io.apptik.comm.jus.retro.http.Path;
import io.apptik.comm.jus.retro.http.Priority;
import io.apptik.comm.jus.retro.http.Query;
import io.apptik.comm.jus.retro.http.QueryMap;
import io.apptik.comm.jus.retro.http.ShouldCache;
import io.apptik.comm.jus.retro.http.Tag;
import io.apptik.comm.jus.retro.http.Url;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@SuppressWarnings({"UnusedParameters", "unused"}) // Parameters inspected reflectively.
public final class RequestBuilderTest {
    private static final MediaType TEXT_PLAIN = MediaType.parse("text/plain");

    @Test
    public void customMethodNoBody() {
        class Example {
            @HTTP(method = "CUSTOM1", path = "/foo")
            Request<NetworkResponse> method() {
                return null;
            }
        }

        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("CUSTOM1");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void customMethodWithBody() {
        class Example {
            @HTTP(method = "CUSTOM2", path = "/foo", hasBody = true)
            Request<NetworkResponse> method(@Body NetworkRequest body) {
                return null;
            }
        }

        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "hi");
        Request request = buildRequest(Example.class, body);
        assertThat(request.getMethod()).isEqualTo("CUSTOM2");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo");
        assertBody(request.getNetworkRequest(), "hi");
    }

    @Test
    public void onlyOneEncodingIsAllowedMultipartFirst() {
        class Example {
            @Multipart //
            @FormUrlEncoded //
            @POST("/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Only one encoding annotation is allowed.\n    for method Example.method");
        }
    }

    @Test
    public void onlyOneEncodingIsAllowedFormEncodingFirst() {
        class Example {
            @FormUrlEncoded //
            @Multipart //
            @POST("/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Only one encoding annotation is allowed.\n    for method Example.method");
        }
    }

    @Test
    public void invalidPathParam() throws Exception {
        class Example {
            @GET("/")
                //
            Request<NetworkResponse> method(@Path("hey!") String thing) {
                return null;
            }
        }

        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@Path parameter name must match \\{([a-zA-Z][a-zA-Z0-9_-]*)\\}."
                            + " Found: hey! (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void pathParamNotAllowedInQuery() throws Exception {
        class Example {
            @GET("/foo?bar={bar}")
                //
            Request<NetworkResponse> method(@Path("bar") String thing) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "URL query string \"bar={bar}\" must not have replace block."
                            + " For dynamic query parameters use @Query.\n    for method Example.method");
        }
    }

    @Test
    public void multipleParameterAnnotationsNotAllowed() throws Exception {
        class Example {
            @GET("/")
                //
            Request<NetworkResponse> method(@Body @Query("nope") String o) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Multiple RetroProxy annotations found, only one allowed. (parameter #1)\n    for method Example.method");
        }
    }

    @interface NonNull {
    }

    @Test
    public void multipleParameterAnnotationsOnlyOneRetroProxyAllowed() throws Exception {
        class Example {
            @GET("/")
                //
            Request<NetworkResponse> method(@Query("maybe") @NonNull Object o) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "yep");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/?maybe=yep");
    }

    @Test
    public void twoMethodsFail() {
        class Example {
            @PATCH("/foo") //
            @POST("/foo")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }

        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Only one HTTP method is allowed. Found: PATCH and POST.\n    for method Example.method");
        }
    }

    @Test
    public void lackingMethod() {
        class Example {
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "HTTP method annotation is required (e.g., @GET, @POST, etc.).\n    for method Example.method");
        }
    }

    @Test
    public void implicitMultipartForbidden() {
        class Example {
            @POST("/")
                //
            Request<NetworkResponse> method(@Part("a") int a) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@Part parameters can only be used with multipart encoding. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void implicitMultipartWithPartMapForbidden() {
        class Example {
            @POST("/")
                //
            Request<NetworkResponse> method(@PartMap Map<String, String> params) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@PartMap parameters can only be used with multipart encoding. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void multipartFailsOnNonBodyMethod() {
        class Example {
            @Multipart //
            @GET("/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Multipart can only be specified on HTTP methods with request body (e.g., @POST).\n    for method Example.method");
        }
    }

    @Test
    public void multipartFailsWithNoParts() {
        class Example {
            @Multipart //
            @POST("/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Multipart method must contain at least one @Part.\n    for method Example.method");
        }
    }

    @Test
    public void implicitFormEncodingByFieldForbidden() {
        class Example {
            @POST("/")
                //
            Request<NetworkResponse> method(@Field("a") int a) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@Field parameters can only be used with form encoding. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void implicitFormEncodingByFieldMapForbidden() {
        class Example {
            @POST("/")
                //
            Request<NetworkResponse> method(@FieldMap Map<String, String> a) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@FieldMap parameters can only be used with form encoding. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void formEncodingFailsOnNonBodyMethod() {
        class Example {
            @FormUrlEncoded //
            @GET("/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "FormUrlEncoded can only be specified on HTTP methods with request body (e.g., @POST).\n    for method Example.method");
        }
    }

    @Test
    public void formEncodingFailsWithNoParts() {
        class Example {
            @FormUrlEncoded //
            @POST("/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Form-encoded method must contain at least one @Field.\n    for method Example.method");
        }
    }

    @Test
    public void headersFailWhenEmptyOnMethod() {
        class Example {
            @GET("/") //
            @Headers({})
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("@Headers annotation is empty.\n    for method Example.method");
        }
    }

    @Test
    public void headersFailWhenMalformed() {
        class Example {
            @GET("/") //
            @Headers("Malformed")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@Headers value must be in the form \"Name: Value\". Found: \"Malformed\"\n    for method Example.method");
        }
    }

    @Test
    public void pathParamNonPathParamAndTypedBytes() {
        class Example {
            @PUT("/{a}")
                //
            Request<NetworkResponse> method(@Path("a") int a, @Path("b") int b, @Body int c) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "URL \"/{a}\" does not contain \"{b}\". (parameter #2)\n    for method Example.method");
        }
    }

    @Test
    public void parameterWithoutAnnotation() {
        class Example {
            @GET("/")
                //
            Request<NetworkResponse> method(String a) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "No RetroProxy annotation found. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void nonBodyHttpMethodWithSingleEntity() {
        class Example {
            @GET("/")
                //
            Request<NetworkResponse> method(@Body String o) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Non-body HTTP method cannot contain @Body.\n    for method Example.method");
        }
    }

    @Test
    public void queryMapMustBeAMap() {
        class Example {
            @GET("/")
                //
            Request<NetworkResponse> method(@QueryMap List<String> a) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@QueryMap parameter type must be Map. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void queryMapRejectsNullKeys() {
        class Example {
            @GET("/")
                //
            Request<NetworkResponse> method(@QueryMap Map<String, String> a) {
                return null;
            }
        }

        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put("ping", "pong");
        queryParams.put(null, "kat");

        try {
            buildRequest(Example.class, queryParams);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Query map contained null key.");
        }
    }

    @Test
    public void twoBodies() {
        class Example {
            @PUT("/")
                //
            Request<NetworkResponse> method(@Body String o1, @Body String o2) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "Multiple @Body method annotations found. (parameter #2)\n    for method Example.method");
        }
    }

    @Test
    public void bodyInNonBodyRequest() {
        class Example {
            @Multipart //
            @PUT("/")
                //
            Request<NetworkResponse> method(@Part("one") String o1, @Body String o2) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@Body parameters cannot be used with form or multi-part encoding. (parameter #2)\n    for method Example.method");
        }
    }

    @Test
    public void get() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void delete() {
        class Example {
            @DELETE("/foo/bar/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertNull(request.getNetworkRequest().data);
    }

    @Test
    public void head() {
        class Example {
            @HEAD("/foo/bar/")
                //
            Request<Void> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("HEAD");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void headWithoutVoidThrows() {
        class Example {
            @HEAD("/foo/bar/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "HEAD method must use Void as response type.\n    for method Example.method");
        }
    }

    @Test
    public void post() {
        class Example {
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "hi");
        Request request = buildRequest(Example.class, body);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "hi");
    }

    @Test
    public void put() {
        class Example {
            @PUT("/foo/bar/")
                //
            Request<NetworkResponse> method(@Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "hi");
        Request request = buildRequest(Example.class, body);
        assertThat(request.getMethod()).isEqualTo("PUT");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "hi");
    }

    @Test
    public void patch() {
        class Example {
            @PATCH("/foo/bar/")
                //
            Request<NetworkResponse> method(@Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "hi");
        Request request = buildRequest(Example.class, body);
        assertThat(request.getMethod()).isEqualTo("PATCH");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "hi");
    }

    @Test
    public void getWithPathParam() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "po ng");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/po%20ng/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithUnusedAndInvalidNamedPathParam() {
        class Example {
            @GET("/foo/bar/{ping}/{kit,kat}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "pong");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/pong/%7Bkit,kat%7D/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithEncodedPathParam() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path(value = "ping", encoded = true) String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "po%20ng");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/po%20ng/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void pathParamRequired() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping) {
                return null;
            }
        }
        try {
            buildRequest(Example.class, new Object[]{null});
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Path parameter \"ping\" value must not be null.");
        }
    }

    @Test
    public void getWithQueryParam() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Query("ping") String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "pong");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?ping=pong");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithEncodedQueryParam() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Query(value = "pi%20ng", encoded = true) String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "p%20o%20n%20g");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?pi%20ng=p%20o%20n%20g");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void queryParamOptionalOmitsQuery() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Query("ping") String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, new Object[]{null});
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
    }

    @Test
    public void queryParamOptional() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Query("foo") String foo, @Query("ping") String ping,
                                            @Query("kit") String kit) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "bar", null, "kat");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?foo=bar&kit=kat");
    }

    @Test
    public void getWithQueryUrlAndParam() {
        class Example {
            @GET("/foo/bar/?hi=mom")
                //
            Request<NetworkResponse> method(@Query("ping") String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "pong");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?hi=mom&ping=pong");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithQuery() {
        class Example {
            @GET("/foo/bar/?hi=mom")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?hi=mom");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithPathAndQueryParam() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping, @Query("kit") String kit,
                                            @Query("riff") String riff) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "pong", "kat", "raff");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/pong/?kit=kat&riff=raff");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithQueryThenPathThrows() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Query("kit") String kit, @Path("ping") String ping) {
                return null;
            }
        }

        try {
            buildRequest(Example.class, "kat", "pong");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("A @Path parameter must not come after a @Query. (parameter #2)\n"
                    + "    for method Example.method");
        }
    }

    @Test
    public void getWithPathAndQueryQuestionMarkParam() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping, @Query("kit") String kit) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "pong?", "kat?");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/pong%3F/?kit=kat?");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithPathAndQueryAmpersandParam() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping, @Query("kit") String kit) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "pong&", "kat&");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/pong&/?kit=kat%26");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithPathAndQueryHashParam() {
        class Example {
            @GET("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping, @Query("kit") String kit) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "pong#", "kat#");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/pong%23/?kit=kat%23");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithQueryParamList() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Query("key") List<Object> keys) {
                return null;
            }
        }

        List<Object> values = Arrays.<Object>asList(1, 2, null, "three");
        Request request = buildRequest(Example.class, values);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?key=1&key=2&key=three");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithQueryParamArray() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Query("key") Object[] keys) {
                return null;
            }
        }

        Object[] values = {1, 2, null, "three"};
        Request request = buildRequest(Example.class, new Object[]{values});
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?key=1&key=2&key=three");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithQueryParamPrimitiveArray() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Query("key") int[] keys) {
                return null;
            }
        }

        int[] values = {1, 2, 3};
        Request request = buildRequest(Example.class, new Object[]{values});
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?key=1&key=2&key=3");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithQueryParamMap() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@QueryMap Map<String, Object> query) {
                return null;
            }
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("kit", "kat");
        params.put("foo", null);
        params.put("ping", "pong");

        Request request = buildRequest(Example.class, params);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?kit=kat&ping=pong");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithEncodedQueryParamMap() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@QueryMap(encoded = true) Map<String, Object> query) {
                return null;
            }
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("kit", "k%20t");
        params.put("foo", null);
        params.put("pi%20ng", "p%20g");

        Request request = buildRequest(Example.class, params);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?kit=k%20t&pi%20ng=p%20g");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithUrl() {
        class Example {
            @GET
            Request<NetworkResponse> method(@Url String url) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "foo/bar/");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getAbsoluteUrl() {
        class Example {
            @GET("http://example2.com/foo/bar/")
            Request<NetworkResponse> method() {
                return null;
            }
        }

        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example2.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithUrlAbsolute() {
        class Example {
            @GET
            Request<NetworkResponse> method(@Url String url) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "https://example2.com/foo/bar/");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("https://example2.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithUrlAbsoluteSameHost() {
        class Example {
            @GET
            Request<NetworkResponse> method(@Url String url) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "http://example.com/foo/bar/");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void getWithNonStringUrlThrows() {
        class Example {
            @GET
            Request<NetworkResponse> method(@Url Object url) {
                return null;
            }
        }

        try {
            buildRequest(Example.class, "foo/bar");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("@Url must be String type. (parameter #1)\n"
                    + "    for method Example.method");
        }
    }

    @Test
    public void getUrlAndUrlParamThrows() {
        class Example {
            @GET("foo/bar")
            Request<NetworkResponse> method(@Url Object url) {
                return null;
            }
        }

        try {
            buildRequest(Example.class, "foo/bar");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("@Url must be String type. (parameter #1)\n"
                    + "    for method Example.method");
        }
    }

    @Test
    public void getWithoutUrlThrows() {
        class Example {
            @GET
            Request<NetworkResponse> method() {
                return null;
            }
        }

        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Missing either @GET URL or @Url parameter.\n"
                    + "    for method Example.method");
        }
    }

    @Test
    public void getWithUrlThenPathThrows() {
        class Example {
            @GET
            Request<NetworkResponse> method(@Url String url, @Path("hey") String hey) {
                return null;
            }
        }

        try {
            buildRequest(Example.class, "foo/bar");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("@Path parameters may not be used with @Url. (parameter #2)\n"
                    + "    for method Example.method");
        }
    }

    @Test
    public void getWithPathThenUrlThrows() {
        class Example {
            @GET
            Request<NetworkResponse> method(@Path("hey") String hey, @Url Object url) {
                return null;
            }
        }

        try {
            buildRequest(Example.class, "foo/bar");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("@Path can only be used with relative url on @GET (parameter #1)\n"
                    + "    for method Example.method");
        }
    }

    @Test
    public void getWithQueryThenUrlThrows() {
        class Example {
            @GET("foo/bar")
            Request<NetworkResponse> method(@Query("hey") String hey, @Url Object url) {
                return null;
            }
        }

        try {
            buildRequest(Example.class, "hey", "foo/bar/");
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("A @Url parameter must not come after a @Query (parameter #2)\n"
                    + "    for method Example.method");
        }
    }

    @Test
    public void getWithUrlThenQuery() {
        class Example {
            @GET
            Request<NetworkResponse> method(@Url String url, @Query("hey") String hey) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "foo/bar/", "hey!");
        assertThat(request.getMethod()).isEqualTo("GET");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/?hey=hey!");
    }

    @Test
    public void postWithUrl() {
        class Example {
            @POST
            Request<NetworkResponse> method(@Url String url, @Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "hi");
        Request request = buildRequest(Example.class, "http://example.com/foo/bar", body);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar");
        assertBody(request.getNetworkRequest(), "hi");
    }

    @Test
    public void normalPostWithPathParam() {
        class Example {
            @POST("/foo/bar/{ping}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping, @Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(TEXT_PLAIN, "Hi!");
        Request request = buildRequest(Example.class, "pong", body);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/pong/");
        assertBody(request.getNetworkRequest(), "Hi!");
    }

    @Test
    public void emptyBody() {
        class Example {
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
    }

    @Test
    public void requestTagInMethod() {
        class Example {
            @Tag("cool tag")
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.getTag()).isEqualTo("cool tag");
    }

    @Test
    public void requestTagInParam() {
<<<<<<< HEAD
=======
        class Example {
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Tag String tag) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "cool tag");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.getTag()).isEqualTo("cool tag");
    }

    @Test
    public void requestDoNotCache() {
>>>>>>> 102c788... closes 15
        class Example {
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Tag String tag) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "cool tag");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.getTag()).isEqualTo("cool tag");
    }

    @Test
    public void requestDoCache() {
        class Example {
            @ShouldCache(true)
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.shouldCache()).isEqualTo(true);
    }

    @Test
    public void requestDoNotCacheInParamPrimitive() {
        class Example {
            @POST("/foo/bar/")
            Request<NetworkResponse> method(@ShouldCache boolean shouldCache) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, false);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.shouldCache()).isEqualTo(false);
    }

    @Test
    public void requestDoNotCacheInParamWrapper() {
        class Example {
            @POST("/foo/bar/")
            Request<NetworkResponse> method(@ShouldCache Boolean shouldCache) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, Boolean.FALSE);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.shouldCache()).isEqualTo(false);
    }

    @Test
    public void requestDoNotCacheInMethod() {
        class Example {
            @ShouldCache(false)
            @POST("/foo/bar/")
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.shouldCache()).isEqualTo(false);
    }


    @Test
    public void requestDefaultShouldCache() {
        class Example {
            @ShouldCache
            @POST("/foo/bar/")
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.shouldCache()).isEqualTo(true);
    }
    @Test
    public void requestDefaultShouldCache2() {
        class Example {
            @POST("/foo/bar/")
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.shouldCache()).isEqualTo(true);
    }

    @Test
    public void requestPriorityInParam() {
        class Example {
            @POST("/foo/bar/")
            Request<NetworkResponse> method(@Priority Request.Priority priority) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, Request.Priority.HIGH);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.getPriority()).isEqualTo(Request.Priority.HIGH);
    }

    @Test
    public void requestPriorityInParamBadType() {
        class Example {
            @POST("/foo/bar/")
            Request<NetworkResponse> method(@Priority String priority) {
                return null;
            }
        }
        try {
            Request request = buildRequest(Example.class, "wrong!");
            fail();
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage().equals("@Priority must be Request.Priority type. " +
                    "(parameter #1)"));
        }
    }

    @Test
    public void requestPriorityInMethod() {
        class Example {
            @Priority(Request.Priority.HIGH)
            @POST("/foo/bar/")
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.getPriority()).isEqualTo(Request.Priority.HIGH);
    }

    @Test
    public void requestDefaultPriority() {
        class Example {
            @Priority
            @POST("/foo/bar/")
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
        assertThat(request.getPriority()).isEqualTo(Request.Priority.NORMAL);
    }

    @Test
    public void customMethodEmptyBody() {
        class Example {
            @HTTP(method = "CUSTOM", path = "/foo/bar/", hasBody = true)
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("CUSTOM");
        assertThat(request.getHeaders().size()).isEqualTo(0);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "");
    }

    @Test
    public void bodyNetworkResponse() {
        class Example {
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(TEXT_PLAIN, "hi");
        Request request = buildRequest(Example.class, body);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertBody(request.getNetworkRequest(), "hi");
    }

    @Test
    public void bodyRequired() {
        class Example {
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Body NetworkRequest body) {
                return null;
            }
        }
        try {
            buildRequest(Example.class, new Object[]{null});
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Body parameter value must not be null.");
        }
    }

    @Test
    public void bodyWithPathParams() {
        class Example {
            @POST("/foo/bar/{ping}/{kit}/")
                //
            Request<NetworkResponse> method(@Path("ping") String ping, @Body NetworkRequest body, @Path("kit") String kit) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(TEXT_PLAIN, "Hi!");
        Request request = buildRequest(Example.class, "pong", body, "kat");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/pong/kat/");
        assertBody(request.getNetworkRequest(), "Hi!");
    }

    @Test
    public void simpleMultipart() throws IOException {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Part("ping") String ping, @Part("kit") NetworkRequest kit) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "pong", NetworkRequest.create(
                MediaType.parse("text/plain"), "kat"));
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");

        String bodyString = request.getNetworkRequest().getBodyAsString();

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"ping\"\r\n")
                .contains("\r\npong\r\n--");

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"kit\"")
                .contains("\r\nkat\r\n--");
    }

    @Test
    public void multipartWithEncoding() throws IOException {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Part(value = "ping", encoding = "8-bit") String ping,
                                            @Part(value = "kit", encoding = "7-bit") NetworkRequest kit) {
                return null;
            }
        }

        Request request = buildRequest(Example.class, "pong", NetworkRequest.create(
                MediaType.parse("text/plain"), "kat"));
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");

        String bodyString = request.getNetworkRequest().getBodyAsString();

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"ping\"\r\n")
                .contains("Content-Transfer-Encoding: 8-bit")
                .contains("\r\npong\r\n--");

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"kit\"")
                .contains("Content-Transfer-Encoding: 7-bit")
                .contains("\r\nkat\r\n--");
    }

    @Test
    public void multipartPartMap() throws IOException {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@PartMap Map<String, Object> parts) {
                return null;
            }
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ping", "pong");
        params.put("foo", null); // Should be skipped.
        params.put("kit", "kat");

        Request request = buildRequest(Example.class, params);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");

        String bodyString = request.getNetworkRequest().getBodyAsString();

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"ping\"\r\n")
                .contains("\r\npong\r\n--");

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"kit\"")
                .contains("\r\nkat\r\n--");

        assertThat(bodyString).doesNotContain("name=\"foo\"\r\n");
    }

    @Test
    public void multipartPartMapWithEncoding() throws IOException {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@PartMap(encoding = "8-bit") Map<String, Object> parts) {
                return null;
            }
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ping", "pong");
        params.put("foo", null); // Should be skipped.
        params.put("kit", "kat");

        Request request = buildRequest(Example.class, params);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");

        String bodyString = request.getNetworkRequest().getBodyAsString();

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"ping\"\r\n")
                .contains("Content-Transfer-Encoding: 8-bit")
                .contains("\r\npong\r\n--");

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"kit\"")
                .contains("Content-Transfer-Encoding: 8-bit")
                .contains("\r\nkat\r\n--");

        assertThat(bodyString).doesNotContain("name=\"foo\"\r\n");
    }

    @Test
    public void multipartPartMapRejectsNullKeys() {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@PartMap Map<String, Object> parts) {
                return null;
            }
        }

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("ping", "pong");
        params.put(null, "kat");

        try {
            buildRequest(Example.class, params);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Part map contained null key.");
        }
    }

    @Test
    public void multipartPartMapMustBeMap() {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@PartMap List<Object> parts) {
                return null;
            }
        }

        try {
            buildRequest(Example.class, Collections.emptyList());
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@PartMap parameter type must be Map. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void multipartNullRemovesPart() throws IOException {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Part("ping") String ping, @Part("fizz") String fizz) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "pong", null);
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeaders().size()).isEqualTo(1);
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");

        String bodyString = request.getNetworkRequest().getBodyAsString();

        assertThat(bodyString)
                .contains("Content-Disposition: form-data;")
                .contains("name=\"ping\"")
                .contains("\r\npong\r\n--");
    }

    @Test
    public void multipartPartOptional() {
        class Example {
            @Multipart //
            @POST("/foo/bar/")
                //
            Request<NetworkResponse> method(@Part("ping") NetworkRequest ping) {
                return null;
            }
        }
        try {
            buildRequest(Example.class, new Object[]{null});
            fail();
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("Multipart body must have at least one part.");
        }
    }

    @Test
    public void simpleFormEncoded() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@Field("foo") String foo, @Field("ping") String ping) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "bar", "pong");
        assertBody(request.getNetworkRequest(), "foo=bar&ping=pong");
    }

    @Test
    public void formEncodedWithEncodedNameFieldParam() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@Field(value = "na%20me", encoded = true) String foo) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "ba%20r");
        assertBody(request.getNetworkRequest(), "na%20me=ba%20r");
    }

    @Test
    public void formEncodedFieldOptional() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@Field("foo") String foo, @Field("ping") String ping,
                                            @Field("kit") String kit) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "bar", null, "kat");
        assertBody(request.getNetworkRequest(), "foo=bar&kit=kat");
    }

    @Test
    public void formEncodedFieldList() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@Field("foo") List<Object> fields, @Field("kit") String kit) {
                return null;
            }
        }

        List<Object> values = Arrays.<Object>asList("foo", "bar", null, 3);
        Request request = buildRequest(Example.class, values, "kat");
        assertBody(request.getNetworkRequest(), "foo=foo&foo=bar&foo=3&kit=kat");
    }

    @Test
    public void formEncodedFieldArray() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@Field("foo") Object[] fields, @Field("kit") String kit) {
                return null;
            }
        }

        Object[] values = {1, 2, null, "three"};
        Request request = buildRequest(Example.class, values, "kat");
        assertBody(request.getNetworkRequest(), "foo=1&foo=2&foo=three&kit=kat");
    }

    @Test
    public void formEncodedFieldPrimitiveArray() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@Field("foo") int[] fields, @Field("kit") String kit) {
                return null;
            }
        }

        int[] values = {1, 2, 3};
        Request request = buildRequest(Example.class, values, "kat");
        assertBody(request.getNetworkRequest(), "foo=1&foo=2&foo=3&kit=kat");
    }

    @Test
    public void formEncodedWithEncodedNameFieldParamMap() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@FieldMap(encoded = true) Map<String, Object> fieldMap) {
                return null;
            }
        }

        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("k%20it", "k%20at");
        fieldMap.put("pin%20g", "po%20ng");

        Request request = buildRequest(Example.class, fieldMap);
        assertBody(request.getNetworkRequest(), "k%20it=k%20at&pin%20g=po%20ng");
    }

    @Test
    public void formEncodedFieldMap() {
        class Example {
            @FormUrlEncoded //
            @POST("/foo")
                //
            Request<NetworkResponse> method(@FieldMap Map<String, Object> fieldMap) {
                return null;
            }
        }

        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("kit", "kat");
        fieldMap.put("foo", null);
        fieldMap.put("ping", "pong");

        Request request = buildRequest(Example.class, fieldMap);
        assertBody(request.getNetworkRequest(), "kit=kat&ping=pong");
    }

    @Test
    public void fieldMapRejectsNullKeys() {
        class Example {
            @FormUrlEncoded //
            @POST("/")
                //
            Request<NetworkResponse> method(@FieldMap Map<String, Object> a) {
                return null;
            }
        }

        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("kit", "kat");
        fieldMap.put("foo", null);
        fieldMap.put(null, "pong");

        try {
            buildRequest(Example.class, fieldMap);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Field map contained null key.");
        }
    }

    @Test
    public void fieldMapMustBeAMap() {
        class Example {
            @FormUrlEncoded //
            @POST("/")
                //
            Request<NetworkResponse> method(@FieldMap List<String> a) {
                return null;
            }
        }
        try {
            buildRequest(Example.class);
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage(
                    "@FieldMap parameter type must be Map. (parameter #1)\n    for method Example.method");
        }
    }

    @Test
    public void simpleHeaders() {
        class Example {
            @GET("/foo/bar/")
            @Headers({
                    "ping: pong",
                    "kit: kat"
            })
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getMethod()).isEqualTo("GET");
        io.apptik.comm.jus.http.Headers headers = request.getHeaders();
        assertThat(headers.size()).isEqualTo(2);
        assertThat(headers.get("ping")).isEqualTo("pong");
        assertThat(headers.get("kit")).isEqualTo("kat");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void headerParamToString() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Header("kit") BigInteger kit) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, new BigInteger("1234"));
        assertThat(request.getMethod()).isEqualTo("GET");
        io.apptik.comm.jus.http.Headers headers = request.getHeaders();
        assertThat(headers.size()).isEqualTo(1);
        assertThat(headers.get("kit")).isEqualTo("1234");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void headerParam() {
        class Example {
            @GET("/foo/bar/") //
            @Headers("ping: pong")
                //
            Request<NetworkResponse> method(@Header("kit") String kit) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, "kat");
        assertThat(request.getMethod()).isEqualTo("GET");
        io.apptik.comm.jus.http.Headers headers = request.getHeaders();
        assertThat(headers.size()).isEqualTo(2);
        assertThat(headers.get("ping")).isEqualTo("pong");
        assertThat(headers.get("kit")).isEqualTo("kat");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void headerParamList() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Header("foo") List<String> kit) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, Arrays.asList("bar", null, "baz"));
        assertThat(request.getMethod()).isEqualTo("GET");
        io.apptik.comm.jus.http.Headers headers = request.getHeaders();
        assertThat(headers.size()).isEqualTo(2);
        assertThat(headers.values("foo")).containsExactly("bar", "baz");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void headerParamArray() {
        class Example {
            @GET("/foo/bar/")
                //
            Request<NetworkResponse> method(@Header("foo") String[] kit) {
                return null;
            }
        }
        Request request = buildRequest(Example.class, (Object) new String[]{"bar", null, "baz"});
        assertThat(request.getMethod()).isEqualTo("GET");
        io.apptik.comm.jus.http.Headers headers = request.getHeaders();
        assertThat(headers.size()).isEqualTo(2);
        assertThat(headers.values("foo")).containsExactly("bar", "baz");
        assertThat(request.getUrlString()).isEqualTo("http://example.com/foo/bar/");
        assertThat(request.getNetworkRequest().data).isNull();
    }

    @Test
    public void contentTypeAnnotationHeaderOverrides() {
        class Example {
            @POST("/") //
            @Headers("Content-Type: text/not-plain")
                //
            Request<NetworkResponse> method(@Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "hi");
        Request request = buildRequest(Example.class, body);
        assertThat(request.getNetworkRequest().contentType.toString()).isEqualTo("text/not-plain");
        assertThat(request.getBodyContentType()).isEqualTo("text/not-plain");
    }

    @Test
    public void contentTypeAnnotationHeaderAddsHeaderWithNoBody() {
        class Example {
            @DELETE("/") //
            @Headers("Content-Type: text/not-plain")
                //
            Request<NetworkResponse> method() {
                return null;
            }
        }
        Request request = buildRequest(Example.class);
        assertThat(request.getHeaders().get("Content-Type")).isEqualTo("text/not-plain");
    }

    @Test
    public void contentTypeParameterHeaderOverrides() {
        class Example {
            @POST("/")
                //
            Request<NetworkResponse> method(@Header("Content-Type") String contentType, @Body NetworkRequest body) {
                return null;
            }
        }
        NetworkRequest body = NetworkRequest.create(MediaType.parse("text/plain"), "Plain");
        Request request = buildRequest(Example.class, "text/not-plain", body);
        assertThat(request.getNetworkRequest().contentType.toString()).isEqualTo("text/not-plain");
    }

    private static void assertBody(NetworkRequest body, String expected) {
        assertThat(body).isNotNull();
        assertThat(body.getBodyAsString()).isEqualTo(expected);
    }

    private Request buildRequest(Class<?> cls, Object... args) {

        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl("http://example.com/")
                .execManually()
                .addConverterFactory(new ToStringConverterFactory())
                .build();

        Method method = TestingUtils.onlyMethod(cls);
        MethodHandler<?> handler = retroProxy.loadMethodHandler(method);
        Request request = (Request) handler.invoke(args);
//        try {
//            invoke.getFuture().get();
//            throw new AssertionError();
//        } catch (RuntimeException e) {
//            throw e;
//        } catch (Exception e) {
//            throw new AssertionError(e);
//        }

        return request;
    }
}
