package io.apptik.comm.jus.examples;


import io.apptik.comm.jus.Listener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.request.StringRequest;

public class Requests {

    private Requests() {

    }

    // http://validate.jsontest.com/?json={'key':'value'}
    // http://echo.jsontest.com/key/value/one/two
    public static Request<String> getWeatherRequest(String q) {

        Request<String> res = null;

        if (q == null) {
            q = "London,uk";
        }

        res = new StringRequest(Request.Method.GET, "http://api.openweathermap.org/data/2.5/weather?q=" + q);

        return res;
    }

    public static Request<String> getDummyRequest(String key, String val, Listener.ResponseListener<String> listener,
                                                        Listener.ErrorListener errorListener) {
        Request<String> res =
                new StringRequest(Request.Method.GET, "http://validate.jsontest.com/?json={'" + key + "':'" + val + "'}");

        return res;

    }

}