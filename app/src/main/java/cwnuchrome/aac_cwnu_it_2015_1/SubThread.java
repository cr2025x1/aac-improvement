package cwnuchrome.aac_cwnu_it_2015_1;

/**
 * Created by Chrome on 8/8/15.
 */
public class SubThread extends Thread {
    Thread parent_thread = null;

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