package com.badlogic.gdx.backends.android;

import android.content.Context;
import com.badlogic.gdx.Gdx;
import com.epicness.neonbattle.android.BuildConfig;

public class AndroidMultiTouchHandler implements AndroidTouchHandler {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onTouch(android.view.MotionEvent r26, com.badlogic.gdx.backends.android.AndroidInput r27) {
        /*
        r25 = this;
        r3 = r26.getAction();
        r2 = r3 & 255;
        r3 = r26.getAction();
        r4 = 65280; // 0xff00 float:9.1477E-41 double:3.22526E-319;
        r3 = r3 & r4;
        r24 = r3 >> 8;
        r0 = r26;
        r1 = r24;
        r23 = r0.getPointerId(r1);
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r10 = java.lang.System.nanoTime();
        monitor-enter(r27);
        switch(r2) {
            case 0: goto L_0x002f;
            case 1: goto L_0x009a;
            case 2: goto L_0x00fd;
            case 3: goto L_0x009a;
            case 4: goto L_0x009a;
            case 5: goto L_0x002f;
            case 6: goto L_0x009a;
            default: goto L_0x0024;
        };
    L_0x0024:
        monitor-exit(r27);	 Catch:{ all -> 0x0095 }
        r3 = com.badlogic.gdx.Gdx.app;
        r3 = r3.getGraphics();
        r3.requestRendering();
        return;
    L_0x002f:
        r8 = r27.getFreePointerIndex();	 Catch:{ all -> 0x0095 }
        r3 = 20;
        if (r8 >= r3) goto L_0x0024;
    L_0x0037:
        r0 = r27;
        r3 = r0.realId;	 Catch:{ all -> 0x0095 }
        r3[r8] = r23;	 Catch:{ all -> 0x0095 }
        r0 = r26;
        r1 = r24;
        r3 = r0.getX(r1);	 Catch:{ all -> 0x0095 }
        r6 = (int) r3;	 Catch:{ all -> 0x0095 }
        r0 = r26;
        r1 = r24;
        r3 = r0.getY(r1);	 Catch:{ all -> 0x0095 }
        r7 = (int) r3;	 Catch:{ all -> 0x0095 }
        r3 = android.os.Build.VERSION.SDK_INT;	 Catch:{ all -> 0x0095 }
        r4 = 14;
        if (r3 < r4) goto L_0x005f;
    L_0x0055:
        r3 = r26.getButtonState();	 Catch:{ all -> 0x0095 }
        r0 = r25;
        r9 = r0.toGdxButton(r3);	 Catch:{ all -> 0x0095 }
    L_0x005f:
        r3 = -1;
        if (r9 == r3) goto L_0x006a;
    L_0x0062:
        r5 = 0;
        r3 = r25;
        r4 = r27;
        r3.postTouchEvent(r4, r5, r6, r7, r8, r9, r10);	 Catch:{ all -> 0x0095 }
    L_0x006a:
        r0 = r27;
        r3 = r0.touchX;	 Catch:{ all -> 0x0095 }
        r3[r8] = r6;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.touchY;	 Catch:{ all -> 0x0095 }
        r3[r8] = r7;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.deltaX;	 Catch:{ all -> 0x0095 }
        r4 = 0;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.deltaY;	 Catch:{ all -> 0x0095 }
        r4 = 0;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r4 = r0.touched;	 Catch:{ all -> 0x0095 }
        r3 = -1;
        if (r9 == r3) goto L_0x0098;
    L_0x008b:
        r3 = 1;
    L_0x008c:
        r4[r8] = r3;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.button;	 Catch:{ all -> 0x0095 }
        r3[r8] = r9;	 Catch:{ all -> 0x0095 }
        goto L_0x0024;
    L_0x0095:
        r3 = move-exception;
        monitor-exit(r27);	 Catch:{ all -> 0x0095 }
        throw r3;
    L_0x0098:
        r3 = 0;
        goto L_0x008c;
    L_0x009a:
        r0 = r27;
        r1 = r23;
        r8 = r0.lookUpPointerIndex(r1);	 Catch:{ all -> 0x0095 }
        r3 = -1;
        if (r8 == r3) goto L_0x0024;
    L_0x00a5:
        r3 = 20;
        if (r8 >= r3) goto L_0x0024;
    L_0x00a9:
        r0 = r27;
        r3 = r0.realId;	 Catch:{ all -> 0x0095 }
        r4 = -1;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r26;
        r1 = r24;
        r3 = r0.getX(r1);	 Catch:{ all -> 0x0095 }
        r6 = (int) r3;	 Catch:{ all -> 0x0095 }
        r0 = r26;
        r1 = r24;
        r3 = r0.getY(r1);	 Catch:{ all -> 0x0095 }
        r7 = (int) r3;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.button;	 Catch:{ all -> 0x0095 }
        r9 = r3[r8];	 Catch:{ all -> 0x0095 }
        r3 = -1;
        if (r9 == r3) goto L_0x00d3;
    L_0x00cb:
        r5 = 1;
        r3 = r25;
        r4 = r27;
        r3.postTouchEvent(r4, r5, r6, r7, r8, r9, r10);	 Catch:{ all -> 0x0095 }
    L_0x00d3:
        r0 = r27;
        r3 = r0.touchX;	 Catch:{ all -> 0x0095 }
        r3[r8] = r6;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.touchY;	 Catch:{ all -> 0x0095 }
        r3[r8] = r7;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.deltaX;	 Catch:{ all -> 0x0095 }
        r4 = 0;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.deltaY;	 Catch:{ all -> 0x0095 }
        r4 = 0;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.touched;	 Catch:{ all -> 0x0095 }
        r4 = 0;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.button;	 Catch:{ all -> 0x0095 }
        r4 = 0;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        goto L_0x0024;
    L_0x00fd:
        r22 = r26.getPointerCount();	 Catch:{ all -> 0x0095 }
        r12 = 0;
    L_0x0102:
        r0 = r22;
        if (r12 >= r0) goto L_0x0024;
    L_0x0106:
        r24 = r12;
        r0 = r26;
        r1 = r24;
        r23 = r0.getPointerId(r1);	 Catch:{ all -> 0x0095 }
        r0 = r26;
        r1 = r24;
        r3 = r0.getX(r1);	 Catch:{ all -> 0x0095 }
        r6 = (int) r3;	 Catch:{ all -> 0x0095 }
        r0 = r26;
        r1 = r24;
        r3 = r0.getY(r1);	 Catch:{ all -> 0x0095 }
        r7 = (int) r3;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r1 = r23;
        r8 = r0.lookUpPointerIndex(r1);	 Catch:{ all -> 0x0095 }
        r3 = -1;
        if (r8 != r3) goto L_0x0130;
    L_0x012d:
        r12 = r12 + 1;
        goto L_0x0102;
    L_0x0130:
        r3 = 20;
        if (r8 >= r3) goto L_0x0024;
    L_0x0134:
        r0 = r27;
        r3 = r0.button;	 Catch:{ all -> 0x0095 }
        r9 = r3[r8];	 Catch:{ all -> 0x0095 }
        r3 = -1;
        if (r9 == r3) goto L_0x016e;
    L_0x013d:
        r5 = 2;
        r3 = r25;
        r4 = r27;
        r3.postTouchEvent(r4, r5, r6, r7, r8, r9, r10);	 Catch:{ all -> 0x0095 }
    L_0x0145:
        r0 = r27;
        r3 = r0.deltaX;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r4 = r0.touchX;	 Catch:{ all -> 0x0095 }
        r4 = r4[r8];	 Catch:{ all -> 0x0095 }
        r4 = r6 - r4;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.deltaY;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r4 = r0.touchY;	 Catch:{ all -> 0x0095 }
        r4 = r4[r8];	 Catch:{ all -> 0x0095 }
        r4 = r7 - r4;
        r3[r8] = r4;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.touchX;	 Catch:{ all -> 0x0095 }
        r3[r8] = r6;	 Catch:{ all -> 0x0095 }
        r0 = r27;
        r3 = r0.touchY;	 Catch:{ all -> 0x0095 }
        r3[r8] = r7;	 Catch:{ all -> 0x0095 }
        goto L_0x012d;
    L_0x016e:
        r15 = 4;
        r19 = 0;
        r13 = r25;
        r14 = r27;
        r16 = r6;
        r17 = r7;
        r18 = r8;
        r20 = r10;
        r13.postTouchEvent(r14, r15, r16, r17, r18, r19, r20);	 Catch:{ all -> 0x0095 }
        goto L_0x0145;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.backends.android.AndroidMultiTouchHandler.onTouch(android.view.MotionEvent, com.badlogic.gdx.backends.android.AndroidInput):void");
    }

    private void logAction(int action, int pointer) {
        String actionStr = BuildConfig.VERSION_NAME;
        if (action == 0) {
            actionStr = "DOWN";
        } else if (action == 5) {
            actionStr = "POINTER DOWN";
        } else if (action == 1) {
            actionStr = "UP";
        } else if (action == 6) {
            actionStr = "POINTER UP";
        } else if (action == 4) {
            actionStr = "OUTSIDE";
        } else if (action == 3) {
            actionStr = "CANCEL";
        } else if (action == 2) {
            actionStr = "MOVE";
        } else {
            actionStr = "UNKNOWN (" + action + ")";
        }
        Gdx.app.log("AndroidMultiTouchHandler", "action " + actionStr + ", Android pointer id: " + pointer);
    }

    private int toGdxButton(int button) {
        if (button == 0 || button == 1) {
            return 0;
        }
        if (button == 2) {
            return 1;
        }
        if (button == 4) {
            return 2;
        }
        if (button == 8) {
            return 3;
        }
        if (button == 16) {
            return 4;
        }
        return -1;
    }

    private void postTouchEvent(AndroidInput input, int type, int x, int y, int pointer, int button, long timeStamp) {
        TouchEvent event = (TouchEvent) input.usedTouchEvents.obtain();
        event.timeStamp = timeStamp;
        event.pointer = pointer;
        event.x = x;
        event.y = y;
        event.type = type;
        event.button = button;
        input.touchEvents.add(event);
    }

    public boolean supportsMultitouch(Context activity) {
        return activity.getPackageManager().hasSystemFeature("android.hardware.touchscreen.multitouch");
    }
}
