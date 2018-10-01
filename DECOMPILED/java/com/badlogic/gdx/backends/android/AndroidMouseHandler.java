package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.Gdx;
import com.epicness.neonbattle.android.BuildConfig;

public class AndroidMouseHandler {
    private int deltaX = 0;
    private int deltaY = 0;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onGenericMotion(android.view.MotionEvent r20, com.badlogic.gdx.backends.android.AndroidInput r21) {
        /*
        r19 = this;
        r2 = r20.getSource();
        r2 = r2 & 2;
        if (r2 != 0) goto L_0x000a;
    L_0x0008:
        r2 = 0;
    L_0x0009:
        return r2;
    L_0x000a:
        r2 = r20.getAction();
        r0 = r2 & 255;
        r18 = r0;
        r5 = 0;
        r6 = 0;
        r15 = 0;
        r8 = java.lang.System.nanoTime();
        monitor-enter(r21);
        switch(r18) {
            case 7: goto L_0x0029;
            case 8: goto L_0x0054;
            default: goto L_0x001d;
        };
    L_0x001d:
        monitor-exit(r21);	 Catch:{ all -> 0x0051 }
        r2 = com.badlogic.gdx.Gdx.app;
        r2 = r2.getGraphics();
        r2.requestRendering();
        r2 = 1;
        goto L_0x0009;
    L_0x0029:
        r2 = r20.getX();	 Catch:{ all -> 0x0051 }
        r5 = (int) r2;	 Catch:{ all -> 0x0051 }
        r2 = r20.getY();	 Catch:{ all -> 0x0051 }
        r6 = (int) r2;	 Catch:{ all -> 0x0051 }
        r0 = r19;
        r2 = r0.deltaX;	 Catch:{ all -> 0x0051 }
        if (r5 != r2) goto L_0x003f;
    L_0x0039:
        r0 = r19;
        r2 = r0.deltaY;	 Catch:{ all -> 0x0051 }
        if (r6 == r2) goto L_0x001d;
    L_0x003f:
        r4 = 4;
        r7 = 0;
        r2 = r19;
        r3 = r21;
        r2.postTouchEvent(r3, r4, r5, r6, r7, r8);	 Catch:{ all -> 0x0051 }
        r0 = r19;
        r0.deltaX = r5;	 Catch:{ all -> 0x0051 }
        r0 = r19;
        r0.deltaY = r6;	 Catch:{ all -> 0x0051 }
        goto L_0x001d;
    L_0x0051:
        r2 = move-exception;
        monitor-exit(r21);	 Catch:{ all -> 0x0051 }
        throw r2;
    L_0x0054:
        r2 = 9;
        r0 = r20;
        r2 = r0.getAxisValue(r2);	 Catch:{ all -> 0x0051 }
        r2 = java.lang.Math.signum(r2);	 Catch:{ all -> 0x0051 }
        r2 = -r2;
        r15 = (int) r2;	 Catch:{ all -> 0x0051 }
        r12 = 3;
        r13 = 0;
        r14 = 0;
        r10 = r19;
        r11 = r21;
        r16 = r8;
        r10.postTouchEvent(r11, r12, r13, r14, r15, r16);	 Catch:{ all -> 0x0051 }
        goto L_0x001d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.backends.android.AndroidMouseHandler.onGenericMotion(android.view.MotionEvent, com.badlogic.gdx.backends.android.AndroidInput):boolean");
    }

    private void logAction(int action) {
        String actionStr = BuildConfig.VERSION_NAME;
        if (action == 9) {
            actionStr = "HOVER_ENTER";
        } else if (action == 7) {
            actionStr = "HOVER_MOVE";
        } else if (action == 10) {
            actionStr = "HOVER_EXIT";
        } else if (action == 8) {
            actionStr = "SCROLL";
        } else {
            actionStr = "UNKNOWN (" + action + ")";
        }
        Gdx.app.log("AndroidMouseHandler", "action " + actionStr);
    }

    private void postTouchEvent(AndroidInput input, int type, int x, int y, int scrollAmount, long timeStamp) {
        TouchEvent event = (TouchEvent) input.usedTouchEvents.obtain();
        event.timeStamp = timeStamp;
        event.x = x;
        event.y = y;
        event.type = type;
        event.scrollAmount = scrollAmount;
        input.touchEvents.add(event);
    }
}
