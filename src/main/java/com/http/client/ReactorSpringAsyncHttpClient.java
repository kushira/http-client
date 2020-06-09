package com.http.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ReactorSpringAsyncHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReactorSpringAsyncHttpClient.class);
    private final WebClient webClient;
    private static int INSTANCES = 0;

    public static int INSTANCES() {
        return INSTANCES;
    }

    public ReactorSpringAsyncHttpClient() {
        ReactorSpringAsyncHttpClient.INSTANCES++;
        ClientHttpConnector httpConnector = new ReactorClientHttpConnector();
        webClient = WebClient.builder().clientConnector(httpConnector).build();
    }

    public void doRequest() {
        int count = 0;
        Set<Mono<ClientResponse>> monos = new HashSet<>();
        String id = UUID.randomUUID().toString();
        while (count < 1000) {
            monos.add(
                    webClient.get().uri("http://localhost:3000/perf/" + id).header("thread", Thread.currentThread().getName())
                            .exchange());
            count++;
        }

        for (Mono<ClientResponse> entry : monos) {
            ClientResponse response = entry.block();
            assert id.equals(response.headers().header("id").get(0));
            assert response.rawStatusCode() == 200;
            response.releaseBody().block();
        }
    }

    public void close() {
    }

    public static void main(String[] args) {
        ReactorSpringAsyncHttpClient httpClient = new ReactorSpringAsyncHttpClient();
        httpClient.doRequest();
    }

}
