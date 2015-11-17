package io.apptik.comm.jus.stack;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import io.apptik.comm.jus.toolbox.ByteArrayPool;
import io.apptik.comm.jus.toolbox.PoolingByteArrayOutputStream;

public abstract class AbstractHttpStack implements HttpStack {

    /**
     * Reads the contents of HttpEntity into a byte[].
     */
    protected final byte[] getContentBytes(HttpURLConnection connection, ByteArrayPool
            byteArrayPool)
            throws IOException {
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        return getContentBytes(inputStream, byteArrayPool, connection.getContentLength());
    }

    protected final byte[] getContentBytes(InputStream inputStream, ByteArrayPool
            byteArrayPool, int contentLen) throws IOException {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(byteArrayPool, contentLen);
        byte[] buffer = null;

        try {

            if (inputStream == null) {
                return new byte[0];
            }
            buffer = byteArrayPool.getBuf(1024);
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            try {
                // Close the InputStream and release the resources
                inputStream.close();
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
