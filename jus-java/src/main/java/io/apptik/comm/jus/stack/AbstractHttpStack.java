/*
 * Copyright (C) 2015 AppTik Project
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

import io.apptik.comm.jus.toolbox.ByteArrayPool;
import io.apptik.comm.jus.toolbox.PoolingByteArrayOutputStream;


/**
 * A base abstract class to extend when implementing new Http Stack.
 */
public abstract class AbstractHttpStack implements HttpStack {

    /**
     * Reads the contents of HttpEntity into a byte[].
     */
    public static final byte[] getContentBytes(HttpURLConnection connection, ByteArrayPool
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

    public static final byte[] getContentBytes(InputStream inputStream, ByteArrayPool
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
            try {
                while ((count = inputStream.read(buffer)) != -1) {
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
