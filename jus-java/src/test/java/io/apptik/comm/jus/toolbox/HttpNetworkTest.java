/*
 * Copyright (C) 2015 Apptik Project
 * Copyright (C) 2014 Kalin Maldzhanski
 * Copyright (C) 2011 The Android Open Source Project
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

package io.apptik.comm.jus.toolbox;

import org.junit.Test;

import io.apptik.comm.jus.Cache;
import io.apptik.comm.jus.http.FormEncodingBuilder;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.mock.MockHttpStack;

import static org.junit.Assert.assertEquals;

public class HttpNetworkTest {

    @Test public void headersAndPostParams() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        final NetworkResponse fakeResponse = new NetworkResponse(200, "foobar".getBytes(), new Headers
                .Builder().add("requestheader", "foo").build(), 0);

        mockHttpStack.setResponseToReturn(fakeResponse);
        HttpNetwork httpNetwork = new HttpNetwork(mockHttpStack);
        Request<String> request = new Request<String>(Request.Method.GET, "http://foo") {
            @Override
            protected void deliverResponse(String response) {
            }
        };
        request.setNetworkRequest(new FormEncodingBuilder()
                .add("requestpost", "foo")
                .builder().setHeader("requestheader", "foo").build());
        httpNetwork.performRequest(request);
        assertEquals("foo", mockHttpStack.getLastHeaders().get("requestheader"));
        assertEquals("requestpost=foo", new String(mockHttpStack.getLastPostBody()));
    }

    @Test public void cacheHeadersFromCache() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        final NetworkResponse fakeResponse = new NetworkResponse(304, new byte[0],
                new Headers.Builder().add("responseheader", "response-foo").build(), 0);

        mockHttpStack.setResponseToReturn(fakeResponse);
        HttpNetwork httpNetwork = new HttpNetwork(mockHttpStack);
        Request<NetworkResponse> request = new Request<NetworkResponse>(Request.Method.GET, "http://foo") {
            @Override
            protected void deliverResponse(NetworkResponse response) {
            }

            @Override
            public Cache.Entry getCacheEntry() {
                Cache.Entry entry = new Cache.Entry();
                entry.data = "cached-foo".getBytes();
                entry.responseHeaders = new Headers.Builder()
                        .add("Date: Sun, 15 May 2016 11:03:26 GMT")
                        .add("ETag: W/\"123456\"")
                        .add("Cache-Control: public, max-age=60, s-maxage=60")
                        .add("Expires: Mon, 14 Nov 2016 11:03:26 GMT")
                        .add("Vary: Accept")
                        .add("Content-Location: /cache")
                        .build();
                return entry;
            }
        };
        request.setNetworkRequest(new NetworkRequest.Builder()
                .setHeader("requestheader", "foo").build());
        NetworkResponse networkResponse =  httpNetwork.performRequest(request);

        assertEquals("foo", mockHttpStack.getLastHeaders().get("requestheader"));
        assertEquals("cached-foo", new String(networkResponse.data));
        assertEquals("response-foo", networkResponse.headers.get("responseheader"));
        assertEquals("Sun, 15 May 2016 11:03:26 GMT", networkResponse.headers.get("Date"));
        assertEquals("W/\"123456\"", networkResponse.headers.get("ETag"));
        assertEquals("public, max-age=60, s-maxage=60", networkResponse.headers.get("Cache-Control"));
        assertEquals("Mon, 14 Nov 2016 11:03:26 GMT", networkResponse.headers.get("Expires"));
        assertEquals("Accept", networkResponse.headers.get("Vary"));
        assertEquals("/cache", networkResponse.headers.get("Content-Location"));
    }

    @Test public void cacheHeadersFromResponse() throws Exception {
        MockHttpStack mockHttpStack = new MockHttpStack();
        final NetworkResponse fakeResponse = new NetworkResponse(304, new byte[0],
                new Headers.Builder().add("responseheader", "response-foo")
                        .add("Date: Sun, 15 May 2016 11:03:27 GMT")
                        .add("ETag: W/\"123457\"")
                        .add("Cache-Control: public, max-age=70, s-maxage=70")
                        .add("Expires: Mon, 14 Nov 2016 11:03:27 GMT")
                        .add("Vary: Accept7")
                        .add("Content-Location: /response")
                        .build(), 0);

        mockHttpStack.setResponseToReturn(fakeResponse);
        HttpNetwork httpNetwork = new HttpNetwork(mockHttpStack);
        Request<NetworkResponse> request = new Request<NetworkResponse>(Request.Method.GET, "http://foo") {
            @Override
            protected void deliverResponse(NetworkResponse response) {
            }

            @Override
            public Cache.Entry getCacheEntry() {
                Cache.Entry entry = new Cache.Entry();
                entry.data = "cached-foo".getBytes();
                entry.responseHeaders = new Headers.Builder()
                        .add("Date: Sun, 15 May 2016 11:03:26 GMT")
                        .add("ETag: W/\"123456\"")
                        .add("Cache-Control: public, max-age=60, s-maxage=60")
                        .add("Expires: Mon, 14 Nov 2016 11:03:26 GMT")
                        .add("Vary: Accept")
                        .add("Content-Location: /cache")
                        .build();
                return entry;
            }
        };
        request.setNetworkRequest(new NetworkRequest.Builder()
                .setHeader("requestheader", "foo").build());
        NetworkResponse networkResponse =  httpNetwork.performRequest(request);

        assertEquals("foo", mockHttpStack.getLastHeaders().get("requestheader"));
        assertEquals("cached-foo", new String(networkResponse.data));
        assertEquals("response-foo", networkResponse.headers.get("responseheader"));
        assertEquals(1,networkResponse.headers.values("Date").size());
        assertEquals("Sun, 15 May 2016 11:03:27 GMT", networkResponse.headers.get("Date"));
        assertEquals(1,networkResponse.headers.values("ETag").size());
        assertEquals("W/\"123457\"", networkResponse.headers.get("ETag"));
        assertEquals(1,networkResponse.headers.values("Cache-Control").size());
        assertEquals("public, max-age=70, s-maxage=70", networkResponse.headers.get("Cache-Control"));
        assertEquals(1,networkResponse.headers.values("Expires").size());
        assertEquals("Mon, 14 Nov 2016 11:03:27 GMT", networkResponse.headers.get("Expires"));
        assertEquals(1,networkResponse.headers.values("Vary").size());
        assertEquals("Accept7", networkResponse.headers.get("Vary"));
        assertEquals(1,networkResponse.headers.values("Content-Location").size());
        assertEquals("/response", networkResponse.headers.get("Content-Location"));
    }

}
