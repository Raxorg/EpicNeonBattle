package com.badlogic.gdx.utils;

public class PauseableThread extends Thread {
    boolean exit = false;
    boolean paused = false;
    final Runnable runnable;

    public PauseableThread(Runnable runnable) {
        this.runnable = runnable;
    }

    public void run() {
        while (true) {
            synchronized (this) {
                while (this.paused) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!this.exit) {
                this.runnable.run();
            } else {
                return;
            }
        }
    }

    public void onPause() {
        this.paused = true;
    }

    public void onResume() {
        synchronized (this) {
            this.paused = false;
            notifyAll();
        }
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void stopThread() {
        this.exit = true;
        if (this.paused) {
            onResume();
        }
    }
}
