package com.syos.server.concurrency;

import java.util.concurrent.CompletableFuture;

public class ClientRequest<T> {
    private final String requestId;
    private final RequestType type;
    private final Object payload;
    private final CompletableFuture<T> future;
    private final long timestamp;

    public ClientRequest(RequestType type, Object payload) {
        this.requestId = java.util.UUID.randomUUID().toString();
        this.type = type;
        this.payload = payload;
        this.future = new CompletableFuture<>();
        this.timestamp = System.currentTimeMillis();
    }

    public String getRequestId() { return requestId; }
    public RequestType getType() { return type; }
    public Object getPayload() { return payload; }
    public CompletableFuture<T> getFuture() { return future; }
    public long getTimestamp() { return timestamp; }

    public void complete(T result) {
        future.complete(result);
    }

    public void completeExceptionally(Throwable ex) {
        future.completeExceptionally(ex);
    }

    public enum RequestType {
        CHECKOUT,
        ADD_INVENTORY,
        GET_ITEM,
        SEARCH_ITEMS,
        GET_ALL_ITEMS,
        GET_LOW_STOCK
    }
}