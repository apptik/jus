/*
 * Copyright (C) 2012 The Android Open Source Project
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

package io.apptik.comm.jus.stack;

import org.junit.Before;
import org.junit.Test;

import io.apptik.comm.jus.Request.Method;
import io.apptik.comm.jus.mock.MockHttpURLConnection;
import io.apptik.comm.jus.mock.TestRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HurlStackTest {

    private MockHttpURLConnection mockConnection;
    private HurlStack hurlStack;

    @Before
    public void setUp() throws Exception {
        mockConnection = new MockHttpURLConnection();
        hurlStack = new HurlStack();
    }

    @Test
    public void connectionForGetRequest() throws Exception {
        TestRequest.Get request = new TestRequest.Get();
        assertEquals(request.getMethod(), Method.GET);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("GET", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForPostRequest() throws Exception {
        TestRequest.Post request = new TestRequest.Post();
        assertEquals(request.getMethod(), Method.POST);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("POST", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForPostWithBodyRequest() throws Exception {
        TestRequest.PostWithBody request = new TestRequest.PostWithBody();
        assertEquals(request.getMethod(), Method.POST);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("POST", mockConnection.getRequestMethod());
        assertTrue(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForPutRequest() throws Exception {
        TestRequest.Put request = new TestRequest.Put();
        assertEquals(request.getMethod(), Method.PUT);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("PUT", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForPutWithBodyRequest() throws Exception {
        TestRequest.PutWithBody request = new TestRequest.PutWithBody();
        assertEquals(request.getMethod(), Method.PUT);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("PUT", mockConnection.getRequestMethod());
        assertTrue(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForDeleteRequest() throws Exception {
        TestRequest.Delete request = new TestRequest.Delete();
        assertEquals(request.getMethod(), Method.DELETE);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("DELETE", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForHeadRequest() throws Exception {
        TestRequest.Head request = new TestRequest.Head();
        assertEquals(request.getMethod(), Method.HEAD);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("HEAD", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForOptionsRequest() throws Exception {
        TestRequest.Options request = new TestRequest.Options();
        assertEquals(request.getMethod(), Method.OPTIONS);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("OPTIONS", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForTraceRequest() throws Exception {
        TestRequest.Trace request = new TestRequest.Trace();
        assertEquals(request.getMethod(), Method.TRACE);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("TRACE", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForPatchRequest() throws Exception {
        TestRequest.Patch request = new TestRequest.Patch();
        assertEquals(request.getMethod(), Method.PATCH);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("PATCH", mockConnection.getRequestMethod());
        assertFalse(mockConnection.getDoOutput());
    }

    @Test
    public void connectionForPatchWithBodyRequest() throws Exception {
        TestRequest.PatchWithBody request = new TestRequest.PatchWithBody();
        assertEquals(request.getMethod(), Method.PATCH);

        hurlStack.setConnectionParametersForRequest(mockConnection, request);
        assertEquals("PATCH", mockConnection.getRequestMethod());
        assertTrue(mockConnection.getDoOutput());
    }
}
