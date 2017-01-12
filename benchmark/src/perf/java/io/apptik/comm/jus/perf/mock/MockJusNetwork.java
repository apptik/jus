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

package io.apptik.comm.jus.perf.mock;

import java.util.concurrent.atomic.AtomicInteger;

import io.apptik.comm.jus.Network;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.error.JusError;
import io.apptik.comm.jus.error.ServerError;
import io.apptik.comm.jus.http.Headers;

public class MockJusNetwork implements Network {
    public final static int ALWAYS_THROW_EXCEPTIONS = -1;

    private int mNumExceptionsToThrow = 0;
    private byte[] mDataToReturn = null;
    private AtomicInteger requestCnt = new AtomicInteger();
    private int slowness = 0;
    private NetworkResponse mResponseToReturn;


    /**
     * @param numExceptionsToThrow number of times to throw an exception or
     *                             {@link #ALWAYS_THROW_EXCEPTIONS}
     */
    public void setNumExceptionsToThrow(int numExceptionsToThrow) {
        mNumExceptionsToThrow = numExceptionsToThrow;
    }

    public void setDataToReturn(byte[] data) {
        mDataToReturn = data;
    }

    public MockJusNetwork setSlowness(int slowness) {
        this.slowness = slowness;
        return this;
    }

    public Request<?> requestHandled = null;

    public int getRequestCnt() {
        return requestCnt.get();
    }

    public void setResponseToReturn(NetworkResponse response) {
        mResponseToReturn = response;
    }

    @Override
    public NetworkResponse performRequest(final Request<?> request) throws JusError {
        try {
            Thread.sleep(slowness);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (mNumExceptionsToThrow > 0 || mNumExceptionsToThrow == ALWAYS_THROW_EXCEPTIONS) {
            if (mNumExceptionsToThrow != ALWAYS_THROW_EXCEPTIONS) {
                mNumExceptionsToThrow--;
            }
            throw new ServerError(null);
        }

        requestHandled = request;
        requestCnt.incrementAndGet();
        if(mResponseToReturn!=null) {
            return mResponseToReturn;
        }
        return new NetworkResponse(200, mDataToReturn, new Headers.Builder().build(), 0);
    }

}
