package com.badlogic.gdx.utils.async;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class AsyncExecutor implements Disposable {
    private final ExecutorService executor;

    public AsyncExecutor(int maxConcurrent) {
        this.executor = Executors.newFixedThreadPool(maxConcurrent, new ThreadFactory() {
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "AsynchExecutor-Thread");
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    public <T> AsyncResult<T> submit(final AsyncTask<T> task) {
        if (!this.executor.isShutdown()) {
            return new AsyncResult(this.executor.submit(new Callable<T>() {
                public T call() throws Exception {
                    return task.call();
                }
            }));
        }
        throw new GdxRuntimeException("Cannot run tasks on an executor that has been shutdown (disposed)");
    }

    public void dispose() {
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new GdxRuntimeException("Couldn't shutdown loading thread", e);
        }
    }
}
