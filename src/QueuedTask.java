import java.util.concurrent.CompletableFuture;

public class QueuedTask<T> {
    final Task<T> task;
    final CompletableFuture<T> future;

    public QueuedTask(Task<T> task) {
        this.task = task;
        this.future = new CompletableFuture<T>().exceptionally(e -> {
            System.err.println("Exception caught while executing task id : " + this.task.taskUUID() + " with exception : " + e.getMessage());
            return null;
        });
    }

    public void run() {
        GroupLock groupLock = TaskGroupLockManager.getGroupLock(this.task);
        groupLock.lock();
        try {
            T result = task.taskAction().call();
            future.complete(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            future.completeExceptionally(e);
        } finally {
            groupLock.unlock();
        }
    }
}