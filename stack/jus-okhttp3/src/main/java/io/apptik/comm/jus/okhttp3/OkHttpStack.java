/*
 * Copyright (C) 2016 AppTik Project
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

package io.apptik.comm.jus.okhttp3;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import io.apptik.comm.jus.NetworkDispatcher;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.stack.AbstractHttpStack;
import io.apptik.comm.jus.stack.HttpStack;
import io.apptik.comm.jus.toolbox.ByteArrayPool;
import io.apptik.comm.jus.toolbox.PoolingByteArrayOutputStream;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okio.BufferedSource;

/**
 * An {@link HttpStack} based on {@link okhttp3.OkHttpClient}.
 */

public class OkHttpStack extends AbstractHttpStack {
    private final OkHttpClient client;

    public OkHttpStack() {
        this(new OkHttpClient());
    }

    public OkHttpStack(OkHttpClient client) {
        if (client == null) {
            throw new NullPointerException("Client must not be null.");
        }
        this.client = client;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request, Headers
            additionalHeaders, ByteArrayPool byteArrayPool) throws IOException {

        //clone to be able to set timeouts per call
        OkHttpClient client = this.client.newBuilder()
        .connectTimeout(request.getRetryPolicy().getCurrentConnectTimeout(), TimeUnit
                .MILLISECONDS)
        .readTimeout(request.getRetryPolicy().getCurrentReadTimeout(), TimeUnit
                .MILLISECONDS).build();

        okhttp3.Request okRequest = new okhttp3.Request.Builder()
                .url(request.getUrlString())
                .headers(JusOk.okHeaders(request.getHeaders(), additionalHeaders))
                .tag(request.getTag())
                .method(request.getMethod(), JusOk.okBody(request.getNetworkRequest()))
                .build();

        long requestStart = System.nanoTime();

        Response response = client.newCall(okRequest).execute();

        byte[] data = null;
        if (NetworkDispatcher.hasResponseBody(request.getMethod(), response.code())) {
            data = getContentBytes(response.body().source(),
                    byteArrayPool, (int) response.body().contentLength());
        } else {
            // Add 0 byte response as a way of honestly representing a
            // no-content request.
            data = new byte[0];
        }
        return new NetworkResponse.Builder()
                .setHeaders(JusOk.jusHeaders(response.headers()))
                .setStatusCode(response.code())
                .setBody(data)
                .setNetworkTimeNs(System.nanoTime() - requestStart)
                .build();
    }

    protected final byte[] getContentBytes(BufferedSource bufferedSource, ByteArrayPool
            byteArrayPool, int contentLen) throws IOException {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(byteArrayPool, contentLen);
        byte[] buffer = null;

        try {
            if (bufferedSource == null) {
                return new byte[0];
            }
            buffer = byteArrayPool.getBuf(1024);
            int count;
            try {
                while ((count = bufferedSource.read(buffer)) != -1) {
                    bytes.write(buffer, 0, count);
                }
            } catch (IOException ex) {
                //we will get this anyway keep on so we can have whatever we got from the body
                //except timeout error
                if(ex instanceof SocketTimeoutException) {
                    throw ex;
                }
            }
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources
                bufferedSource.close();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                //todo add queue markers
            }
            byteArrayPool.returnBuf(buffer);
            bytes.close();
        }
    }

}
