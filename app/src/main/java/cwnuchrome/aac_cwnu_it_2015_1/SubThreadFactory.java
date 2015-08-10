package cwnuchrome.aac_cwnu_it_2015_1;

import android.support.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Chrome on 8/8/15.
 *
 * 하위스레드를 생성하는 ThreadFactory 클래스.
 * 이 클래스는 Java 표준 라이브러리의 Executors.defaultThreadFactory() 메소드에서 반환하는,
 * Executors.DefaultThreadFactory 클래스의 코드를 그대로 복사해와 변형한 것이다.
 */
public class SubThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    SubThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }

    /*
     * 실질적으로 변형이 있는 부분은 이 메소드 뿐이다.
     */
    public SubThread newThread(@NonNull Runnable r) { // 반환형이 Thread->SubThread
        // 반환형이 SubThread로 바뀌었고, 생성자를 거기에 맞춰서 변형했다.
        SubThread t = new SubThread(Thread.currentThread(), group, r,
                namePrefix + threadNumber.getAndIncrement(),
                0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);

        return t;
    }
}
