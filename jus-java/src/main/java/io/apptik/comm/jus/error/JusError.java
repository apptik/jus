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

package io.apptik.comm.jus.error;

import java.util.Arrays;

import io.apptik.comm.jus.NetworkResponse;

/**
 * Exception style class encapsulating Jus errors
 */
@SuppressWarnings("serial")
public class JusError extends Exception {
    public final NetworkResponse networkResponse;
    private long networkTimeMs;

    public JusError() {
        networkResponse = null;
    }

    public JusError( NetworkResponse response) {
        networkResponse = response;
    }
    public JusError( NetworkResponse response, Throwable reason) {
        super(reason);
        networkResponse = response;
    }

    public JusError(String exceptionMessage) {
       super(exceptionMessage);
        networkResponse = null;
    }

    public JusError(String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        networkResponse = null;
    }

    public JusError(Throwable cause) {
        super(cause);
        networkResponse = null;
    }

    public JusError(String exceptionMessage, NetworkResponse response) {
        super(exceptionMessage);
        networkResponse = response;
    }

    /* package */
    public void setNetworkTimeNs(long networkTimeMs) {
       this.networkTimeMs = networkTimeMs;
    }

    public long getNetworkTimeMs() {
       return networkTimeMs;
    }

    @Override
    public String toString() {
        return "JusError{" +
                "networkResponse=" + networkResponse +
                ", networkTimeMs=" + networkTimeMs +
                ", \nmessage=" + getLocalizedMessage() +
                ", \nstacktrace= "+ Arrays.toString(getStackTrace()) + "\n\tCause: " + Arrays
                .toString
                (getCause().getStackTrace()) + "} ";
    }
}
