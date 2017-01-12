package io.apptik.comm.jus.perf;


import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.perf.mock.LatchedJusCallback;
import io.apptik.comm.jus.perf.mock.LatchedVolleyCallback;
import io.apptik.comm.jus.perf.mock.MockVolleyRequest;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class RoundTripPerf {

    /**
     * This is a dummy benchmark showing the time it takes for a complete async request-response
     * round trip, considering no or negligible network latency
     *
     * @param state
     * @return
     * @throws Exception
     */
    @org.openjdk.jmh.annotations.Benchmark
    public CountDownLatch get(States.GenericState state) throws Exception {
        //JusLog.ResponseLog.on();
        //JusLog.MarkerLog.on();
        CountDownLatch latch = new CountDownLatch(state.targetBacklog);
        if(state.pipeline==Pipeline.JusDef) {
            Request<NetworkResponse> request;
            for (int i = 0; i < state.targetBacklog; i++) {
                request = state.jusRequests.get(i);
                request.addResponseListener(new LatchedJusCallback(latch));
                state.requestPipeline.addRequest(request);
            }
        } else if(state.pipeline==Pipeline.VolleyDef){
            MockVolleyRequest request;
            for (int i = 0; i < state.targetBacklog; i++) {
                request = state.volleyRequests.get(i);
                request.setListener(new LatchedVolleyCallback(latch));
                state.requestPipeline.addRequest(request);
            }
        }
        latch.await();
        return latch;
    }




}
