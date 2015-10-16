
package io.apptik.comm.jus.converter;

import org.djodjo.json.JsonElement;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import io.apptik.comm.jus.Converter;
import io.apptik.comm.jus.NetworkRequest;
import io.apptik.comm.jus.http.MediaType;
import io.apptik.comm.jus.toolbox.Utils;

public final class JJsonRequestBodyConverter implements Converter<JsonElement, NetworkRequest> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

    public JJsonRequestBodyConverter() {
    }

    @Override
    public NetworkRequest convert(JsonElement value) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(baos));
        value.writeTo(writer);
        Utils.closeQuietly(writer);
        byte[] bytes = baos.toByteArray();
        return new NetworkRequest.Builder().setContentType(MEDIA_TYPE).setBody(bytes).build();
    }
}
