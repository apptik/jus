package io.apptik.comm.jus.perf;


import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.RequestQueue;
import io.apptik.comm.jus.perf.mock.MockJusHttpStack;
import io.apptik.comm.jus.toolbox.HttpNetwork;
import io.apptik.comm.jus.toolbox.NoCache;

public class JusRequestPipeline implements RequestPipeline {

    private  RequestQueue requestQueue;

    @Override
    public Request<NetworkResponse> addRequest(Request<NetworkResponse> request) {
            return requestQueue.add(request);
    }

    @Override
    public com.android.volley.Request<com.android.volley.NetworkResponse> addRequest
            (com.android.volley.Request<com.android.volley.NetworkResponse> request) {
        throw new RuntimeException("NOOP");
    }

    @Override
    public RequestPipeline prepare(States.GenericState state) {
        NetworkResponse resp =  Util.newResponse(state);
        MockJusHttpStack stack= new MockJusHttpStack();
        stack.setResponseToReturn(resp);
        //MockNetwork httpNetwork = new MockNetwork();
        //httpNetwork.setResponseToReturn(resp);
        //httpNetwork.setSlowness(33);
        this.requestQueue =  new RequestQueue(
                new NoCache(),
                new HttpNetwork(stack),
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
