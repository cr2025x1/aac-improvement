package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 8/8/15.
 *
 * LockWrapper 클래스에서 하위스레드를 구분할 필요성이 생겼기에, 스레드 객체를 구분하기 위해 만든,
 * Thread 클래스의 하위 클래스이다.
 *
 * 큰 차이는 없으며, 단지 자신의 부모 클래스에 대한 레퍼런스가 추가된 것이 전부이다.
 * Java 7 기준 모든 생성자들에 대해 인수 목록 중 맨 앞에 부모 스레드가 추가되었다.
 */
@SuppressWarnings("unused")
public class SubThread extends Thread {
    Thread parent_thread = null; // 물론 추가한 변수이기는 하지만, 꼭 쓸 필요는 없다.

    public SubThread(Thread parent_thread) {
        super();
        this.parent_thread = parent_thread;
    }

    public SubThread(Thread parent_thread, Runnable target) {
        super(target);
        this.parent_thread = parent_thread;
    }

    public SubThread(Thread parent_thread, Runnable target, String name) {
        super(target, name);
        this.parent_thread = parent_thread;
    }

    public SubThread(Thread parent_thread, String name) {
        super(name);
        this.parent_thread = parent_thread;
    }

    public SubThread(Thread parent_thread, ThreadGroup group, Runnable target) {
        super(group, target);
        this.parent_thread = parent_thread;
    }

    public SubThread(Thread parent_thread, ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
        this.parent_thread = parent_thread;
    }

    public SubThread(Thread parent_thread, ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
        this.parent_thread = parent_thread;
    }

    public SubThread(Thread parent_thread, ThreadGroup group, String name) {
        super(group, name);
        this.parent_thread = parent_thread;
    }
}