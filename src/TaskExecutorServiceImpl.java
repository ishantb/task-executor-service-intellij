import java.util.concurrent.*;

public class TaskExecutorServiceImpl implements TaskExecutor {

    private final ExecutorService executorService;
    private final BlockingQueue<QueuedTask<?>> taskQueue;
    private final Thread schedulerThread;

    public TaskExecutorServiceImpl(int maxConcurrency) {
        this.executorService = Executors.newFixedThreadPool(maxConcurrency);
        // We can limit capacity of Queue and deny submission if full
        this.taskQueue = new LinkedBlockingQueue<>();
        this.schedulerThread = new Thread(this::scheduleTasks);
        this.schedulerThread.setDaemon(true);
        this.schedulerThread.start();
    }

    @Override
    public <T> Future<T> submitTask(Task<T> task) {
        if (schedulerThread.isInterrupted()) {
            System.err.println("Unable to submit task as Task Executor Service had been shutdown.");
            return null;
        }
        QueuedTask<T> queuedTask = new QueuedTask<>(task);
        taskQueue.offer(queuedTask);
        System.out.println("Submitted task with id : " + task.taskUUID());
        return queuedTask.exceptionHandledFuture;
    }

    private void scheduleTasks() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                QueuedTask<?> queuedTask = taskQueue.take();
                executorService.submit(queuedTask::run);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Submit pending tasks waiting in queue to be scheduled if schedulerThread is interrupted
        while (!taskQueue.isEmpty()) {
            QueuedTask<?> queuedTask = taskQueue.poll();
            executorService.submit(queuedTask::run);
        }
        executorService.shutdown();

        Thread.interrupted(); //Remove interrupted status from thread to wait for termination
        // Wait for completion of all tasks
        while (!executorService.isTerminated()) {
            try {
                if (executorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    break;
                } else {
                    System.out.println("Waiting for execution of submitted tasks for shutdown...");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Interrupted while waiting for execution of submitted tasks. Shutting down forcefully now.");
                executorService.shutdownNow();
                return;
            }
        }
        System.out.println("Executor Service is terminated after execution of all submitted tasks.");
    }

    public void shutdown() {
        schedulerThread.interrupt();
    }
}
