package com.syos.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AUTOMATED CONCURRENCY TEST CLIENT
 * 
 * Satisfies Assignment Criteria:
 * "Write two or more test clients that will simultaneously send 
 * asynchronous requests in rapid succession to the server."
 */
public class RapidCheckoutTest {

    private static final int NUMBER_OF_CLIENTS = 40; 
    
    private static final int REQUESTS_PER_CLIENT = 5; 
    
    private static final String TARGET_URL = "http://localhost:8080/api/items?action=getAll";

    // Synchronization aids to ensure all threads attack at the EXACT same time
    private static final CountDownLatch startSignal = new CountDownLatch(1);
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("==========================================");
        System.out.println("   SYOS HTTP CONCURRENCY STRESS TEST");
        System.out.println("==========================================");
        System.out.println("Target: " + TARGET_URL);
        System.out.println("Simulated Users: " + NUMBER_OF_CLIENTS);
        System.out.println("Requests/User:   " + REQUESTS_PER_CLIENT);
        System.out.println("Total Requests:  " + (NUMBER_OF_CLIENTS * REQUESTS_PER_CLIENT));
        System.out.println("------------------------------------------");

        ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 1; i <= NUMBER_OF_CLIENTS; i++) {
            futures.add(executor.submit(new HttpWorker(i)));
        }

        System.out.println("Initializing clients...");
        Thread.sleep(1000);
        
        System.out.println(">>> STARTING ATTACK IN 3... 2... 1... GO!");

        long startTime = System.currentTimeMillis();
        startSignal.countDown();

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // 6. Report Results
        executor.shutdown();
        System.out.println("\n==========================================");
        System.out.println("TEST COMPLETED");
        System.out.println("Total Time: " + totalTime + "ms");
        System.out.println("Successful Responses (200 OK): " + successCount.get());
        System.out.println("Failed Responses: " + failCount.get());
        System.out.println("==========================================");
    }

    /**
     * Inner class representing one User/Client
     */
    static class HttpWorker implements Runnable {
        private final int clientId;
        private final HttpClient httpClient;

        public HttpWorker(int clientId) {
            this.clientId = clientId;
            // Use Java 11+ HttpClient
            this.httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }

        @Override
        public void run() {
            try {
                // Wait for the main thread to say "GO"
                startSignal.await();
                
                System.out.println("Client #" + clientId + " running...");

                for (int i = 0; i < REQUESTS_PER_CLIENT; i++) {
                    sendRequest(i);
                }
                
                System.out.println("Client #" + clientId + " finished.");

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        private void sendRequest(int requestId) {
            try {
                // Build the GET request
                HttpRequest request = HttpRequest.newBuilder()
                        .GET()
                        .uri(URI.create(TARGET_URL))
                        .header("User-Agent", "SYOS-AutoTest-" + clientId)
                        .build();

                // Send request
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                // Check result
                if (response.statusCode() == 200) {
                    successCount.incrementAndGet();
                } else {
                    failCount.incrementAndGet();
                    System.err.println("Client #" + clientId + " Req #" + requestId + 
                        " Error: " + response.statusCode());
                }

            } catch (Exception e) {
                failCount.incrementAndGet();
                System.err.println("Client #" + clientId + " Connection Failed: " + e.getMessage());
            }
        }
    }
}