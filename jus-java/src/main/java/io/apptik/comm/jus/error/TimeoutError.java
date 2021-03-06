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

import java.net.SocketTimeoutException;

/**
 * Indicates that the connection or the socket timed out.
 */
@SuppressWarnings("serial")
public class TimeoutError extends NetworkError {

    public TimeoutError() {
        super();
    }

    public TimeoutError(String msg) {
        super(msg);
    }

    public TimeoutError(String msg, SocketTimeoutException e) {
        super(msg, e);
    }

    @Override
    public String toString() {
        return "TimeoutError{} " + super.toString();
    }
}
