package cwnuchrome.aac_cwnu_it_2015_1;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Chrome on 8/2/15.
 */
public class DBWriteLockWrapper {
    ActionMain actionMain;
    ReentrantReadWriteLock.WriteLock lock;

    public DBWriteLockWrapper(ActionMain actionMain, ReentrantReadWriteLock.WriteLock lock) {
        this.lock = lock;
        this.actionMain = actionMain;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
        if (lock.getHoldCount() == 0) {
            actionMain.activate_morpheme_analyzer();
        }
    }

    public Lock getLock() {
        return lock;
    }
}
