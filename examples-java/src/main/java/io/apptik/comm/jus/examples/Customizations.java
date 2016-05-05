package io.apptik.comm.jus.examples;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.apptik.comm.jus.Cache;
import io.apptik.comm.jus.Jus;
import io.apptik.comm.jus.JusLog;
import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.Response;
import io.apptik.comm.jus.converter.Converters;
import io.apptik.comm.jus.http.HttpUrl;

import static java.lang.System.out;

public class Customizations {
    public static void main(String[] args) {
        RequestQueue queue = Jus.newRequestQueue(new File("."));
        Set<String> opts = new HashSet<>();
        if (args != null) {
            Collections.addAll(opts, args);
        }

        if (opts.contains("cache")) {
            JusLog.MarkerLog.on();
            queue.add(new Request<String>(
                    Request.Method.GET,
                    HttpUrl.parse("https://geek.ng/wp-json/wp/v2/posts?page=1"),
                    new Converters.StringResponseConverter()) {
                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    Response<String> resp = super.parseNetworkResponse(response);
                    if (!resp.isSuccess()) {
                        return resp;
                    }
                    long now = System.currentTimeMillis();
                    Cache.Entry entry = resp.cacheEntry;
                    if (entry == null) {

                        entry = new Cache.Entry();
                        entry.data = response.data;
                        entry.responseHeaders = response.headers;
                        entry.ttl = now + 60 * 60 * 1000;  //keeps cache for 1 hr
                    }
                    entry.softTtl = 0; // will always refresh

                    return Response.success(resp.result, entry);
                }
            }
                    .addResponseListener((r) -> out.println("RESPONSE: " + r))
                    .addErrorListener((e) -> out.println("ERROR: " + e)));
        }
        queue.stopWhenDone();
    }
}
