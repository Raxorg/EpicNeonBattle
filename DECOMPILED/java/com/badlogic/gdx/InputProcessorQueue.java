package com.badlogic.gdx;

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.TimeUtils;

public class InputProcessorQueue implements InputProcessor {
    private static final int KEY_DOWN = 0;
    private static final int KEY_TYPED = 2;
    private static final int KEY_UP = 1;
    private static final int MOUSE_MOVED = 6;
    private static final int SCROLLED = 7;
    private static final int TOUCH_DOWN = 3;
    private static final int TOUCH_DRAGGED = 5;
    private static final int TOUCH_UP = 4;
    private long currentEventTime;
    private final IntArray processingQueue = new IntArray();
    private InputProcessor processor;
    private final IntArray queue = new IntArray();

    public InputProcessorQueue(InputProcessor processor) {
        this.processor = processor;
    }

    public void setProcessor(InputProcessor processor) {
        this.processor = processor;
    }

    public InputProcessor getProcessor() {
        return this.processor;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void drain() {
        /*
        r10 = this;
        r3 = r10.processingQueue;
        monitor-enter(r10);
        r4 = r10.processor;	 Catch:{ all -> 0x0044 }
        if (r4 != 0) goto L_0x000e;
    L_0x0007:
        r4 = r10.queue;	 Catch:{ all -> 0x0044 }
        r4.clear();	 Catch:{ all -> 0x0044 }
        monitor-exit(r10);	 Catch:{ all -> 0x0044 }
    L_0x000d:
        return;
    L_0x000e:
        r4 = r10.queue;	 Catch:{ all -> 0x0044 }
        r3.addAll(r4);	 Catch:{ all -> 0x0044 }
        r4 = r10.queue;	 Catch:{ all -> 0x0044 }
        r4.clear();	 Catch:{ all -> 0x0044 }
        monitor-exit(r10);	 Catch:{ all -> 0x0044 }
        r0 = 0;
        r2 = r3.size;
        r1 = r0;
    L_0x001d:
        if (r1 >= r2) goto L_0x00e6;
    L_0x001f:
        r0 = r1 + 1;
        r4 = r3.get(r1);
        r4 = (long) r4;
        r6 = 32;
        r4 = r4 << r6;
        r1 = r0 + 1;
        r6 = r3.get(r0);
        r6 = (long) r6;
        r8 = 4294967295; // 0xffffffff float:NaN double:2.1219957905E-314;
        r6 = r6 & r8;
        r4 = r4 | r6;
        r10.currentEventTime = r4;
        r0 = r1 + 1;
        r4 = r3.get(r1);
        switch(r4) {
            case 0: goto L_0x0047;
            case 1: goto L_0x0054;
            case 2: goto L_0x0061;
            case 3: goto L_0x006f;
            case 4: goto L_0x008d;
            case 5: goto L_0x00ab;
            case 6: goto L_0x00c5;
            case 7: goto L_0x00d8;
            default: goto L_0x0042;
        };
    L_0x0042:
        r1 = r0;
        goto L_0x001d;
    L_0x0044:
        r4 = move-exception;
        monitor-exit(r10);	 Catch:{ all -> 0x0044 }
        throw r4;
    L_0x0047:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r4.keyDown(r5);
        r0 = r1;
        goto L_0x0042;
    L_0x0054:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r4.keyUp(r5);
        r0 = r1;
        goto L_0x0042;
    L_0x0061:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r5 = (char) r5;
        r4.keyTyped(r5);
        r0 = r1;
        goto L_0x0042;
    L_0x006f:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r0 = r1 + 1;
        r6 = r3.get(r1);
        r1 = r0 + 1;
        r7 = r3.get(r0);
        r0 = r1 + 1;
        r8 = r3.get(r1);
        r4.touchDown(r5, r6, r7, r8);
        goto L_0x0042;
    L_0x008d:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r0 = r1 + 1;
        r6 = r3.get(r1);
        r1 = r0 + 1;
        r7 = r3.get(r0);
        r0 = r1 + 1;
        r8 = r3.get(r1);
        r4.touchUp(r5, r6, r7, r8);
        goto L_0x0042;
    L_0x00ab:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r0 = r1 + 1;
        r6 = r3.get(r1);
        r1 = r0 + 1;
        r7 = r3.get(r0);
        r4.touchDragged(r5, r6, r7);
        r0 = r1;
        goto L_0x0042;
    L_0x00c5:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r0 = r1 + 1;
        r6 = r3.get(r1);
        r4.mouseMoved(r5, r6);
        goto L_0x0042;
    L_0x00d8:
        r4 = r10.processor;
        r1 = r0 + 1;
        r5 = r3.get(r0);
        r4.scrolled(r5);
        r0 = r1;
        goto L_0x0042;
    L_0x00e6:
        r3.clear();
        goto L_0x000d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.InputProcessorQueue.drain():void");
    }

    private void queueTime() {
        long time = TimeUtils.nanoTime();
        this.queue.add((int) (time >> 32));
        this.queue.add((int) time);
    }

    public synchronized boolean keyDown(int keycode) {
        queueTime();
        this.queue.add(KEY_DOWN);
        this.queue.add(keycode);
        return false;
    }

    public synchronized boolean keyUp(int keycode) {
        queueTime();
        this.queue.add(KEY_UP);
        this.queue.add(keycode);
        return false;
    }

    public synchronized boolean keyTyped(char character) {
        queueTime();
        this.queue.add(KEY_TYPED);
        this.queue.add(character);
        return false;
    }

    public synchronized boolean touchDown(int screenX, int screenY, int pointer, int button) {
        queueTime();
        this.queue.add(TOUCH_DOWN);
        this.queue.add(screenX);
        this.queue.add(screenY);
        this.queue.add(pointer);
        this.queue.add(button);
        return false;
    }

    public synchronized boolean touchUp(int screenX, int screenY, int pointer, int button) {
        queueTime();
        this.queue.add(TOUCH_UP);
        this.queue.add(screenX);
        this.queue.add(screenY);
        this.queue.add(pointer);
        this.queue.add(button);
        return false;
    }

    public synchronized boolean touchDragged(int screenX, int screenY, int pointer) {
        queueTime();
        this.queue.add(TOUCH_DRAGGED);
        this.queue.add(screenX);
        this.queue.add(screenY);
        this.queue.add(pointer);
        return false;
    }

    public synchronized boolean mouseMoved(int screenX, int screenY) {
        queueTime();
        this.queue.add(MOUSE_MOVED);
        this.queue.add(screenX);
        this.queue.add(screenY);
        return false;
    }

    public synchronized boolean scrolled(int amount) {
        queueTime();
        this.queue.add(SCROLLED);
        this.queue.add(amount);
        return false;
    }

    public long getCurrentEventTime() {
        return this.currentEventTime;
    }
}
