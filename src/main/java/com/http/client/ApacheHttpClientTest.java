package com.http.client;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

@State(Scope.Benchmark)
public class ApacheHttpClientTest {

    private ApacheAsyncHttpClient httpClient;

    @Setup(Level.Trial)
    public void setUp() {
        httpClient = new ApacheAsyncHttpClient();
    }

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    @Threads(value = 4)
    @Fork(value = 3)
    @Warmup(iterations = 4)
    @Measurement(iterations = 6)
    public void run() {
        assert httpClient != null;
        httpClient.doRequest();
    }

    @TearDown(Level.Trial)
    public void clear() {
        assert ApacheAsyncHttpClient.INSTANCES() == 1;
        httpClient.close();
    }

    public static void main(String args[]) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ApacheHttpClientTest.class.getSimpleName())
                .jvmArgs("-ea")
                .build();

        new Runner(opt).run();
    }

}
