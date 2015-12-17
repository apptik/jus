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
import io.apptik.comm.jus.toolbox.Utils;

public interface RequestListener {
    /**
     * Callback interface for delivering parsed responses.
     */
    interface ResponseListener<T> {
        /**
         * Called when a response is received.
         */
        void onResponse(T response);
    }

    /**
     * Callback interface for delivering error responses.
     */
    interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        void onError(JusError error);
    }

    /**
     * Callback interface for delivering markers.
     */
    interface MarkerListener {
        /**
         * Called when a marker is set.
         */
        void onMarker(Marker marker, Object... args);
    }

    interface QListenerFactory {
        ResponseListener getResponseListener(Request<?> request);

        ErrorListener getErrorListener(Request<?> request);

        MarkerListener getMarkerListener(Request<?> request);
    }

    abstract class QListener<T> {
        protected final Request<T> request;

        protected QListener(Request<T> request) {
            Utils.checkNotNull(request, "request==null");
            this.request = request;
        }
    }

    abstract class QResponseListener<T> extends QListener implements ResponseListener<T> {
        protected QResponseListener(Request<T> request) {
            super(request);
        }
    }

    abstract class QErrorListener extends QListener implements ErrorListener {
        protected QErrorListener(Request<?> request) {
            super(request);
        }
    }

    abstract class QMarkerListener extends QListener implements MarkerListener {
        protected QMarkerListener(Request<?> request) {
            super(request);
        }
    }

    class SimpleQListenerFactory implements QListenerFactory {
        @Override
        public ResponseListener getResponseListener(Request<?> request) {
            return null;
        }

        @Override
        public ErrorListener getErrorListener(Request<?> request) {
            return null;
        }

        @Override
        public MarkerListener getMarkerListener(Request<?> request) {
            return null;
        }
    }

    abstract class FilteredQListenerFactory implements QListenerFactory {
        protected final RequestQueue.RequestFilter filter;

        protected FilteredQListenerFactory(RequestQueue.RequestFilter filter) {
            this.filter = filter;
        }

        protected abstract <T> ResponseListener<T> getFilteredResponseListener(Request<T> request);

        protected abstract ErrorListener getFilteredErrorListener(Request<?> request);

        protected abstract MarkerListener getFilteredMarkerListener(Request<?> request);

        @Override
        public final ResponseListener getResponseListener(Request<?> request) {
            if (filter == null || filter.apply(request)) {
                return getFilteredResponseListener(request);
            }
            return null;
        }

        @Override
        public final ErrorListener getErrorListener(Request<?> request) {
            if (filter == null || filter.apply(request)) {
                return getFilteredErrorListener(request);
            }
            return null;
        }

        @Override
        public final MarkerListener getMarkerListener(Request<?> request) {
            if (filter == null || filter.apply(request)) {
                return getFilteredMarkerListener(request);
            }
            return null;
        }
    }

    class SimpleFilteredQListenerFactory extends FilteredQListenerFactory {

        public SimpleFilteredQListenerFactory() {
            super(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }

        public SimpleFilteredQListenerFactory(RequestQueue.RequestFilter filter) {
            super(filter);
        }

        @Override
        protected <T> ResponseListener<T> getFilteredResponseListener(Request<T> request) {
            return null;
        }

        @Override
        protected ErrorListener getFilteredErrorListener(Request<?> request) {
            return null;
        }

        @Override
        protected MarkerListener getFilteredMarkerListener(Request<?> request) {
            return null;
        }
    }
}
