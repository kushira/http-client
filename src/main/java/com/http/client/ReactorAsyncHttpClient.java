package com.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ReactorAsyncHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorAsyncHttpClient.class);
    private HttpClient httpClient;
    private static int INSTANCES = 0;

    public static int INSTANCES() {
        return INSTANCES;
    }

    public ReactorAsyncHttpClient() {
        ReactorAsyncHttpClient.INSTANCES++;
        this.httpClient = HttpClient.create();
    }

    public void doRequest() {
        int count = 0;
        Set<Mono<String>> monos = new HashSet<>();
        String id = UUID.randomUUID().toString();
        while (count < 1000) {
            monos.add(httpClient
                    .headers(h -> h.add("thread", Thread.currentThread().getName()))
                    .get()
                    .uri("http://localhost:3000/perf/" + id)
                    .responseSingle((httpClientResponse, byteBufMono) -> {
                                assert id.equals(httpClientResponse.responseHeaders().get("id"));
                                assert httpClientResponse.status().code() == 200;
                                return byteBufMono.asString();
                            }
                    ));
            count++;
        }

        for (Mono<String> entry : monos) {
            String response = entry.block();
            assert response != null;
        }
    }

    public void close() {

    }

    public static void main(String[] args) {
        ReactorAsyncHttpClient httpClient = new ReactorAsyncHttpClient();
        httpClient.doRequest();
    }

}
