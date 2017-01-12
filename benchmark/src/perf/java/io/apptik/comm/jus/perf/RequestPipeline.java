package io.apptik.comm.jus.perf;

import io.apptik.comm.jus.NetworkResponse;
import io.apptik.comm.jus.Request;

public interface RequestPipeline {


    Request<NetworkResponse> addRequest(Request<NetworkResponse> request);

    com.android.volley.Request<com.android.volley.NetworkResponse> addRequest
            (com.android.volley.Request<com.android.volley.NetworkResponse> request);

    RequestPipeline prepare(States.GenericState state);

    void shutdown();

    class Util {
        private Util(){}
        public static NetworkResponse newResponse(States.GenericState state) {
            byte[] bytes = new byte[state.bodyByteCount];
            state.random.nextBytes(bytes);
            NetworkResponse.Builder respBuilder = new NetworkResponse.Builder();
            respBuilder.setStatusCode(200);
            respBuilder.setBody(bytes);
            for (int i = 0; i < state.headerCount; i++) {
                respBuilder.addHeader(randomString(12, state), randomString(20, state));
            }

            return respBuilder.build();
        }

        public static String randomString(int length, States.GenericState state) {
            String alphabet = "-abcdefghijklmnopqrstuvwxyz";
            char[] result = new char[length];
            for (int i = 0; i < length; i++) {
                result[i] = alphabet.charAt(state.random.nextInt(alphabet.length()));
            }
            return new String(result);
        }
    }
}
