package thumbnail;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/*
 * ---------------------Used only for version 3 of the treatImages function---------------------
 * helper class that wraps ExecutorService and implements AutoCloseable, 
 * which will automatically shut down the executor when it goes out of scope.
 */
public class AutoCloseableExecutorService implements AutoCloseable {
    private final ExecutorService executorService;

    public AutoCloseableExecutorService(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Some tasks did not finish within the timeout.");
            }
        } catch (InterruptedException e) {
            System.err.println("Task interrupted: " + e.getMessage());
        }
    }
}
