package com.http.client;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ApacheAsyncHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApacheAsyncHttpClient.class);
    private static int INSTANCES = 0;

    private final PoolingNHttpClientConnectionManager connectionManager;
    private final CloseableHttpAsyncClient httpclient;

    public static int INSTANCES() {
        return INSTANCES;
    }

    public ApacheAsyncHttpClient() {
        ApacheAsyncHttpClient.INSTANCES++;
        ConnectingIOReactor connectingIOReactor;
        try {
            connectingIOReactor = new DefaultConnectingIOReactor();
        } catch (IOReactorException e) {
            throw new RuntimeException(e);
        }
        connectionManager = new PoolingNHttpClientConnectionManager(connectingIOReactor);
        connectionManager.setMaxTotal(100);
        httpclient = HttpAsyncClients.custom().setConnectionManager(connectionManager).build();
//        httpclient = HttpAsyncClients.createDefault();
        httpclient.start();
    }

    public void doRequest() {
        int count = 0;
        Set<Future<HttpResponse>> futures = new HashSet<>();
        String id = UUID.randomUUID().toString();
        while (count < 1000) {
            HttpGet request = new HttpGet("http://localhost:3000/perf/" + id);
            request.addHeader("thread", Thread.currentThread().getName());
            futures.add(httpclient.execute(request, null));
            count++;
        }

        for (Future<HttpResponse> entry : futures) {
            try {
                HttpResponse response = entry.get();
                assert id.equals(response.getFirstHeader("id").getValue());
                assert response.getStatusLine().getStatusCode() == 200;
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(e.getMessage(), e);
//                LOGGER.error("Client running: {} Pool available:{}, pending:{}", httpclient.isRunning(),
//                        connectionManager.getTotalStats().getAvailable(),
//                        connectionManager.getTotalStats().getPending());
            }
        }
    }

    public void close() {
        try {
//            LOGGER.info("Closing the client");
            httpclient.close();
        } catch (IOException e) {
            LOGGER.error("Cannot close the client", e);
        }
    }

}
