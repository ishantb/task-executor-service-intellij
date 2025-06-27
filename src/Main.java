import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TaskExecutor executor = new TaskExecutorServiceImpl(2);

        TaskGroup taskGroup1 = new TaskGroup(UUID.randomUUID());
        TaskGroup taskGroup2 = new TaskGroup(UUID.randomUUID());

        Task<Integer> task1 = new Task<>(
                UUID.randomUUID(),
                taskGroup1,
                TaskType.Read,
                () -> {
                    Thread.sleep(500);
                    return 1;
                }
        );
        Task<Integer> task2 = new Task<>(
                UUID.randomUUID(),
                taskGroup2,
                TaskType.Write,
                () -> {
                    throw new RuntimeException("Some Exception");
                }
        );
        Task<String> task3 = new Task<>(
                UUID.randomUUID(),
                taskGroup1,
                TaskType.Read,
                () -> "3"
        );

        List<Future<?>> futureList = new ArrayList<>();
        for (Task<?> task : Arrays.asList(task1, task2, task3)) {
            futureList.add(executor.submitTask(task));
        }

        int i = 1;
        for (Future<?> future : futureList) {
            var result = future.get();
            if (result == null) {
                result = "NULL";
            }
            System.out.println("Result of " + i++ + "th submitted task is : " + result);
        }

        executor.shutdown();
    }
}