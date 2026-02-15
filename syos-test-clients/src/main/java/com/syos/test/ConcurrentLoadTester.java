package com.syos.test;

import com.syos.common.dto.*;
import com.syos.common.util.JsonUtil;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CONCURRENT LOAD TESTER
 * Tests BlockingQueue and Worker Threads
 * 
 * Usage:
 *   1. Start database server
 *   2. Start application server
 *   3. Run this test client
 */
public class ConcurrentLoadTester {
    
    private static final String SERVER_URL = "http://localhost:8080";
    private static final int NUM_CLIENTS = 50;  // Simulate 50 concurrent clients
    private static final int REQUESTS_PER_CLIENT = 5;
    
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);
    
    public static void main(String[] args) throws Exception {
        System.out.println("===========================================");
        System.out.println("   SYOS CONCURRENT LOAD TESTER");
        System.out.println("===========================================");
        System.out.println("Testing BlockingQueue & Worker Threads");
        System.out.println("Server: " + SERVER_URL);
        System.out.println("Concurrent Clients: " + NUM_CLIENTS);
        System.out.println("Requests per Client: " + REQUESTS_PER_CLIENT);
        System.out.println("Total Requests: " + (NUM_CLIENTS * REQUESTS_PER_CLIENT));
        System.out.println("===========================================\n");
        
        // Check server is running
        if (!checkServerStatus()) {
            System.err.println("✗ Server is not running!");
            System.err.println("  Start server first: cd syos-server/target && java -jar syos-server-2.0.0.jar");
            System.exit(1);
        }
        
        System.out.println("✓ Server is running\n");
        
        // Run tests
        System.out.println("Starting concurrent requests...\n");
        
        long startTime = System.currentTimeMillis();
        
        // Test 1: GET requests (lightweight)
        testConcurrentGETRequests();
        
        // Test 2: POST requests (heavier load)
        testConcurrentPOSTRequests();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Final report
        System.out.println("\n===========================================");
        System.out.println("   TEST RESULTS");
        System.out.println("===========================================");
        System.out.println("Total Time: " + totalTime + " ms");
        System.out.println("Successful Requests: " + successCount.get());
        System.out.println("Failed Requests: " + failureCount.get());
        System.out.println("Requests per Second: " + 
            (successCount.get() * 1000.0 / totalTime));
        System.out.println("===========================================\n");
        
        // Check final status
        checkServerStatus();
    }
    
    /**
     * Test 1: Concurrent GET requests (fetch all items)
     */
    private static void testConcurrentGETRequests() throws Exception {
        System.out.println("TEST 1: Concurrent GET Requests");
        System.out.println("-------------------------------");
        
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientId = i;
            Future<?> future = executor.submit(() -> {
                for (int j = 0; j < REQUESTS_PER_CLIENT; j++) {
                    try {
                        String response = sendGET("/api/items?action=getAll");
                        
                        if (response.contains("itemCode")) {
                            successCount.incrementAndGet();
                            System.out.println("[Client " + clientId + "] GET success ✓");
                        } else {
                            failureCount.incrementAndGet();
                            System.err.println("[Client " + clientId + "] GET failed ✗");
                        }
                        
                        Thread.sleep(100); // Small delay between requests
                        
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("[Client " + clientId + "] Error: " + e.getMessage());
                    }
                }
            });
            futures.add(future);
        }
        
        // Wait for all clients to complete
        for (Future<?> future : futures) {
            future.get();
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        
        System.out.println("GET Test Complete: " + successCount.get() + " successful\n");
    }
    
    /**
     * Test 2: Concurrent POST requests (checkout)
     */
    private static void testConcurrentPOSTRequests() throws Exception {
        System.out.println("TEST 2: Concurrent POST Requests (Checkout)");
        System.out.println("-------------------------------------------");
        
        ExecutorService executor = Executors.newFixedThreadPool(NUM_CLIENTS);
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int clientId = i;
            Future<?> future = executor.submit(() -> {
                for (int j = 0; j < REQUESTS_PER_CLIENT; j++) {
                    try {
                        // Create checkout request
                        CheckoutRequest request = createCheckoutRequest();
                        String json = JsonUtil.toJson(request);
                        
                        String response = sendPOST("/api/checkout", json);
                        
                        if (response.contains("billId")) {
                            successCount.incrementAndGet();
                            System.out.println("[Client " + clientId + "] POST success ✓");
                        } else {
                            failureCount.incrementAndGet();
                            System.err.println("[Client " + clientId + "] POST failed ✗");
                        }
                        
                        Thread.sleep(200); // Delay between checkouts
                        
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.err.println("[Client " + clientId + "] Error: " + e.getMessage());
                    }
                }
            });
            futures.add(future);
        }
        
        // Wait for all clients to complete
        for (Future<?> future : futures) {
            future.get();
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        
        System.out.println("POST Test Complete: " + successCount.get() + " successful\n");
    }
    
    /**
     * Check server status
     */
    private static boolean checkServerStatus() {
        try {
            String response = sendGET("/api/status");
            System.out.println("Server Status: " + response);
            return response.contains("queueSize");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Send GET request
     */
    private static String sendGET(String path) throws Exception {
        URL url = new URL(SERVER_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return new String(conn.getInputStream().readAllBytes());
        } else {
            throw new Exception("HTTP " + responseCode);
        }
    }
    
    /**
     * Send POST request
     */
    private static String sendPOST(String path, String json) throws Exception {
        URL url = new URL(SERVER_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            return new String(conn.getInputStream().readAllBytes());
        } else {
            throw new Exception("HTTP " + responseCode);
        }
    }
    
    /**
     * Create sample checkout request
     */
    private static CheckoutRequest createCheckoutRequest() {
        List<BillItemDto> items = new ArrayList<>();
        items.add(new BillItemDto("ITEM-001", "Test Item", 1, 
            new java.math.BigDecimal("10.00"), new java.math.BigDecimal("10.00")));
        
        return new CheckoutRequest("CUST-001", items, "CASH", "TEST-CASHIER");
    }
}