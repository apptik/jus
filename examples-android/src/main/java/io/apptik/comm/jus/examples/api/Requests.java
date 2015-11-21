package io.apptik.comm.jus.examples.api;


import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.request.JsonObjectRequest;
import io.apptik.comm.jus.request.StringRequest;


public class Requests {

    private Requests() {

    }

    // http://validate.jsontest.com/?json={'key':'value'}
    // http://echo.jsontest.com/key/value/one/two

    public static JsonObjectRequest getStationsRequest() {
        return new JsonObjectRequest(Request.Method.GET, "https://irail.be/stations/NMBS?q=Brussels");
    }


    public static Request<String> getDummyRequest(String key, String val, RequestListener.ResponseListener<String> listener,
                                           RequestListener.ErrorListener errorListener) {
        Request<String> res =
                new StringRequest(Request.Method.GET, "http://validate.jsontest.com/?json={'" + key + "':'" + val + "'}")
                .addResponseListener(listener)
                .addErrorListener(errorListener);

        return res;

    }

}