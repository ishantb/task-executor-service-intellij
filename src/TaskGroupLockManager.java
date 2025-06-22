import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class TaskGroupLockManager {
    // We can limit number of groups (Blocking tasks in Queue and not polling task till group size reduced)
    private static final ConcurrentHashMap<UUID, ReentrantLock> groupLocks = new ConcurrentHashMap<>();

    public static ReentrantLock getGroupLock(Task<?> task) {
        return groupLocks.computeIfAbsent(task.taskGroup().groupUUID(), id -> new ReentrantLock(true));
    }
}
