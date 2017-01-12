/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package okhttp3.benchmarks;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import okhttp3.HttpUrl;
import okhttp3.internal.tls.SslClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import okio.GzipSink;

/**
 * This benchmark is fake, but may be useful for certain relative comparisons. It uses a local
 * connection to a MockWebServer to measure how many identical requests per second can be carried
 * over a fixed number of threads.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class Benchmark {
  private static final int NUM_REPORTS = 10;
  private static final boolean VERBOSE = true;

  @org.openjdk.jmh.annotations.Benchmark
  public double fake(States.GenericState state) throws Exception {
    if (VERBOSE) System.out.println(toString(state));
    HttpClient httpClient = state.client.create();

    // Prepare the client & server
    httpClient.prepare(state);
    MockWebServer server = startServer(state);
    HttpUrl url = server.url("/");

    int requestCount = 0;
    long reportStart = System.nanoTime();
    long reportPeriod = TimeUnit.SECONDS.toNanos(1);
    int reports = 0;
    double best = 0.0;

    // Run until we've printed enough reports.
    while (reports < NUM_REPORTS) {
      // Print a report if we haven't recently.
      long now = System.nanoTime();
      double reportDuration = now - reportStart;
      if (reportDuration > reportPeriod) {
        double requestsPerSecond = requestCount / reportDuration * TimeUnit.SECONDS.toNanos(1);
        if (VERBOSE) {
          System.out.println(String.format("Requests per second: %.1f", requestsPerSecond));
        }
        best = Math.max(best, requestsPerSecond);
        requestCount = 0;
        reportStart = now;
        reports++;
      }

      // Fill the job queue with work.
      while (httpClient.acceptingJobs()) {
        httpClient.enqueue(url);
        requestCount++;
      }

      // The job queue is full. Take a break.
      sleep(1);
    }

    return best;
  }

  public String toString(States.GenericState state) {
    List<Object> modifiers = new ArrayList<>();
    if (state.tls) modifiers.add("tls");
    if (state.gzip) modifiers.add("gzip");
    if (state.chunked) modifiers.add("chunked");
    modifiers.addAll(state.protocols);

    return String.format("%s %s\nbodyByteCount=%s headerCount=%s concurrencyLevel=%s",
            state.client, modifiers, state.bodyByteCount, state.headerCount, state.concurrencyLevel);
  }

  private void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) {
    }
  }

  private MockWebServer startServer(States.GenericState state) throws IOException {
    Logger.getLogger(MockWebServer.class.getName()).setLevel(Level.WARNING);
    MockWebServer server = new MockWebServer();

    if (state.tls) {
      SslClient sslClient = SslClient.localhost();
      server.useHttps(sslClient.socketFactory, false);
      server.setProtocols(state.protocols);
    }

    final MockResponse response = newResponse(state);
    server.setDispatcher(new Dispatcher() {
      @Override public MockResponse dispatch(RecordedRequest request) {
        return response;
      }
    });

    server.start();
    return server;
  }

  private MockResponse newResponse(States.GenericState state) throws IOException {
    byte[] bytes = new byte[state.bodyByteCount];
    state.random.nextBytes(bytes);
    Buffer body = new Buffer().write(bytes);

    MockResponse result = new MockResponse();

    if (state.gzip) {
      Buffer gzipBody = new Buffer();
      GzipSink gzipSink = new GzipSink(gzipBody);
      gzipSink.write(body, body.size());
      gzipSink.close();
      body = gzipBody;
      result.addHeader("Content-Encoding: gzip");
    }

    if (state.chunked) {
      result.setChunkedBody(body, 1024);
    } else {
      result.setBody(body);
    }

    for (int i = 0; i < state.headerCount; i++) {
      result.addHeader(randomString(12, state), randomString(20, state));
    }

    return result;
  }

  private String randomString(int length, States.GenericState state) {
    String alphabet = "-abcdefghijklmnopqrstuvwxyz";
    char[] result = new char[length];
    for (int i = 0; i < length; i++) {
      result[i] = alphabet.charAt(state.random.nextInt(alphabet.length()));
    }
    return new String(result);
  }
}
