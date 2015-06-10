package org.djodjo.comm.jus.examples.api;


import org.djodjo.comm.jus.Request;
import org.djodjo.comm.jus.Response;
import org.djodjo.comm.jus.request.StringRequest;

public class Requests {

    private Requests() {

    }

    // http://validate.jsontest.com/?json={'key':'value'}
    // http://echo.jsontest.com/key/value/one/two

    public static Request<String> addOsmRequest() {
        return null;
    }

    public static Request<String> getBeerRequest(String q, Response.Listener<String> listener,
                                          Response.ErrorListener errorListener) {
        Request<String> res;

        final String userString = "c6266a50b6603fe87d681ef34fe11e3e";
        final String baseUrl = "http://beermapping.com/webservice/";
        //get general info
        final String locquery = "locquery";

        //get lat/lon
        final String locmap = "locmap";

        //get ratings
        final String locscore = "locscore";

        //get pics
        final String locimage = "locimage";

        res = new StringRequest(baseUrl + locquery
                + "/" + userString + "/" + q, listener, errorListener);


        return res;

    }

    public static Request<String> getWeatherRequest(String q, Response.Listener<String> listener,
                                             Response.ErrorListener errorListener) {

        Request<String> res = null;

        if (q == null) {
            q = "London,uk";
        }

        res = new StringRequest("http://api.openweathermap.org/data/2.5/weather?q=" + q, listener, errorListener);

        return res;
    }

    public static Request<String> getDummyRequest(String key, String val, Response.Listener<String> listener,
                                           Response.ErrorListener errorListener) {
        Request<String> res =
                new StringRequest("http://validate.jsontest.com/?json={'" + key + "':'" + val + "'}", listener, errorListener);

        return res;

    }

}