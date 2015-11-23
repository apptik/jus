package io.apptik.comm.jus.okhttp;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;

import io.apptik.comm.jus.NetworkDispatcher;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.AuthFailureError;
import io.apptik.comm.jus.http.Headers;
import io.apptik.comm.jus.stack.AbstractHttpStack;
import io.apptik.comm.jus.stack.HttpStack;
import io.apptik.comm.jus.toolbox.ByteArrayPool;
import io.apptik.comm.jus.toolbox.PoolingByteArrayOutputStream;
import okio.BufferedSource;

/**
 * An {@link HttpStack} based on {@link com.squareup.okhttp.OkHttpClient}.
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
            additionalHeaders, ByteArrayPool byteArrayPool) throws IOException, AuthFailureError {

        OkHttpClient client = this.client.clone();
        client.setConnectTimeout(request.getRetryPolicy().getCurrentConnectTimeout(), TimeUnit
                .MILLISECONDS);
        client.setReadTimeout(request.getRetryPolicy().getCurrentReadTimeout(), TimeUnit
                .MILLISECONDS);
        com.squareup.okhttp.Request okRequest = new com.squareup.okhttp.Request.Builder()
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
            } catch (ProtocolException ex) {
                //we will get this anyway keep on so we can have whatever we got from the body
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
