package com.badlogic.gdx.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;

public class Timer {
    private static final int CANCELLED = -1;
    private static final int FOREVER = -2;
    static Timer instance = new Timer();
    static final Array<Timer> instances = new Array(1);
    static TimerThread thread;
    private final Array<Task> tasks = new Array(false, 8);

    public static abstract class Task implements Runnable {
        long executeTimeMillis;
        long intervalMillis;
        int repeatCount = Timer.CANCELLED;

        public abstract void run();

        public void cancel() {
            this.executeTimeMillis = 0;
            this.repeatCount = Timer.CANCELLED;
        }

        public boolean isScheduled() {
            return this.repeatCount != Timer.CANCELLED;
        }

        public long getExecuteTimeMillis() {
            return this.executeTimeMillis;
        }
    }

    static class TimerThread implements LifecycleListener, Runnable {
        Application app;
        private long pauseMillis;

        public TimerThread() {
            Gdx.app.addLifecycleListener(this);
            resume();
        }

        public void run() {
            while (true) {
                synchronized (Timer.instances) {
                    if (this.app != Gdx.app) {
                        return;
                    }
                    long timeMillis = System.nanoTime() / 1000000;
                    long waitMillis = 5000;
                    int i = 0;
                    int n = Timer.instances.size;
                    while (i < n) {
                        try {
                            waitMillis = ((Timer) Timer.instances.get(i)).update(timeMillis, waitMillis);
                            i++;
                        } catch (Throwable ex) {
                            GdxRuntimeException gdxRuntimeException = new GdxRuntimeException("Task failed: " + ((Timer) Timer.instances.get(i)).getClass().getName(), ex);
                        }
                    }
                    if (this.app != Gdx.app) {
                        return;
                    } else if (waitMillis > 0) {
                        try {
                            Timer.instances.wait(waitMillis);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }

        public void resume() {
            long delayMillis = (System.nanoTime() / 1000000) - this.pauseMillis;
            synchronized (Timer.instances) {
                int n = Timer.instances.size;
                for (int i = 0; i < n; i++) {
                    ((Timer) Timer.instances.get(i)).delay(delayMillis);
                }
            }
            this.app = Gdx.app;
            Thread t = new Thread(this, "Timer");
            t.setDaemon(true);
            t.start();
            Timer.thread = this;
        }

        public void pause() {
            this.pauseMillis = System.nanoTime() / 1000000;
            synchronized (Timer.instances) {
                this.app = null;
                Timer.wake();
            }
            Timer.thread = null;
        }

        public void dispose() {
            pause();
            Gdx.app.removeLifecycleListener(this);
            Timer.instances.clear();
            Timer.instance = null;
        }
    }

    public static Timer instance() {
        if (instance == null) {
            instance = new Timer();
        }
        return instance;
    }

    public Timer() {
        start();
    }

    public Task postTask(Task task) {
        return scheduleTask(task, 0.0f, 0.0f, 0);
    }

    public Task scheduleTask(Task task, float delaySeconds) {
        return scheduleTask(task, delaySeconds, 0.0f, 0);
    }

    public Task scheduleTask(Task task, float delaySeconds, float intervalSeconds) {
        return scheduleTask(task, delaySeconds, intervalSeconds, FOREVER);
    }

    public Task scheduleTask(Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
        if (task.repeatCount != CANCELLED) {
            throw new IllegalArgumentException("The same task may not be scheduled twice.");
        }
        task.executeTimeMillis = (System.nanoTime() / 1000000) + ((long) (delaySeconds * 1000.0f));
        task.intervalMillis = (long) (intervalSeconds * 1000.0f);
        task.repeatCount = repeatCount;
        synchronized (this.tasks) {
            this.tasks.add(task);
        }
        wake();
        return task;
    }

    public void stop() {
        synchronized (instances) {
            instances.removeValue(this, true);
        }
    }

    public void start() {
        synchronized (instances) {
            if (instances.contains(this, true)) {
                return;
            }
            instances.add(this);
            if (thread == null) {
                thread = new TimerThread();
            }
            wake();
        }
    }

    public void clear() {
        synchronized (this.tasks) {
            int n = this.tasks.size;
            for (int i = 0; i < n; i++) {
                ((Task) this.tasks.get(i)).cancel();
            }
            this.tasks.clear();
        }
    }

    long update(long timeMillis, long waitMillis) {
        synchronized (this.tasks) {
            int i = 0;
            int n = this.tasks.size;
            while (i < n) {
                Task task = (Task) this.tasks.get(i);
                if (task.executeTimeMillis > timeMillis) {
                    waitMillis = Math.min(waitMillis, task.executeTimeMillis - timeMillis);
                } else {
                    if (task.repeatCount != CANCELLED) {
                        if (task.repeatCount == 0) {
                            task.repeatCount = CANCELLED;
                        }
                        Gdx.app.postRunnable(task);
                    }
                    if (task.repeatCount == CANCELLED) {
                        this.tasks.removeIndex(i);
                        i += CANCELLED;
                        n += CANCELLED;
                    } else {
                        task.executeTimeMillis = task.intervalMillis + timeMillis;
                        waitMillis = Math.min(waitMillis, task.intervalMillis);
                        if (task.repeatCount > 0) {
                            task.repeatCount += CANCELLED;
                        }
                    }
                }
                i++;
            }
        }
        return waitMillis;
    }

    public void delay(long delayMillis) {
        synchronized (this.tasks) {
            int n = this.tasks.size;
            for (int i = 0; i < n; i++) {
                Task task = (Task) this.tasks.get(i);
                task.executeTimeMillis += delayMillis;
            }
        }
    }

    static void wake() {
        synchronized (instances) {
            instances.notifyAll();
        }
    }

    public static Task post(Task task) {
        return instance().postTask(task);
    }

    public static Task schedule(Task task, float delaySeconds) {
        return instance().scheduleTask(task, delaySeconds);
    }

    public static Task schedule(Task task, float delaySeconds, float intervalSeconds) {
        return instance().scheduleTask(task, delaySeconds, intervalSeconds);
    }

    public static Task schedule(Task task, float delaySeconds, float intervalSeconds, int repeatCount) {
        return instance().scheduleTask(task, delaySeconds, intervalSeconds, repeatCount);
    }
}
