package com.syos.test;

import com.syos.common.dto.*;
import com.syos.common.util.JsonUtil;
import java.util.concurrent.atomic.AtomicInteger; 

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;

/**
 * AUTOMATIC TEST CLIENT - Rapid Concurrent Requests
 * Tests server-side concurrency (BlockingQueue handling)
 */
public class RapidCheckoutTest {
    
    private static final String SERVER_URL = "http://localhost:8080/api/checkout";
    private static final int NUM_CLIENTS = 10;
    private static final int REQUESTS_PER_CLIENT = 20;

    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("  RAPID CHECKOUT TEST");
        System.out.println("  Clients: " + NUM_CLIENTS);
        System.out.println("  Requests per client: " + REQUESTS_PER_CLIENT);
        System.out.println("===========================================\n");

        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);
        CountDownLatch latch = new CountDownLatch(NUM_CLIENTS);
        List<Long> responseTimes = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        // Launch concurrent test clients
        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientId = i;
            executor.submit(() -> {
                runClient(clientId, responseTimes, successCount, failureCount);
                latch.countDown();
            });
        }

        // Wait for all clients to complete
        latch.await();
        executor.shutdown();

        long totalTime = System.currentTimeMillis() - startTime;

        // Print results
        printStatistics(responseTimes, successCount.get(), 
                       failureCount.get(), totalTime);
    }

    private static void runClient(int clientId, List<Long> responseTimes,
                                 AtomicInteger successCount, 
                                 AtomicInteger failureCount) {
        
        for (int i = 0; i < REQUESTS_PER_CLIENT; i++) {
            try {
                CheckoutRequest request = generateRandomCheckout(clientId, i);
                
                long startTime = System.nanoTime();
                sendCheckoutRequest(request);
                long duration = System.nanoTime() - startTime;
                
                responseTimes.add(duration / 1_000_000); // Convert to ms
                successCount.incrementAndGet();
                
                System.out.printf("[Client %d] Request %d completed in %d ms%n",
                    clientId, i, duration / 1_000_000);
                
                // Small delay for rapid succession
                Thread.sleep(10);
                
            } catch (Exception e) {
                failureCount.incrementAndGet();
                System.err.printf("[Client %d] Request failed: %s%n", 
                    clientId, e.getMessage());
            }
        }
    }

    private static CheckoutRequest generateRandomCheckout(int clientId, int requestId) {
        Random random = new Random();
        
        List<BillItemDto> items = new ArrayList<>();
        int numItems = random.nextInt(3) + 1;
        
        String[] itemCodes = {"ITEM-001", "ITEM-002", "ITEM-003", "ITEM-004", "ITEM-005"};
        
        for (int i = 0; i < numItems; i++) {
            String itemCode = itemCodes[random.nextInt(itemCodes.length)];
            items.add(new BillItemDto(itemCode, "", random.nextInt(3) + 1,
                BigDecimal.ZERO, BigDecimal.ZERO));
        }

        return new CheckoutRequest(
            "CUST-00" + (random.nextInt(3) + 1),
            items,
            random.nextBoolean() ? "CASH" : "CARD",
            "CASHIER-001"
        );
    }

    private static void sendCheckoutRequest(CheckoutRequest request) throws Exception {
        URL url = new URL(SERVER_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String jsonBody = JsonUtil.toJson(request);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP " + responseCode);
        }
    }

    private static void printStatistics(List<Long> responseTimes, 
                                       int successCount, 
                                       int failureCount,
                                       long totalTime) {
        System.out.println("\n===========================================");
        System.out.println("  TEST RESULTS");
        System.out.println("===========================================");
        System.out.println("Total Requests: " + (successCount + failureCount));
        System.out.println("Successful: " + successCount);
        System.out.println("Failed: " + failureCount);
        System.out.println("Success Rate: " + 
            String.format("%.2f%%", (successCount * 100.0 / (successCount + failureCount))));
        System.out.println("\nTotal Time: " + totalTime + " ms");
        System.out.println("Throughput: " + 
            String.format("%.2f requests/sec", successCount * 1000.0 / totalTime));

        if (!responseTimes.isEmpty()) {
            long sum = responseTimes.stream().mapToLong(Long::longValue).sum();
            double avg = (double) sum / responseTimes.size();
            long min = Collections.min(responseTimes);
            long max = Collections.max(responseTimes);

            System.out.println("\nResponse Times:");
            System.out.println("  Average: " + String.format("%.2f ms", avg));
            System.out.println("  Min: " + min + " ms");
            System.out.println("  Max: " + max + " ms");
        }
        System.out.println("===========================================");
    }
}