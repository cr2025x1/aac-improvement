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

    public LockWrapper(ActionMain actionMain, ReentrantReadWriteLock lock) {
        this.actionMain = actionMain;
        this.lock = lock;
        read_lock = lock.readLock();
        write_lock = lock.writeLock();
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
            if (hcs.write_hold_count != lock.getWriteHoldCount() || hcs.read_hold_count != lock.getReadHoldCount()) {
                throw new IllegalStateException("Lock count mismatch!! Check the code!!");
            }
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
            if (hcs.write_hold_count != lock.getWriteHoldCount() || hcs.read_hold_count != lock.getReadHoldCount()) {
                throw new IllegalStateException("Lock count mismatch!! Check the code!!");
            }
            read_lock.unlock();
        }

        public Lock getLock() {
            return read_lock;
        }
    }
}