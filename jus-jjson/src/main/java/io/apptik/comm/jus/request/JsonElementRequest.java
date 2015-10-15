package io.apptik.comm.jus.request;


import org.djodjo.json.JsonElement;

import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JJsonRequestBodyConverter;
import io.apptik.comm.jus.converter.JJsonResponseBodyConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class JsonElementRequest extends Request<JsonElement> {

    public JsonElementRequest(String method, HttpUrl url) {
        super(method, url, new JJsonResponseBodyConverter());
    }

    public JsonElementRequest(String method, String url) {
        super(method, url, new JJsonResponseBodyConverter());
    }


    public Request<JsonElement> setObjectRequest(JsonElement objectRequest) {
        return super.setObjectRequest(objectRequest, new JJsonRequestBodyConverter());
    }

}
