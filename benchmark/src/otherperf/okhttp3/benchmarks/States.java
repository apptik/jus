package okhttp3.benchmarks;


import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import okhttp3.Protocol;

public class States {
    private States() {
    }

    @State(Scope.Thread)
    public static class GenericState {

        final Random random = new Random(0);

        /** Which ALPN protocols are in use. Only useful with TLS. */
        List<Protocol> protocols = Arrays.asList(Protocol.HTTP_1_1);

        /** Which client to run. */
        @Param
        Client client;

        /** How many concurrent requests to execute. */
        @Param({"1", "10"})
        int concurrencyLevel;

        /** How many requests to enqueue to await threads to execute them. */
        @Param({"10"})
        int targetBacklog;

        /** True to use TLS. */
        // TODO: compare different ciphers?
        @Param({"true"})
        boolean tls;

        /** True to use gzip content-encoding for the response body. */
        @Param({"true"})
        boolean gzip;

        /** Don't combine chunked with HTTP_2; that's not allowed. */
        @Param({"false"})
        boolean chunked;

        /** The size of the HTTP response body, in uncompressed bytes. */
        @Param({"128", "1048576"})
        int bodyByteCount;

        /** How many additional headers were included, beyond the built-in ones. */
        @Param({"0", "20"})
        int headerCount;
    }
}