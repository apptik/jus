package io.apptik.comm.jus.perf;

import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;
import io.apptik.comm.jus.perf.mock.MockVolleyRequest;

public class States {

    private States() {
    }

    @State(Scope.Thread)
    public static class GenericState {
        RequestPipeline requestPipeline;
        List<Request<NetworkResponse>> jusRequests = new ArrayList<>();
        List<MockVolleyRequest> volleyRequests = new ArrayList<>();

        @Setup(Level.Iteration)
        public void setup() {
            requestPipeline = pipeline.create().prepare(this);

        }

        @TearDown(Level.Iteration)
        public void tearDown() {
            requestPipeline.shutdown();
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            for (int i = 0; i < targetBacklog; i++) {
                if (this.pipeline == Pipeline.JusDef) {
                    jusRequests.add(getJusRequest("http://127.0.0.1/", Request.Method.GET));
                } else if (this.pipeline == Pipeline.VolleyDef) {
                    volleyRequests.add(getVolleyRequest("http://127.0.0.1/", Request.Method.GET));
                }
            }
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            jusRequests.clear();
            volleyRequests.clear();
        }

        @Param
        Pipeline pipeline;

        final Random random = new Random(0);

        /**
         * How many concurrent jusRequests to execute.
         */
        @Param({"1", "2", "3", "4", "8", "10", "12"})
        int concurrencyLevel;

        //TODO investigate using @OperationsPerInvocation(targetBacklog)
        /**
         * How many jusRequests to enqueue to await threads to execute them.
         */
        @Param({"1", "10", "100", "1000"})
        int targetBacklog;

        /**
         * The size of the HTTP response body, in uncompressed bytes.
         */
        @Param({"128", "1048576"})
        int bodyByteCount;

        /**
         * How many additional headers were included, beyond the built-in ones.
         */
        @Param({"1", "20"})
        int headerCount;

    }

    private static Request<NetworkResponse> getJusRequest(String url, String method) {
        return new Request<>(method, url, NetworkResponse.class);
    }

    private static MockVolleyRequest getVolleyRequest(String url, String method) {
        return new MockVolleyRequest(url,method);
    }
}
