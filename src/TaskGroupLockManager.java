import java.util.UUID;
import java.util.concurrent.*;

public class TaskGroupLockManager {
    // We can limit number of groups (Blocking tasks in Queue and not polling task till group size reduced)
    private static final ConcurrentHashMap<UUID, GroupLock> groupLocks = new ConcurrentHashMap<>();

    // Synchronized on taskGroup and hence race condition between below 2 methods - get and remove on same group lock avoided
    public static GroupLock getGroupLock(Task<?> task) {
        TaskGroup taskGroup = task.taskGroup();
        synchronized (taskGroup) {
            GroupLock groupLock = groupLocks.computeIfAbsent(taskGroup.groupUUID(), id -> {
                System.out.println("Creating new Group Lock for Task Group id : " + id);
                return new GroupLock(taskGroup);
            });
            groupLock.activeThreadCount.incrementAndGet();
            return groupLock;
        }
    }

    public static void removeGroupLockIfUnused(GroupLock groupLock) {
        synchronized (groupLock.taskGroup) {
            // Runs like garbage collector - Remove unused group lock from groupLocks map
            if (groupLock.activeThreadCount.decrementAndGet() == 0) {
                groupLocks.remove(groupLock.taskGroup.groupUUID());
                System.out.println("Removing Unused Group Lock for Task Group id : " + groupLock.taskGroup.groupUUID());
            }
        }
    }
}
