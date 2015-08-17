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
 * ReentrantReadWriteLock의 동작 방식을 변경하고, 데드락 발생을 검출하기 위한 Wrapper 클래스.
 * read_lock.lock()을 자체적으로 통제해 writer의 대기열 우선권을 무효화한다.
 *
 * 참고: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html
 *
 * ReentrantReadWriteLock의 동작 방식은 다음과 같다.
 * 1. 쓰기 락은 읽기 락으로 격하할 수 있다. 그 역은 불가능하다.
 * 2. 모든 락은 소유 스레드의 경우 재진입이 가능하다.
 * 3. 읽기 락은 공유된다. 한 스레드가 읽기 락을 가지고 있으면 다른 스레드도 읽기 락을 가질 수 있다.
 * 4. 쓰기 락은 동시에 단 한 스레드만이 가질 수 있다. 쓰기 락은 읽기 락과 동시에 걸릴 수 없다.
 * 5. 대기열에 쓰기 락 요청이 있다면, 먼저 요청된 스레드라도 읽기 락 요청은 블럭된다.
 * 6. 대기열에 쓰기 락 요청이 있다면, 읽기 락의 소유 스레드의 재진입도 블럭된다.
 *
 * 바로 6번을 알지 못한 상황에서 락을 걸었고, 데드락이 발생했던 것이다.
 * 6번의 경우가 특히 뼈아팠는데, 현재 내 락 모델에서는 Atomic해야하는 모든 메소드의 시작과 끝에 락이 걸려 있다.
 * 즉, 읽기 락을 재진입하는 경우가 일상사인데, 백그라운드 갱신 스레드 때문에 쓰기 락 요청이 동시에 들어오는 경우도 빈번했다.
 * 이는 그대로 데드락 상태로 이어져 문제를 일으켰다.
 *
 * 이를 해결하기 위해서는 쓰기 락이 대기열에 있는 경우는 몹시 흔하기 때문에, 읽기 락의 재진입이 발생해서는 안 되었다.
 * 하지만 재진입을 막는 것은 절대 불가능했다. Atomic해야 하는 메소드가 다른 Atomic 메소드를 부르는 일은 얼마든지 가능성이 있기 때문이다.
 * 그렇다면 자체적으로 읽기 락의 재진입을 관리하고, ReentrantReadWriteLock 객체에는 읽기 락의 재진입 요청이 도달하지 않으면 되는 것이다.
 *
 * 그리하여, HashMap<Thread, Integer> 맴버를 두고, 이 맵에 읽기 락을 소지한 스레드와 각 스레드의 락 중첩 수를 관리하게 했다.
 * 재진입이 발생하는 경우에는 ReentrantReadWriteLock 객체에는 lock() 요청을 전달하지 않고, 맵 상의 중첩 수만 늘리는 것이다.
 * 그리고 중첩 수가 1일 때에도 언락 요청 시에만 ReentrantReadWriteLock 객체에 unlock() 요청이 전달되도록 했다.
 * 이렇게 되면 읽기 락이 "재진입"을 요청하는 상황 자체가 ReentrantReadWriteLock 객체 상으로는 발생하지 않는다.
 * 따라서 6번 상황의 조건인 "읽기 락의 재진입"을 아예 없어져 6번 상황이 생기는 일을 차단했다.
 *
 * 그 다음으로 문제가 되는 것은 읽기 락을 가진 스레드가 다시 새 스레드 여럿을 시작해 작업을 맡기고 이 스레드들의 작업 완료를 기다리는 상황이다.
 * 이 때, 쓰기 락 요청이 대기열에 있다면 역시 하위 스레드는 읽기 락 요청을 했으므로 블럭되버리고, 이 하위 스레드들을 시작한 스레드도 블록된 하위 스레드의 작업을 기다리느라 블럭된다.
 * 또한 쓰기 락 요청을 한 스레드 또한 작업을 맡긴 읽기 락 소유 스레드가 블럭되었으므로 블럭되고, 데드락 상황에 도달한다.
 * 위의 동작 방식에서의 5번 상황인 것이다.
 *
 * 이 상황은 6번 상황과는 다르다. 작업을 분담받은 하위 스레드들은 읽기 락을 가진 주 스레드와는 "다른" 스레드이기 때문에, "재진입"을 하는 상황이 아니기 때문이다.
 * 이 문제를 해결하기 위해서 내가 택한 방법은 읽기 락을 가지고 있는 주 스레드, 그리고 그 주 스레드의 하위 스레드를 구분해서 처리하는 것이었다.
 * 일단 하위 스레드는 굳이 자신 소유의 읽기 락이 없더라도 하위 스레드의 작업 완료를 기다리는 주 스레드가 읽기 락을 가진 채로 블럭되어 있다.
 * 이렇게 되면 읽기 락이 여전히 걸려 있는 상황이므로 쓰기 락을 필요로 하는 쓰기 작업에 대해서는 여전히 보호받을 수 있을 것이다.
 * 즉, 하위 스레드는 락이 없어도 하위 스레드가 실행되는 동안은 항상 락을 가지고 있는 주 스레드가 대기하고 있어 읽기 락이 유지된다는 점을 이용하기로 했다.
 *
 * 먼저 SubThread라는 Thread의 하위 클래스를 생성하고, 하위 스레드들은 이 SubThread 객체로 생성되도록 만들었다.
 * 그리고 만일 읽기 락 요청이 들어왔을 경우, 호출한 스레드가 SubThread 객체인지 판별했다.
 * 그리고 만일 SubThread 객체라면 하위 스레드이므로 이미 그 부모 스레드가 락을 가지고 있으므로 lock()을 ReentrantReadWriteLock 객체에 전달하지 않는다.
 * 이렇게 되면 읽기 락을 가진 스레드가 하위 스레드들을 만들어 작업을 분담할 때 5번 상황에 걸리는 일이 없어진다.
 * 따라서 이 경우의 데드락을 해소할 수 있었다.
 *
 * 이렇게 변형된 이 클래스의 락 행동 양식은 다음과 같다.
 * 1. 쓰기 락은 읽기 락으로 격하할 수 있다. 그 역은 불가능하다.
 * 2. 모든 락은 소유 스레드의 경우 재진입이 가능하다.
 * 3. 읽기 락은 공유된다. 한 스레드가 읽기 락을 가지고 있으면 다른 스레드도 읽기 락을 가질 수 있다.
 * 4. 쓰기 락은 동시에 단 한 스레드만이 가질 수 있다. 쓰기 락은 읽기 락과 동시에 걸릴 수 없다.
 * 5. 대기열에 쓰기 락 요청이 있다면, 먼저 요청된 스레드라도 읽기 락 요청은 블럭된다.
 *    단, 읽기 락을 이미 가진 스레드가 시작하는 하위 스레드는 부모 스레드처럼 취급되어 이 경우에도 블럭되지 않는다.
 *    그리고 이 때 하위 스레드가 살아있는 시간 동안은 반드시 부모 스레드가 읽기 락을 가진 채로 살아있어야 한다.
 * 6. 대기열에 쓰기 락 요청이 있어도, 읽기 락의 소유 스레드의 경우에는 재진입이 허용된다.
 *
 * *** 의도 ***
 * Atomic~ 자료형은 있지만, 이런 것들은 그 자료형 객체 하나만의 Atomic을 보장할 뿐, 특정 코드 블럭 전체의 Atomic을 보장하지는 못한다.
 * 이러한 블럭 단위 Atomic을 보장하는 것이 이 클래스의 핵심 목적이다.
 *
 * *** 사용 방법 ***
 * Atomic하게 만들고 싶은 특정 블럭 단위의 시작과 끝에서 락을 걸고 해제해주면 된다. 이렇게 Atomic해진 블럭을 다른 Atomic 블럭 내에서 중첩해서 호출해도 된다.
 * 단, 읽기 락으로 Atomic한 블럭 내에서 쓰기 락으로 보호받는 블럭을 호출하면 얄짤없이 블럭된다.
 *
 * *** 주의 ***
 * 특정 코드 블럭이 락을 걸었다면, 그 블럭을 탈출하는 모든 지점에서는 반드시 락을 해제해주어야 한다. 그렇지 않을 경우 필연적으로 데드락으로 이어진다.
 *
 */

