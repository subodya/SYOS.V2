package com.syos.server.concurrency;

import com.syos.server.business.BusinessFacade;
import com.syos.common.dto.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;


public class RequestProcessor {
    
    private static final int QUEUE_CAPACITY = 1000;
    private static final int NUM_WORKERS = 10;

    private final BlockingQueue<ClientRequest<?>> requestQueue;
    private final ExecutorService workerPool;
    private final BusinessFacade businessFacade;
    private final AtomicBoolean running;
    
    // Metrics
    private final AtomicLong processedCount;
    private final AtomicLong rejectedCount;
    
    public RequestProcessor(BusinessFacade businessFacade) {
        this.requestQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        this.workerPool = Executors.newFixedThreadPool(NUM_WORKERS);
        this.businessFacade = businessFacade;
        this.running = new AtomicBoolean(true);
        this.processedCount = new AtomicLong(0);
        this.rejectedCount = new AtomicLong(0);
        
        startWorkers();
    }

    private void startWorkers() {
        for (int i = 0; i < NUM_WORKERS; i++) {
            workerPool.submit(new WorkerThread(i));
        }
        System.out.println("✓ Started " + NUM_WORKERS + " worker threads");
    }

    public <T> CompletableFuture<T> submitRequest(ClientRequest<T> request) {
        try {
            boolean accepted = requestQueue.offer(request, 5, TimeUnit.SECONDS);
            if (!accepted) {
                rejectedCount.incrementAndGet();
                request.completeExceptionally(
                    new RejectedExecutionException("Server queue full"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            request.completeExceptionally(e);
        }
        return request.getFuture();
    }

    /**
     * Worker thread that processes requests from the queue
     */
    private class WorkerThread implements Runnable {
        private final int workerId;

        public WorkerThread(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public void run() {
            System.out.println("Worker " + workerId + " started");
            
             while (running.get() && !Thread.currentThread().isInterrupted()) {
                 try {
                     ClientRequest<?> request = requestQueue.take();


                     try { Thread.sleep(200); } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                     } 

                     long startTime = System.nanoTime();
                     processRequest(request);

                    long duration = System.nanoTime() - startTime;
                    
                    processedCount.incrementAndGet();
                    
                    System.out.printf("[Worker %d] Processed %s in %.2f ms (Queue: %d)%n",
                        workerId, request.getType(), 
                        duration / 1_000_000.0, requestQueue.size());
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            System.out.println("Worker " + workerId + " stopped");
        }

        @SuppressWarnings("unchecked")
private <T> void processRequest(ClientRequest<T> request) {
    try {
        T result = switch (request.getType()) {
            case CHECKOUT -> (T) processCheckout((ClientRequest<BillDto>) request);
            case ADD_INVENTORY -> (T) processAddInventory((ClientRequest<Void>) request);
            case ADD_ITEM -> (T) processAddItem((ClientRequest<Void>) request); // <--- ADD THIS
            case GET_ITEM -> (T) processGetItem((ClientRequest<ItemDto>) request);
            case SEARCH_ITEMS -> (T) processSearchItems((ClientRequest<java.util.List<ItemDto>>) request);
            case GET_ALL_ITEMS -> (T) processGetAllItems((ClientRequest<java.util.List<ItemDto>>) request);
            case GET_LOW_STOCK -> (T) processGetLowStock((ClientRequest<java.util.List<ItemDto>>) request);
        };
        request.complete(result);
    } catch (Exception e) {
        request.completeExceptionally(e);
    }
}
    private Void processAddItem(ClientRequest<Void> request) {
        ItemDto itemDto = (ItemDto) request.getPayload();
        businessFacade.addItem(itemDto); 
        return null;
    }
        
        private BillDto processCheckout(ClientRequest<BillDto> request) {
            CheckoutRequest checkoutReq = (CheckoutRequest) request.getPayload();
            return businessFacade.processCheckout(checkoutReq);
        }

        private Void processAddInventory(ClientRequest<Void> request) {
            InventoryBatchDto batch = (InventoryBatchDto) request.getPayload();
            businessFacade.addInventory(batch);
            return null;
        }

        private ItemDto processGetItem(ClientRequest<ItemDto> request) {
            String itemCode = (String) request.getPayload();
            return businessFacade.getItem(itemCode);
        }

        private java.util.List<ItemDto> processSearchItems(ClientRequest<java.util.List<ItemDto>> request) {
            String query = (String) request.getPayload();
            return businessFacade.searchItems(query);
        }

        private java.util.List<ItemDto> processGetAllItems(ClientRequest<java.util.List<ItemDto>> request) {
            return businessFacade.getAllItems();
        }

        private java.util.List<ItemDto> processGetLowStock(ClientRequest<java.util.List<ItemDto>> request) {
            return businessFacade.getLowStockItems();
        }
    }

    public ServerMetrics getMetrics() {
        return new ServerMetrics(
            requestQueue.size(),
            processedCount.get(),
            rejectedCount.get(),
            NUM_WORKERS
        );
    }

    public void shutdown() {
        running.set(false);
        workerPool.shutdown();
    }
}