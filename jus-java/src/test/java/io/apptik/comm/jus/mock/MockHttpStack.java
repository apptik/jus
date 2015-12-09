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

package io.apptik.comm.jus.mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.AuthError;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.stack.HttpStack;
import io.apptik.comm.jus.toolbox.ByteArrayPool;

public class MockHttpStack implements HttpStack {

    private NetworkResponse mResponseToReturn;

    private String mLastUrl;

    private Map<String, String> mLastHeaders;

    private byte[] mLastPostBody;

    public String getLastUrl() {
        return mLastUrl;
    }

    public Map<String, String> getLastHeaders() {
        return mLastHeaders;
    }

    public byte[] getLastPostBody() {
        return mLastPostBody;
    }

    public void setResponseToReturn(NetworkResponse response) {
        mResponseToReturn = response;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request, Headers additionalHeaders, ByteArrayPool
            byteArrayPool)
            throws IOException, AuthError {
        mLastUrl = request.getUrl().toString();
        mLastHeaders = new HashMap<String, String>();
        if (request.getHeaders() != null) {
            mLastHeaders.putAll(request.getHeaders().toMap());
        }
        if (additionalHeaders != null) {
            mLastHeaders.putAll(additionalHeaders.toMap());
        }
            mLastPostBody = request.getBody();

        return mResponseToReturn;
    }
}
