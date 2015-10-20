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

package io.apptik.comm.jus;


import io.apptik.comm.jus.error.JusError;

public class Listener {
    /** Callback interface for delivering parsed responses. */
    public interface ResponseListener<T> {
        /** Called when a response is received. */
        void onResponse(T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        void onErrorResponse(JusError error);
    }

    /** Callback interface for delivering markers. */
    public interface MarkerListener {
        /** Called when a marker is set. */
        void onMarker(JusLog.MarkerLog.Marker marker, Object... args);
    }


}
