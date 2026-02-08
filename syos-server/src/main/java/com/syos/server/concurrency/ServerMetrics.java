package com.syos.server.concurrency;

public class ServerMetrics {
    private final int queueSize;
    private final long processedRequests;
    private final long rejectedRequests;
    private final int workerThreads;

    public ServerMetrics(int queueSize, long processedRequests, 
                        long rejectedRequests, int workerThreads) {
        this.queueSize = queueSize;
        this.processedRequests = processedRequests;
        this.rejectedRequests = rejectedRequests;
        this.workerThreads = workerThreads;
    }

    public int getQueueSize() { return queueSize; }
    public long getProcessedRequests() { return processedRequests; }
    public long getRejectedRequests() { return rejectedRequests; }
    public int getWorkerThreads() { return workerThreads; }
}