/*
 * Copyright (C) 2015 AppTik Project
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apptik.comm.jus.converter;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.POST;
import okio.Buffer;
import okio.ByteString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public final class WireConverterFactoryTest {
    interface Service {
        @GET("/")
        Request<Phone> get();

        @POST("/")
        Request<Phone> post(@Body Phone impl);

        @GET("/")
        Request<CharSequence> wrongClass();

        @GET("/")
        Request<List<CharSequence>> wrongType();
    }

    @Rule
    public final MockWebServer server = new MockWebServer();

    private Service service;
    private RequestQueue queue;


    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(WireConverterFactory.create())
                .queue(queue)
                .build();
        service = retroProxy.create(Service.class);
    }

    @Test
    public void serializeAndDeserialize() throws IOException, InterruptedException, ExecutionException {
        ByteString encoded = ByteString.decodeBase64("Cg4oNTE5KSA4NjctNTMwOQ==");
        server.enqueue(new MockResponse().setBody(new Buffer().write(encoded)));

        Phone body = service.post(new Phone("(519) 867-5309")).getFuture().get();
        assertThat(body.number).isEqualTo("(519) 867-5309");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readByteString()).isEqualTo(encoded);
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/x-protobuf");
    }

    @Test
    public void deserializeEmpty() throws IOException, ExecutionException, InterruptedException {
        server.enqueue(new MockResponse());

        Phone body = service.get().getFuture().get();
        assertThat(body.number).isNull();
    }

    @Test
    public void deserializeWrongClass() throws IOException, ExecutionException, InterruptedException {
        ByteString encoded = ByteString.decodeBase64("Cg4oNTE5KSA4NjctNTMwOQ==");
        server.enqueue(new MockResponse().setBody(new Buffer().write(encoded)));

        try {
            service.wrongClass().getFuture().get();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Unable to create converter for interface java.lang.CharSequence\n"
                    + "    for method Service.wrongClass");
            assertThat(e.getCause()).hasMessage(
                    "Could not locate ResponseBody converter for interface java.lang.CharSequence. Tried:\n"
                            + " * io.apptik.comm.jus.converter.BasicConverterFactory\n"
                            + " * io.apptik.comm.jus.converter.WireConverterFactory");
        }
    }

    @Test
    public void deserializeWrongType() throws IOException {
        ByteString encoded = ByteString.decodeBase64("Cg4oNTE5KSA4NjctNTMwOQ==");
        server.enqueue(new MockResponse().setBody(new Buffer().write(encoded)));

        try {
            service.wrongType();
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("Unable to create converter for java.util.List<java.lang.CharSequence>\n"
                    + "    for method Service.wrongType");
            assertThat(e.getCause()).hasMessage(
                    "Could not locate ResponseBody converter for java.util.List<java.lang.CharSequence>. Tried:\n"
                            + " * io.apptik.comm.jus.converter.BasicConverterFactory\n"
                            + " * io.apptik.comm.jus.converter.WireConverterFactory");
        }
    }

    @Test
    public void deserializeWrongValue() throws IOException, InterruptedException {
        ByteString encoded = ByteString.decodeBase64("////");
        server.enqueue(new MockResponse().setBody(new Buffer().write(encoded)));

        Request<Phone> request = service.get();
        try {
            try {
                request.getFuture().get();
            } catch (ExecutionException e) {
                //ignore and check just error
            }
            if(request.getRawResponse().error!=null) {
                throw (EOFException)request.getRawResponse().error.getCause();
            }
            fail();
        } catch (EOFException ignored) {

        }
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }
}
