package cwnuchrome.aac_cwnu_it_2015_1;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by Chrome on 8/5/15.
 *
 * 검색, 아이템 추가 등의 상황에서의 키 입력을 받아 주어진 일을 처리하는 멀티스레드 Wrapper
 *
 */
public abstract class KeyEventHandler implements Runnable {
    protected final Object interrupt_check_lock;
//    protected static final int POOL_SIZE = 2;
    protected final ArrayList<Thread> threads;
    ExecutorService search_executor;

    public KeyEventHandler() {
        this(2);
    }

    public KeyEventHandler(int pool_size) {
        interrupt_check_lock = new Object();
        threads = new ArrayList<>(pool_size);
        search_executor = Executors.newFixedThreadPool(pool_size);
    }

    public void execute() {
        search_executor.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!check_mutual_exclusive_interrupt()) {
                            return;
                        }

                        KeyEventHandler.this.run();

                        synchronized (interrupt_check_lock) {
                            threads.remove(Thread.currentThread());
                        }
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

    public void soft_off() {
        search_executor.shutdown();
        try {
            search_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
