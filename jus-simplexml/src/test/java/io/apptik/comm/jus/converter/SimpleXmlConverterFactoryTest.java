/*
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
import org.simpleframework.xml.core.ElementException;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.HyphenStyle;
import org.simpleframework.xml.stream.Verbosity;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.retro.RetroProxy;
import io.apptik.comm.jus.retro.http.Body;
import io.apptik.comm.jus.retro.http.GET;
import io.apptik.comm.jus.retro.http.POST;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class SimpleXmlConverterFactoryTest {
    interface Service {
        @GET("/")
        Request<MyObject> get();

        @POST("/")
        Request<MyObject> post(@Body MyObject impl);

        @GET("/")
        Request<CharSequence> wrongClass();
    }

    @Rule
    public final MockWebServer server = new MockWebServer();

    private Service service;
    private RequestQueue queue;

    @Before
    public void setUp() {
        queue = Jus.newRequestQueue();
        Format format = new Format(0, null, new HyphenStyle(), Verbosity.HIGH);
        Persister persister = new Persister(format);
        RetroProxy retroProxy = new RetroProxy.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(SimpleXmlConverterFactory.create(persister))
                .queue(queue)
                .build();
        service = retroProxy.create(Service.class);
    }

    @Test
    public void bodyWays() throws IOException, InterruptedException, ExecutionException {
        server.enqueue(new MockResponse().setBody(
                "<my-object><message>hello world</message><count>10</count></my-object>"));
        MyObject body = service.post(new MyObject("hello world", 10)).getFuture().get();
        assertThat(body.getMessage()).isEqualTo("hello world");
        assertThat(body.getCount()).isEqualTo(10);

        RecordedRequest request = server.takeRequest();
        assertThat(request.getBody().readUtf8()).isEqualTo(
                "<my-object><message>hello world</message><count>10</count></my-object>");
        assertThat(request.getHeader("Content-Type")).isEqualTo("application/xml; charset=UTF-8");
    }

    @Test
    public void deserializeWrongValue() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody("<myObject><foo/><bar/></myObject>"));

        Request request = service.get();
        try {
            try {
                request.getFuture().get();
            } catch (ExecutionException e) {
                //were cool ignore this as we should have runtime ex in jusError
            }
            if (request.getRawResponse().error != null) {
                throw (RuntimeException) request.getRawResponse().error.getCause();
            }
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(ElementException.class)
                    .hasMessageStartingWith("Element 'foo' does not have a match in class " +
                            "io.apptik.comm.jus.converter.MyObject");
        }
    }

    @Test
    public void deserializeWrongClass() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().setBody(
                "<my-object><message>hello world</message><count>10</count></my-object>"));

        try {
            Request request = service.wrongClass();
            try {
                request.getFuture().get();
            } catch (ExecutionException e) {
                //were cool ignore this as we should have runtime ex in jusError
            }
            if(request.getRawResponse().error != null ){
                throw (RuntimeException)request.getRawResponse().error.getCause();
            }
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getCause()).isInstanceOf(org.simpleframework.xml.core.InstantiationException.class)
                    .hasMessage("Cannot instantiate interface java.lang.CharSequence for interface java.lang.CharSequence");
        }
    }

    @After
    public void after() {
        queue.stopWhenDone();
    }
}
