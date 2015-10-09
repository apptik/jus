package io.apptik.comm.jus;

import org.junit.Test;

import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.http.HTTP;

import static org.assertj.core.api.Assertions.assertThat;

public class ConvertersTest {

    @Test
    public void stringConverterTest() throws Exception {
        String val = "value";
        Converters.StringRequestConverter converter1 = new Converters.StringRequestConverter();
        NetworkRequest request = converter1.convert(val);
        NetworkResponse response = new NetworkResponse.Builder()
                .setBody(request.data)
                .setHeader(HTTP.CONTENT_TYPE, request.contentType.toString())
                .build();
        Converters.StringResponseConverter converter2 =  new Converters.StringResponseConverter();
        String val2 = converter2.convert(response);
        assertThat(val2).isEqualTo(val);
    }

}
