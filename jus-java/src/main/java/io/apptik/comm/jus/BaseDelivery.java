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
        request.addMarker(Request.EVENT_POST_RESPONSE);
        request.response = response;
        addMarkersAndContinue(request, response);
        doDeliver(request, response, runnable);
    }

    @Override
    public void postError(Request request, JusError error, Runnable runnable) {
        request.addMarker(Request.EVENT_POST_ERROR);
        Response<?> response = Response.error(error);
        request.response = response;
        addMarkersAndContinue(request, response);
        doDeliver(request, response, runnable);
    }

    private void addMarkersAndContinue(Request<?> request, Response<?> response) {
        // If this request has canceled, finish it and don't deliver.
        if (request.isCanceled()) {
            request.finish(Request.EVENT_CANCELED_AT_DELIVERY);
        } else {
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
