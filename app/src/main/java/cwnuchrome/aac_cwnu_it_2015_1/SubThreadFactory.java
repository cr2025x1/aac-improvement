package cwnuchrome.aac_cwnu_it_2015_1;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Chrome on 8/8/15.
 */
public class SubThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    CopyOnWriteArrayList<SubThread> subthreads;

    SubThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
        subthreads = null;
    }

    SubThreadFactory(CopyOnWriteArrayList<SubThread> subthreads) {
        this();
        this.subthreads = subthreads;
    }

    public SubThread newThread(Runnable r) {
        SubThread t = new SubThread(Thread.currentThread(), group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);

        if (subthreads != null) subthreads.add(t);

        return t;
    }

    public void setThreadList(CopyOnWriteArrayList<SubThread> subthreads) {
        this.subthreads = subthreads;
    }

    public CopyOnWriteArrayList<SubThread> getThreadList() {
        return subthreads;
    }
}
