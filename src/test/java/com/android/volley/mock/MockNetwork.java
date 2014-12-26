/*
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

package org.djodjo.comm.jus.mock;

import org.djodjo.comm.jus.JusError;
import org.djodjo.comm.jus.Network;
import org.djodjo.comm.jus.NetworkResponse;
import org.djodjo.comm.jus.Request;
import org.djodjo.comm.jus.ServerError;

public class MockNetwork implements Network {
    public final static int ALWAYS_THROW_EXCEPTIONS = -1;

    private int mNumExceptionsToThrow = 0;
    private byte[] mDataToReturn = null;

    /**
     * @param numExceptionsToThrow number of times to throw an exception or
     * {@link #ALWAYS_THROW_EXCEPTIONS}
     */
    public void setNumExceptionsToThrow(int numExceptionsToThrow) {
        mNumExceptionsToThrow = numExceptionsToThrow;
    }

    public void setDataToReturn(byte[] data) {
        mDataToReturn = data;
    }

    public Request<?> requestHandled = null;

    @Override
    public NetworkResponse performRequest(Request<?> request) throws JusError {
        if (mNumExceptionsToThrow > 0 || mNumExceptionsToThrow == ALWAYS_THROW_EXCEPTIONS) {
            if (mNumExceptionsToThrow != ALWAYS_THROW_EXCEPTIONS) {
                mNumExceptionsToThrow--;
            }
            throw new ServerError();
        }

        requestHandled = request;
        return new NetworkResponse(mDataToReturn);
    }

}