public class LockWrapper {
    private ReentrantReadWriteLock lock;
    private ActionMain actionMain;
    private ReentrantReadWriteLock.ReadLock read_lock;
    private ReentrantReadWriteLock.WriteLock write_lock;
    private ReadLockWrapper read_lock_wrapper;
    private WriteLockWrapper write_lock_wrapper;
    private final HashMap<Thread, Integer> thread_read_lock_map;
    private int log_verbose_level;

    public LockWrapper(ActionMain actionMain) {
        this.actionMain = actionMain;
        this.lock = new ReentrantReadWriteLock();
        read_lock = lock.readLock();
        write_lock = lock.writeLock();
        read_lock_wrapper = new ReadLockWrapper();
        write_lock_wrapper = new WriteLockWrapper();
        thread_read_lock_map = new HashMap<>(AACGroupContainerPreferences.LOCK_WRAPPER_STACK_INIT_SIZE);
        log_verbose_level = AACGroupContainerPreferences.LOCK_WRAPPER_LOG_VERBOSE_LEVEL;
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    /*
     * 쓰기 락 객체의 외부 노출 Wrapper.
     * 외부에서는 단순히 lock(), unlock()만 이용하면 된다.
     * 만일 쓰기 락 객체 자체에 접근하고 싶다면 getLock()을 이용하면 되나, 추천하지는 않는다.
     * 이 Wrapper는 읽기 락 Wrapper에 비해 하는 일이 거의 없다.
     * 읽기 락과 달리 중간 단계가 없이 바로 ReentrantReadWriteLock.WriteLock에 그 요청이 전달된다.
     */
    public class WriteLockWrapper {
        /*
         * 쓰기 락을 요청하는 메소드.
         */
        public void lock() {
            log(1, null, "Trying to get write lock... " + Thread.currentThread().toString());
            write_lock.lock();
            log_with_lock_stat(1, null, "Write lock acquired.", Thread.currentThread(), lock.getWriteHoldCount(), get_read_hold_count(), lock.getReadLockCount());
        }

        /*
         * 쓰기 락을 해제 요청하는 메소드.
         */
        public void unlock() {
            write_lock.unlock();
            log_with_lock_stat(1, null, "Write lock released.", Thread.currentThread(), lock.getWriteHoldCount(), get_read_hold_count(), lock.getReadLockCount());

            if (write_lock.getHoldCount() == 0) {
                actionMain.activate_morpheme_analyzer();
            }
        }

        public Lock getLock() {
            return write_lock;
        }
    }

    /*
     * 읽기 락 객체의 외부 노출 Wrapper.
     * 외부에서는 단순히 lock(), unlock()만 이용하면 된다.
     * 만일 읽기 락 객체 자체에 접근하고 싶다면 getLock()을 이용하면 되나, 추천하지는 않는다.
     */
    public class ReadLockWrapper {
        /*
         * 읽기 락을 요청하는 메소드.
         * 여기서 읽기 락을 요청한다고 반드시 ReentrantReadWriteLock으로 그 요청이 전달되지는 않는다.
         */
        public void lock() {
            log(1, null, "Trying to get read lock... " + Thread.currentThread().toString());
            // 먼저 현재 스레드가 하위 스레드인지 확인한다.
            if (SubThread.class.isAssignableFrom(Thread.currentThread().getClass())) {
                // 하위 스레드가 맞다면...
                SubThread sub_thread = (SubThread)Thread.currentThread();
                // 현재 읽기 락 카운트를 점검한다. 그런데 현재 읽기 락 카운트를 점검해 현재 읽기 락이 아무 것도 걸려있지 않은지 확인한다. 만일 그렇다면 상태 이상으로, 예외 처리한다.
                if (lock.getReadLockCount() == 0) throw new IllegalStateException("Sub-thread is calling the method, but no read lock is done yet. This only can see ");
                log(2, null, "Sub-thread read lock request denied. It will protected by its parent " + sub_thread.parent_thread.toString() + "'s lock.");
                // 하위 스레드는 부모 스레드에 의해 보호받으면 된다. 따라서 ReentrantReadWriteLock.ReadLock 객체에 lock() 메소드를 호출하지 않는다.
                return;
            }

            // 하위 스레드가 아니라면...
            if (lock.getReadHoldCount() <= 0) read_lock.lock(); // 현재 이 스레드가 읽기 락을 소유하고 있지 않다. 그렇다면 lock() 메소드를 호출해 읽기 락을 요청한다.
            // 이 스레드를 thread_read_lock_map 해시맵에 등록하고, 이 스레드의 읽기 락 중첩 횟수를 기록한다.
            int hold_count = increase_read_lock_map();
            log_with_lock_stat(1, null, "Read lock acquired.", Thread.currentThread(), lock.getWriteHoldCount(), hold_count, lock.getReadLockCount());
        }

        /*
         * 읽기 락을 해제 요청하는 메소드.
         * 여기서 읽기 락을 해제 요청한다고 반드시 ReentrantReadWriteLock.ReadLock 객체로 그 요청이 전달되지는 않는다.
         */
        public void unlock() {
            // 먼저 thread_read_lock_map 해시맵에서 이 스레드의 매핑을 찾아 락 카운트를 감소시킨다.
            int hold_count = decrease_read_lock_map();
            if (hold_count == -1) return; // 이 스레드는 읽기 락을 가진 적이 없다. 그렇다면 추가 작업 없이 그냥 종료한다.
            if (hold_count == 0) read_lock.unlock(); // 이 스레드의 읽기 홀드 카운트가 0이 되므로 락을 해제해야 한다. ReentrantReadWriteLock.ReadLock 객체에 그 요청을 전달한다.
            log_with_lock_stat(1, null, "Read lock released.", Thread.currentThread(), lock.getWriteHoldCount(), hold_count, lock.getReadLockCount());
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

    /*
     * 디버그를 위한 로그를 출력한다. ActionMain.log()와는 다른 점은 stacktrace[4]를 출력한다는 점이다.
     * stacktrace[3]은 이 log()를 호출한 Wrapper 메소드이기 때문에, 스택을 한 단계 더 내려봐야 하기 떄문이다.
     */
    @SuppressWarnings("PointlessBooleanExpression")
    public void log(int verbose_level, @Nullable String prefix, @NonNull String text) {
        if (log_verbose_level < verbose_level) return;
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4]; // 4번째 스택이 바로 lock/unlock을 호출한 스택이다.
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

    /*
     * 데드락 발생을 추적하고, 데드락 발생시 분석을 위한 로그를 출력한다.
     */
    @SuppressWarnings("PointlessBooleanExpression")
    public void log_with_lock_stat(int verbose_level, @Nullable String prefix, @NonNull String text, Thread thread, int write_hold_count, int read_hold_count, int read_lock_count) {
        if (log_verbose_level < verbose_level) return;
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[4]; // 4번째 스택이 바로 lock/unlock을 호출한 스택이다.
        String className = e.getClassName();

        StringBuilder sb = new StringBuilder(100); // 대충 어림잡은 크기이다.
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

    /*
     * writer lock이 큐에 있을 경우 reader lock을 걸려고 할 때 블록이 되는 문제를 해결하기 위한 우회책으로 만들어진 메소드들이다.
     * 또한 객체-홀드 카운트 해시맵은 synchronized 키워드로 보호된다.
     */

    /*
     * 이 메소드를 호출한 스레드를 맵에 등록하며, 만일 이미 스레드가 등록되어 있을 경우 홀드 카운트를 올린다.
     */
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

    /*
     * 이 메소드를 호출한 스레드의 홀드 카운트를 맵에서 1회 감소시킨다. 만일 해당 스레드가 이 맵에 없다면 -1 값을 반환한다.
     * 홀드 카운트가 0이 될 경우 해당 스레드의 매핑은 맵에서 제거된다.
     */
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

    /*
     * 호출한 스레드의 현재 reader_lock 중첩수를 반환한다.
     */
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