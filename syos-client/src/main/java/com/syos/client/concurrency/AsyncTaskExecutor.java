package com.syos.client.concurrency;

import javafx.application.Platform;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.List;

/**
 * CLIENT-SIDE CONCURRENCY: Async task executor
 * Ensures UI never freezes during server calls
 */
public class AsyncTaskExecutor {
    
    private final ExecutorService executor;

    public AsyncTaskExecutor() {
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Execute task asynchronously and update UI on completion
     */
    public <T> void executeAsync(
            Callable<T> task,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError) {  // CHANGED: Exception -> Throwable
        
        CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executor)
        .thenAccept(result -> {
            // CRITICAL: Update UI on JavaFX thread
            Platform.runLater(() -> onSuccess.accept(result));
        })
        .exceptionally(throwable -> {
            // FIXED: Handle all Throwables, not just Exceptions
            Platform.runLater(() -> {
                Throwable cause = throwable.getCause();
                if (cause == null) {
                    cause = throwable;
                }
                onError.accept(cause);
            });
            return null;
        });
    }

    /**
     * Execute multiple tasks concurrently
     */
    public <T> void executeAllAsync(
            List<Callable<T>> tasks,
            Consumer<List<T>> onSuccess,
            Consumer<Throwable> onError) {  // CHANGED: Exception -> Throwable
        
        List<CompletableFuture<T>> futures = tasks.stream()
            .map(task -> CompletableFuture.supplyAsync(() -> {
                try {
                    return task.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, executor))
            .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()))
            .thenAccept(results -> {
                Platform.runLater(() -> onSuccess.accept(results));
            })
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    Throwable cause = throwable.getCause();
                    if (cause == null) {
                        cause = throwable;
                    }
                    onError.accept(cause);
                });
                return null;
            });
    }

    public void shutdown() {
        executor.shutdown();
    }
}