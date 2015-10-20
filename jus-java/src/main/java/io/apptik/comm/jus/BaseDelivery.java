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


public abstract class BaseDelivery implements ResponseDelivery {
    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        postResponse(request, response, null);
    }

    @Override
    public void postError(Request request, JusError error) {
        postError(request, error, null);
    }

    @Override
    public void postResponse(Request request, Response response, Runnable runnable) {
        request.markDelivered();
        request.addMarker(Request.EVENT_POST_RESPONSE, response);
        request.response = response;
        addMarkersAndDeliver(request, response, runnable);
    }

    @Override
    public void postError(Request request, JusError error, Runnable runnable) {
        request.addMarker(Request.EVENT_POST_ERROR, error);
        Response<?> response = Response.error(error);
        request.response = response;
        addMarkersAndDeliver(request, response, runnable);
    }

    private void addMarkersAndDeliver(Request<?> request, Response<?> response, Runnable runnable) {
        // If this request has canceled, finish it and don't deliver.
        if (request.isCanceled()) {
            request.finish(Request.EVENT_CANCELED_AT_DELIVERY);
        } else {
            if(response.error!=null) {
                request.addMarker(Request.EVENT_DELIVER_RESPONSE, response.result, response.intermediate);
                doDeliver(request, response, runnable);
            } else {
                request.addMarker(Request.EVENT_DELIVER_ERROR, response.error);
                doDeliver(request, response, runnable);
            }
            // If this is an intermediate response, add a marker, otherwise we're done
            // and the request can be finished.
            if (response.intermediate) {
                request.addMarker(Request.EVENT_INTERMEDIATE_RESPONSE);
            } else {
                request.finish(Request.EVENT_DONE);
            }
        }
    }

    abstract void doDeliver(Request request, Response response, Runnable runnable);
}
