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

package io.apptik.comm.jus.examples.api;


import io.apptik.comm.jus.RequestListener;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.request.JSONObjectRequest;
import io.apptik.comm.jus.request.StringRequest;


public class Requests {

    private Requests() {

    }

    // http://validate.jsontest.com/?json={'key':'value'}
    // http://echo.jsontest.com/key/value/one/two

    public static JSONObjectRequest getStationsRequest() {
        return new JSONObjectRequest(Request.Method.GET, "https://irail.be/stations/NMBS?q=Brussels");
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