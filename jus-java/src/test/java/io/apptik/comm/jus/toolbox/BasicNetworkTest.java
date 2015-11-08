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

import io.apptik.comm.jus.FormEncodingBuilder;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.mock.MockHttpStack;

import static org.junit.Assert.assertEquals;

public class BasicNetworkTest {

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
}
