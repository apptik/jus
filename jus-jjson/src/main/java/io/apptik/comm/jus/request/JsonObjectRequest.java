package io.apptik.comm.jus.request;


import org.djodjo.json.JsonElement;
import org.djodjo.json.JsonObject;

import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.converter.JJsonObjectResponseBodyConverter;
import io.apptik.comm.jus.converter.JJsonRequestBodyConverter;
import io.apptik.comm.jus.http.HttpUrl;

public class JsonObjectRequest extends Request<JsonObject> {

    public JsonObjectRequest(String method, HttpUrl url) {
        super(method, url, new JJsonObjectResponseBodyConverter());
    }

    public JsonObjectRequest(String method, String url) {
        super(method, url, new JJsonObjectResponseBodyConverter());
    }

    public JsonObjectRequest setObjectRequest(JsonElement objectRequest) {
        super.setObjectRequest(objectRequest, new JJsonRequestBodyConverter());
        setNetworkRequest(NetworkRequest.Builder.from(getNetworkRequest())
                .setHeader("Accept", "application/json; charset=UTF-8")
                .build());
        return this;
    }

}
