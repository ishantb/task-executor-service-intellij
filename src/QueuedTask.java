import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class QueuedTask<T> {
    final Task<T> task;
    final CompletableFuture<T> future = new CompletableFuture<>();

    public QueuedTask(Task<T> task) {
        this.task = task;
    }

    public void run() {
        ReentrantLock groupLock = TaskGroupLockManager.getGroupLock(this.task);
        groupLock.lock();
        try {
            T result = task.taskAction().call();
            future.complete(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            groupLock.unlock();
        }
    }
}