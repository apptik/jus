package io.apptik.comm.jus.apachehttp;


import org.apache.http.Header;

import io.apptik.comm.jus.http.Headers;

public class JusApache {
    private JusApache() {
    }

    public static Headers jusHeaders(Header[] headers) {
        Headers.Builder builder =  new Headers.Builder();
        for(Header header:headers) {
            builder.add(header.getName(), header.getValue());
        }
        return builder.build();
    }
}
