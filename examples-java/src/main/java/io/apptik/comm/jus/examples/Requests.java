package io.apptik.comm.jus.examples;


import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.request.StringRequest;

public class Requests {

    private Requests() {

    }

    // http://validate.jsontest.com/?json={'key':'value'}
    // http://echo.jsontest.com/key/value/one/two
    public static Request<Void, String> getWeatherRequest(String q, Listener.ResponseListener<String> listener,
                                                          Listener.ErrorListener errorListener) {

        Request<Void, String> res = null;

        if (q == null) {
            q = "London,uk";
        }

        res = new StringRequest("http://api.openweathermap.org/data/2.5/weather?q=" + q, listener, errorListener);

        return res;
    }

    public static Request<Void, String> getDummyRequest(String key, String val, Listener.ResponseListener<String> listener,
                                                        Listener.ErrorListener errorListener) {
        Request<Void, String> res =
                new StringRequest("http://validate.jsontest.com/?json={'" + key + "':'" + val + "'}", listener, errorListener);

        return res;

    }

}