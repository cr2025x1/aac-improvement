package cwnuchrome.aac_cwnu_it_2015_1;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Chrome on 8/5/15.
 *
 * 데드락 발생을 검출하기 위한 Wrapper 클래스.
 * 또한 ReentrantReadWriteLock의 read_lock.lock()을 자체적으로 통제해 writer의 대기열 우선권을 무효화하는 역할도 함.
 */
public class LockWrapper {
    ReentrantReadWriteLock lock;
    ActionMain actionMain;
    ReentrantReadWriteLock.ReadLock read_lock;
    ReentrantReadWriteLock.WriteLock write_lock;
    ReadLockWrapper read_lock_wrapper;
    WriteLockWrapper write_lock_wrapper;
    final HashMap<Thread, Integer> thread_read_lock_map;
//    final ArrayList<Thread> thread_write_lock_list;

    public LockWrapper(ActionMain actionMain) {
        this.actionMain = actionMain;
        this.lock = new ReentrantReadWriteLock();
        read_lock = lock.readLock();
        write_lock = lock.writeLock();
        read_lock_wrapper = new ReadLockWrapper();
        write_lock_wrapper = new WriteLockWrapper();
        thread_read_lock_map = new HashMap<>(20);
//        thread_write_lock_list = new ArrayList<>(20);
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    public class WriteLockWrapper {
        public void lock() {
            log(null, "Trying to get write lock... " + Thread.currentThread().toString());
            write_lock.lock();
            log_with_lock_stat(null, "Write lock acquired.", Thread.currentThread(), lock.getWriteHoldCount(), get_read_hold_count(), lock.getReadLockCount());
        }

        public void unlock() {
            write_lock.unlock();
            log_with_lock_stat(null, "Write lock released.", Thread.currentThread(), lock.getWriteHoldCount(), get_read_hold_count(), lock.getReadLockCount());

            if (write_lock.getHoldCount() == 0) {
                actionMain.activate_morpheme_analyzer();
            }
        }

        public void unlock_without_read_lock_check() {
            write_lock.unlock();
            log_with_lock_stat(null, "Write lock released.", Thread.currentThread(), lock.getWriteHoldCount(), get_read_hold_count(), lock.getReadLockCount());

            if (write_lock.getHoldCount() == 0) {
                actionMain.activate_morpheme_analyzer();
            }
        }

        public Lock getLock() {
            return write_lock;
        }
    }

    public class ReadLockWrapper {
        public void lock() {
            log(null, "Trying to get read lock... " + Thread.currentThread().toString());
            if (SubThread.class.isAssignableFrom(Thread.currentThread().getClass())) {
                SubThread sub_thread = (SubThread)Thread.currentThread();
                if (lock.getReadLockCount() == 0) throw new IllegalStateException("Sub-thread is calling the method, but no read lock is done yet. This only can see ");
                log(null, "Sub-thread read lock request denied. It will protected by its parent " + sub_thread.parent_thread.toString() + "'s lock.");
                return;
            }

            if (lock.getReadHoldCount() <= 0) read_lock.lock();
            int hold_count = increase_read_lock_map();
            log_with_lock_stat(null, "Read lock acquired.", Thread.currentThread(), lock.getWriteHoldCount(), hold_count, lock.getReadLockCount());
        }

        public void unlock() {
            int hold_count = decrease_read_lock_map();
            if (hold_count == -1) return;
            if (hold_count == 0) read_lock.unlock();
            log_with_lock_stat(null, "Read lock released.", Thread.currentThread(), lock.getWriteHoldCount(), hold_count, lock.getReadLockCount());
        }

        public void unlock_without_write_lock_check() {
            int hold_count = decrease_read_lock_map();
            if (hold_count == -1) return;
            if (hold_count == 0) read_lock.unlock();
            log_with_lock_stat(null, "Read lock released.", Thread.currentThread(), lock.getWriteHoldCount(), hold_count, lock.getReadLockCount());
        }

        public Lock getLock() {
            return read_lock;
        }
    }

    public ReadLockWrapper read_lock() {
        return read_lock_wrapper;
    }

    public WriteLockWrapper write_lock() {
        return write_lock_wrapper;
    }

    public void log(@Nullable String prefix, @NonNull String text) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4];
        String className = e.getClassName();

        StringBuilder sb = new StringBuilder();
        if (prefix != null) sb.append(prefix);
        sb.append(className.substring(className.lastIndexOf('.') + 1));
        sb.append('.');
        sb.append(e.getMethodName());
        sb.append(": ");
        sb.append(text);

        System.out.println(sb.toString());
    }

    public void log_with_lock_stat(@Nullable String prefix, @NonNull String text, Thread thread, int write_hold_count, int read_hold_count, int read_lock_count) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4];
        String className = e.getClassName();

        StringBuilder sb = new StringBuilder(100);
        if (prefix != null) sb.append(prefix);
        sb
                .append(className.substring(className.lastIndexOf('.') + 1))
                .append('.')
                .append(e.getMethodName())
                .append(": ")
                .append(text)
                .append(" ")
                .append(thread.toString())
                .append(", WH = ")
                .append(write_hold_count)
                .append(", RH = ")
                .append(read_hold_count)
                .append(", RL = ")
                .append(read_lock_count);
        System.out.println(sb.toString());
    }

    private int increase_read_lock_map() {
        Thread thread = Thread.currentThread();
        int hold_count;
        synchronized (thread_read_lock_map) {
            Integer value;
            if ((value = thread_read_lock_map.get(thread)) != null) {
                hold_count = value + 1;
                thread_read_lock_map.put(thread, hold_count);
            } else {
                thread_read_lock_map.put(thread, 1);
                hold_count = 1;
            }
        }
        return hold_count;
    }

    private int decrease_read_lock_map() {
        Thread thread = Thread.currentThread();
        int hold_count;
        synchronized (thread_read_lock_map) {
            Integer value;
            if ((value = thread_read_lock_map.get(thread)) != null) {
                if (value > 1) {
                    hold_count = value - 1;
                    thread_read_lock_map.put(thread, hold_count);
                }
                else {
                    thread_read_lock_map.remove(thread);
                    hold_count = 0;
                }
            }
            else hold_count = -1;
        }
        return hold_count;
    }

    private int get_read_hold_count() {
        Thread thread = Thread.currentThread();
        Integer value;
        synchronized (thread_read_lock_map) {
            if ((value = thread_read_lock_map.get(thread)) == null) {
                value = 0;
            }
        }
        return value;
    }
}