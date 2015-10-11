package io.apptik.comm.jus;

import org.json.JSONObject;
import org.junit.Test;

import io.apptik.comm.jus.converter.JSONConverter;
import io.apptik.comm.jus.http.HTTP;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvertersTest {

    @Test
    public void jsonObjectConverterTest() throws Exception {
        final JSONObject val = new JSONObject().put("key1", 123);

        JSONConverter.Factory factory = new JSONConverter.Factory();
        Converter<JSONObject, NetworkRequest> converter1 =
                (Converter<JSONObject, NetworkRequest>) factory.toRequest(JSONObject.class, null);
       // System.out.println("val: " + val);
        NetworkRequest request = converter1.convert(val);
        NetworkResponse response = new NetworkResponse.Builder()
                .setBody(request.data)
                .setHeader(HTTP.CONTENT_TYPE, request.contentType.toString())
                .build();
        Converter<NetworkResponse, JSONObject> converter2 =
                (Converter<NetworkResponse, JSONObject>) factory.fromResponse(JSONObject.class, null);

        JSONObject val2 = converter2.convert(response);
        //System.out.println("val2: " + val2);
        assertThat(val2).isEqualTo(val);
    }

}
