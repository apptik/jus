package io.apptik.comm.jus.perf;


import com.android.volley.toolbox.BasicNetwork;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.perf.mock.MockVolleyHttpStack;
import io.apptik.comm.jus.perf.mock.VolleyNoCache;


public class VolleyRequestPipeline implements RequestPipeline {

    private com.android.volley.RequestQueue requestQueue;

    @Override
    public Request<NetworkResponse> addRequest(Request<NetworkResponse> request) {
        throw new RuntimeException("NOOP");
    }

    @Override
    public com.android.volley.Request<com.android.volley.NetworkResponse> addRequest
            (com.android.volley.Request<com.android.volley.NetworkResponse> request) {
        return requestQueue.add(request);

    }

    @Override
    public RequestPipeline prepare(States.GenericState state) {
        NetworkResponse resp =  Util.newResponse(state);
        HttpResponse vResp = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1),
                resp.statusCode, "OK");
        vResp.setEntity(new ByteArrayEntity(resp.data));
        for(String h:resp.headers.names()) {
            vResp.setHeader(h,resp.headers.get(h));
        }
        MockVolleyHttpStack stack = new MockVolleyHttpStack();
        stack.setResponseToReturn(vResp);
        this.requestQueue = new com.android.volley.RequestQueue(
                new VolleyNoCache(),
                new BasicNetwork(stack),
                state.concurrencyLevel
        );
        requestQueue.start();
        return this;
    }

    @Override
    public void shutdown() {
        requestQueue.stop();
    }
}
