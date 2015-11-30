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

package io.apptik.comm.jus.stack;

import java.io.IOException;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.AuthFailureError;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.toolbox.ByteArrayPool;

/**
 * An HTTP stack abstraction.
 * When implementing a new Http Stack it is recommended to extend {@link AbstractHttpStack}
 */
public interface HttpStack {
    /**
     * Performs an HTTP request with the given parameters.
     *
     * <p>A GET request is sent if request.getPostBody() == null. A POST request is sent otherwise,
     * and the Content-Type header is set to request.getPostBodyContentType().</p>
     *
     * @param request the request to perform
     * @param additionalHeaders additional headers to be sent together with
     *         {@link Request#getHeadersMap()}
     * @return the HTTP response
     */
    NetworkResponse performRequest(Request<?> request,
                                   Headers additionalHeaders,
                                   ByteArrayPool byteArrayPool)
        throws IOException, AuthFailureError;

}
