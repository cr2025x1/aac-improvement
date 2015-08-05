package cwnuchrome.aac_cwnu_it_2015_1;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Chrome on 8/5/15.
 *
 * 검색, 아이템 추가 등의 상황에서의 키 입력을 받아 주어진 일을 처리하는 멀티스레드 Wrapper
 *
 */
public class KeyEventHandler {
    protected final Object interrupt_check_lock;
    protected static final int POOL_SIZE = 2;
    protected final ArrayList<Thread> threads;
    ExecutorService search_executor;

    public KeyEventHandler() {
        interrupt_check_lock = new Object();
        threads = new ArrayList<>(POOL_SIZE);
        search_executor = Executors.newFixedThreadPool(POOL_SIZE);
    }

    public void execute(Runnable runnable) {
        search_executor.execute(
                () -> {
                    if (!check_mutual_exclusive_interrupt()) {
                        return;
                    }

                    runnable.run();

                    synchronized (interrupt_check_lock) {
                        threads.remove(Thread.currentThread());
                    }
                }
        );
    }

    protected boolean check_mutual_exclusive_interrupt() {
        Thread thread = Thread.currentThread();
        synchronized (interrupt_check_lock) {
            if (thread.isInterrupted()) {
                threads.remove(thread);
                return false;
            }
            for (Thread t : threads) if (t != thread) t.interrupt();
            if (!threads.contains(thread)) threads.add(thread);
            return true;
        }
    }
}
