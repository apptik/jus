package io.apptik.comm.jus.request;


import org.djodjo.json.JsonArray;
import org.djodjo.json.JsonElement;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JJsonArrayResponseBodyConverter;
import io.apptik.comm.jus.converter.JJsonRequestBodyConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class JsonArrayRequest extends Request<JsonArray> {

    public JsonArrayRequest(String method, HttpUrl url) {
        super(method, url, new JJsonArrayResponseBodyConverter());
    }

    public JsonArrayRequest(String method, String url) {
        super(method, url, new JJsonArrayResponseBodyConverter());
    }
    
    public JsonArrayRequest setObjectRequest(JsonElement objectRequest) {
        super.setObjectRequest(objectRequest, new JJsonRequestBodyConverter());
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json; charset=UTF-8")
                .build());
        return this;
    }

}
