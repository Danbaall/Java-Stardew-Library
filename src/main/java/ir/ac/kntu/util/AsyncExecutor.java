package ir.ac.kntu.util;

import javafx.application.Platform;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AsyncExecutor {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private AsyncExecutor() {
        //no instances
    }

    public static <T> void execute(Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        EXECUTOR.submit(() -> {
            try {
                T result = task.call();
                Platform.runLater(() -> {
                    if (onSuccess != null) {
                        onSuccess.accept(result);
                    }
                });
            } catch (Exception exception) {
                Platform.runLater(() -> {
                    if (onError != null) {
                        onError.accept(exception);
                    }
                });
            }
        });
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}