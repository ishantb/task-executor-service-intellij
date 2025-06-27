import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class GroupLock {
    /**
     * We can make taskGroup non final and reuse for new groups by creating an object pool of Group Locks
     * with predefined minimum and maximum size to avoid some overhead of creation and deletion of new Group Locks.
     */
    public final TaskGroup taskGroup;
    private final ReentrantLock reentrantLock;
    public volatile AtomicInteger activeThreadCount;

    public GroupLock(TaskGroup taskGroup) {
        this.taskGroup = taskGroup;
        this.reentrantLock = new ReentrantLock(true);
        this.activeThreadCount = new AtomicInteger(0);
    }

    public void lock() {
        this.reentrantLock.lock();
    }

    public void unlock() {
        this.reentrantLock.unlock();
        TaskGroupLockManager.removeGroupLockIfUnused(this);
    }
}
