package cwnuchrome.aac_cwnu_it_2015_1;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Chrome on 8/5/15.
 *
 * 데드락 발생을 검출하기 위한 Wrapper 클래스.
 * 이 클래스의 공리:
 * - 모든 락은 n번째 중첩 직후와 n번째 중첩 해제 직전의 홀드 카운트가 동일하다.
 *
 */
public class LockWrapper {
    ReentrantReadWriteLock lock;
    ActionMain actionMain;
    ReentrantReadWriteLock.ReadLock read_lock;
    ReentrantReadWriteLock.WriteLock write_lock;
    ReadLockWrapper read_lock_wrapper;
    WriteLockWrapper write_lock_wrapper;

    public LockWrapper(ActionMain actionMain) {
        this.actionMain = actionMain;
        this.lock = new ReentrantReadWriteLock();
        read_lock = lock.readLock();
        write_lock = lock.writeLock();
        read_lock_wrapper = new ReadLockWrapper();
        write_lock_wrapper = new WriteLockWrapper();
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    private class HoldCountStructure {
        int write_hold_count;
        int read_hold_count;
        public HoldCountStructure(int write_hold_count, int read_hold_count) {
            this.write_hold_count = write_hold_count;
            this.read_hold_count = read_hold_count;
        }
    }

    public class WriteLockWrapper {
        ArrayList<HoldCountStructure> counts;

        public WriteLockWrapper() {
            counts = new ArrayList<>(AACGroupContainerPreferences.LOCK_WRAPPER_STACK_INIT_SIZE);
        }

        public void lock() {
            write_lock.lock();
            counts.add(new HoldCountStructure(lock.getWriteHoldCount(), lock.getReadHoldCount()));
        }

        public void unlock() {
            HoldCountStructure hcs = counts.remove(counts.size() - 1);
//            int write_hold_count = lock.getWriteHoldCount();
//            int read_hold_count = lock.getReadHoldCount();
//            if (hcs.write_hold_count != write_hold_count || hcs.read_hold_count != read_hold_count) {
//                StringBuilder sb = new StringBuilder(50)
//                        .append("Stored: ReadHoldCount = ")
//                        .append(hcs.read_hold_count)
//                        .append(", WriteHoldCount = ")
//                        .append(hcs.write_hold_count);
//                System.out.println(sb.toString());
//                sb.setLength(0);
//
//                sb
//                        .append("Current: ReadHoldCount = ")
//                        .append(read_hold_count)
//                        .append(", WriteHoldCount = ")
//                        .append(write_hold_count);
//                System.out.println(sb.toString());
//
//                throw new IllegalStateException("Lock count mismatch!! Check the code!!");
//            }
            write_lock.unlock();

            if (write_lock.getHoldCount() == 0) {
                actionMain.activate_morpheme_analyzer();
            }
        }

        public void unlock_without_read_lock_check() {
            HoldCountStructure hcs = counts.remove(counts.size() - 1);
//            int write_hold_count = lock.getWriteHoldCount();
//            if (hcs.write_hold_count != write_hold_count) {
//                StringBuilder sb = new StringBuilder(50)
//                        .append("Stored: WriteHoldCount = ")
//                        .append(hcs.write_hold_count);
//                System.out.println(sb.toString());
//                sb.setLength(0);
//
//                sb
//                        .append("Current: WriteHoldCount = ")
//                        .append(write_hold_count);
//                System.out.println(sb.toString());
//                throw new IllegalStateException("Lock count mismatch!! Check the code!!");
//            }
            write_lock.unlock();

            if (write_lock.getHoldCount() == 0) {
                actionMain.activate_morpheme_analyzer();
            }
        }

        public Lock getLock() {
            return write_lock;
        }
    }

    public class ReadLockWrapper {
        ArrayList<HoldCountStructure> counts;

        public ReadLockWrapper() {
            counts = new ArrayList<>(AACGroupContainerPreferences.LOCK_WRAPPER_STACK_INIT_SIZE);
        }

        public void lock() {
            read_lock.lock();
            counts.add(new HoldCountStructure(lock.getWriteHoldCount(), lock.getReadHoldCount()));
        }

        public void unlock() {
            HoldCountStructure hcs = counts.remove(counts.size() - 1);
//            int write_hold_count = lock.getWriteHoldCount();
//            int read_hold_count = lock.getReadHoldCount();
//            if (hcs.write_hold_count != write_hold_count || hcs.read_hold_count != read_hold_count) {
//                StringBuilder sb = new StringBuilder(50)
//                        .append("Stored: ReadHoldCount = ")
//                        .append(hcs.read_hold_count)
//                        .append(", WriteHoldCount = ")
//                        .append(hcs.write_hold_count);
//                System.out.println(sb.toString());
//                sb.setLength(0);
//
//                sb
//                        .append("Current: ReadHoldCount = ")
//                        .append(read_hold_count)
//                        .append(", WriteHoldCount = ")
//                        .append(write_hold_count);
//                System.out.println(sb.toString());
//
//                throw new IllegalStateException("Lock count mismatch!! Check the code!!");
//            }
            read_lock.unlock();
        }

        public void unlock_without_write_lock_check() {
            HoldCountStructure hcs = counts.remove(counts.size() - 1);
//            int read_hold_count = lock.getReadHoldCount();
//            if (hcs.read_hold_count != read_hold_count) {
//                StringBuilder sb = new StringBuilder(50)
//                        .append("Stored: ReadHoldCount = ")
//                        .append(hcs.read_hold_count);
//                System.out.println(sb.toString());
//                sb.setLength(0);
//
//                sb
//                        .append("Current: ReadHoldCount = ")
//                        .append(read_hold_count);
//                System.out.println(sb.toString());
//
//                throw new IllegalStateException("Lock count mismatch!! Check the code!!");
//            }
            read_lock.unlock();
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
}