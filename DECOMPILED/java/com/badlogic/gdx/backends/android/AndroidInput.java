package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Orientation;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.ArrayList;
import java.util.Arrays;

public class AndroidInput implements OnKeyListener, OnTouchListener, Input {
    public static final int NUM_TOUCHES = 20;
    public static final int SUPPORTED_KEYS = 260;
    final float[] R = new float[9];
    public boolean accelerometerAvailable = false;
    private SensorEventListener accelerometerListener;
    private final float[] accelerometerValues = new float[3];
    final Application app;
    private float azimuth = 0.0f;
    int[] button = new int[NUM_TOUCHES];
    private boolean catchBack = false;
    private boolean catchMenu = false;
    private boolean compassAvailable = false;
    private SensorEventListener compassListener;
    private final AndroidApplicationConfiguration config;
    final Context context;
    private long currentEventTimeStamp = System.nanoTime();
    int[] deltaX = new int[NUM_TOUCHES];
    int[] deltaY = new int[NUM_TOUCHES];
    private Handler handle;
    final boolean hasMultitouch;
    private float inclination = 0.0f;
    private boolean[] justPressedKeys = new boolean[SUPPORTED_KEYS];
    private boolean justTouched = false;
    private int keyCount = 0;
    ArrayList<KeyEvent> keyEvents = new ArrayList();
    private boolean keyJustPressed = false;
    ArrayList<OnKeyListener> keyListeners = new ArrayList();
    boolean keyboardAvailable;
    private boolean[] keys = new boolean[SUPPORTED_KEYS];
    private final float[] magneticFieldValues = new float[3];
    private SensorManager manager;
    private final Orientation nativeOrientation;
    private final AndroidOnscreenKeyboard onscreenKeyboard;
    final float[] orientation = new float[3];
    private float pitch = 0.0f;
    private InputProcessor processor;
    int[] realId = new int[NUM_TOUCHES];
    boolean requestFocus = true;
    private float roll = 0.0f;
    private int sleepTime = 0;
    private String text = null;
    private TextInputListener textListener = null;
    ArrayList<TouchEvent> touchEvents = new ArrayList();
    private final AndroidTouchHandler touchHandler;
    int[] touchX = new int[NUM_TOUCHES];
    int[] touchY = new int[NUM_TOUCHES];
    boolean[] touched = new boolean[NUM_TOUCHES];
    Pool<KeyEvent> usedKeyEvents = new Pool<KeyEvent>(16, 1000) {
        protected KeyEvent newObject() {
            return new KeyEvent();
        }
    };
    Pool<TouchEvent> usedTouchEvents = new Pool<TouchEvent>(16, 1000) {
        protected TouchEvent newObject() {
            return new TouchEvent();
        }
    };
    protected final Vibrator vibrator;

    static class KeyEvent {
        static final int KEY_DOWN = 0;
        static final int KEY_TYPED = 2;
        static final int KEY_UP = 1;
        char keyChar;
        int keyCode;
        long timeStamp;
        int type;

        KeyEvent() {
        }
    }

    private class SensorListener implements SensorEventListener {
        final float[] accelerometerValues;
        final float[] magneticFieldValues;
        final Orientation nativeOrientation;

        SensorListener(Orientation nativeOrientation, float[] accelerometerValues, float[] magneticFieldValues) {
            this.accelerometerValues = accelerometerValues;
            this.magneticFieldValues = magneticFieldValues;
            this.nativeOrientation = nativeOrientation;
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == 1) {
                if (this.nativeOrientation == Orientation.Portrait) {
                    System.arraycopy(event.values, 0, this.accelerometerValues, 0, this.accelerometerValues.length);
                } else {
                    this.accelerometerValues[0] = event.values[1];
                    this.accelerometerValues[1] = -event.values[0];
                    this.accelerometerValues[2] = event.values[2];
                }
            }
            if (event.sensor.getType() == 2) {
                System.arraycopy(event.values, 0, this.magneticFieldValues, 0, this.magneticFieldValues.length);
            }
        }
    }

    static class TouchEvent {
        static final int TOUCH_DOWN = 0;
        static final int TOUCH_DRAGGED = 2;
        static final int TOUCH_MOVED = 4;
        static final int TOUCH_SCROLLED = 3;
        static final int TOUCH_UP = 1;
        int button;
        int pointer;
        int scrollAmount;
        long timeStamp;
        int type;
        int x;
        int y;

        TouchEvent() {
        }
    }

    public AndroidInput(Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
        if (view instanceof View) {
            View v = (View) view;
            v.setOnKeyListener(this);
            v.setOnTouchListener(this);
            v.setFocusable(true);
            v.setFocusableInTouchMode(true);
            v.requestFocus();
        }
        this.config = config;
        this.onscreenKeyboard = new AndroidOnscreenKeyboard(context, new Handler(), this);
        for (int i = 0; i < this.realId.length; i++) {
            this.realId[i] = -1;
        }
        this.handle = new Handler();
        this.app = activity;
        this.context = context;
        this.sleepTime = config.touchSleepTime;
        this.touchHandler = new AndroidMultiTouchHandler();
        this.hasMultitouch = this.touchHandler.supportsMultitouch(context);
        this.vibrator = (Vibrator) context.getSystemService("vibrator");
        int rotation = getRotation();
        DisplayMode mode = this.app.getGraphics().getDesktopDisplayMode();
        if (((rotation == 0 || rotation == 180) && mode.width >= mode.height) || ((rotation == 90 || rotation == 270) && mode.width <= mode.height)) {
            this.nativeOrientation = Orientation.Landscape;
        } else {
            this.nativeOrientation = Orientation.Portrait;
        }
    }

    public float getAccelerometerX() {
        return this.accelerometerValues[0];
    }

    public float getAccelerometerY() {
        return this.accelerometerValues[1];
    }

    public float getAccelerometerZ() {
        return this.accelerometerValues[2];
    }

    public void getTextInput(TextInputListener listener, String title, String text, String hint) {
        final String str = title;
        final String str2 = hint;
        final String str3 = text;
        final TextInputListener textInputListener = listener;
        this.handle.post(new Runnable() {
            public void run() {
                Builder alert = new Builder(AndroidInput.this.context);
                alert.setTitle(str);
                final EditText input = new EditText(AndroidInput.this.context);
                input.setHint(str2);
                input.setText(str3);
                input.setSingleLine();
                alert.setView(input);
                alert.setPositiveButton(AndroidInput.this.context.getString(17039370), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                textInputListener.input(input.getText().toString());
                            }
                        });
                    }
                });
                alert.setNegativeButton(AndroidInput.this.context.getString(17039360), new OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                textInputListener.canceled();
                            }
                        });
                    }
                });
                alert.setOnCancelListener(new OnCancelListener() {
                    public void onCancel(DialogInterface arg0) {
                        Gdx.app.postRunnable(new Runnable() {
                            public void run() {
                                textInputListener.canceled();
                            }
                        });
                    }
                });
                alert.show();
            }
        });
    }

    public int getX() {
        int i;
        synchronized (this) {
            i = this.touchX[0];
        }
        return i;
    }

    public int getY() {
        int i;
        synchronized (this) {
            i = this.touchY[0];
        }
        return i;
    }

    public int getX(int pointer) {
        int i;
        synchronized (this) {
            i = this.touchX[pointer];
        }
        return i;
    }

    public int getY(int pointer) {
        int i;
        synchronized (this) {
            i = this.touchY[pointer];
        }
        return i;
    }

    public boolean isTouched(int pointer) {
        boolean z;
        synchronized (this) {
            z = this.touched[pointer];
        }
        return z;
    }

    public synchronized boolean isKeyPressed(int key) {
        boolean z = false;
        synchronized (this) {
            if (key == -1) {
                if (this.keyCount > 0) {
                    z = true;
                }
            } else if (key >= 0 && key < SUPPORTED_KEYS) {
                z = this.keys[key];
            }
        }
        return z;
    }

    public synchronized boolean isKeyJustPressed(int key) {
        boolean z;
        if (key == -1) {
            z = this.keyJustPressed;
        } else if (key < 0 || key >= SUPPORTED_KEYS) {
            z = false;
        } else {
            z = this.justPressedKeys[key];
        }
        return z;
    }

    public boolean isTouched() {
        boolean z;
        synchronized (this) {
            if (this.hasMultitouch) {
                for (int pointer = 0; pointer < NUM_TOUCHES; pointer++) {
                    if (this.touched[pointer]) {
                        z = true;
                        break;
                    }
                }
            }
            z = this.touched[0];
        }
        return z;
    }

    public void setInputProcessor(InputProcessor processor) {
        synchronized (this) {
            this.processor = processor;
        }
    }

    void processEvents() {
        synchronized (this) {
            int i;
            this.justTouched = false;
            if (this.keyJustPressed) {
                this.keyJustPressed = false;
                for (i = 0; i < this.justPressedKeys.length; i++) {
                    this.justPressedKeys[i] = false;
                }
            }
            int len;
            TouchEvent e;
            if (this.processor != null) {
                InputProcessor processor = this.processor;
                len = this.keyEvents.size();
                for (i = 0; i < len; i++) {
                    KeyEvent e2 = (KeyEvent) this.keyEvents.get(i);
                    this.currentEventTimeStamp = e2.timeStamp;
                    switch (e2.type) {
                        case Encoder.EMatchFinderTypeBT2 /*0*/:
                            processor.keyDown(e2.keyCode);
                            this.keyJustPressed = true;
                            this.justPressedKeys[e2.keyCode] = true;
                            break;
                        case BuildConfig.VERSION_CODE /*1*/:
                            processor.keyUp(e2.keyCode);
                            break;
                        case Base.kNumLenToPosStatesBits /*2*/:
                            processor.keyTyped(e2.keyChar);
                            break;
                        default:
                            break;
                    }
                    this.usedKeyEvents.free(e2);
                }
                len = this.touchEvents.size();
                for (i = 0; i < len; i++) {
                    e = (TouchEvent) this.touchEvents.get(i);
                    this.currentEventTimeStamp = e.timeStamp;
                    switch (e.type) {
                        case Encoder.EMatchFinderTypeBT2 /*0*/:
                            processor.touchDown(e.x, e.y, e.pointer, e.button);
                            this.justTouched = true;
                            break;
                        case BuildConfig.VERSION_CODE /*1*/:
                            processor.touchUp(e.x, e.y, e.pointer, e.button);
                            break;
                        case Base.kNumLenToPosStatesBits /*2*/:
                            processor.touchDragged(e.x, e.y, e.pointer);
                            break;
                        case Base.kNumMidLenBits /*3*/:
                            processor.scrolled(e.scrollAmount);
                            break;
                        case Base.kStartPosModelIndex /*4*/:
                            processor.mouseMoved(e.x, e.y);
                            break;
                        default:
                            break;
                    }
                    this.usedTouchEvents.free(e);
                }
            } else {
                len = this.touchEvents.size();
                for (i = 0; i < len; i++) {
                    e = (TouchEvent) this.touchEvents.get(i);
                    if (e.type == 0) {
                        this.justTouched = true;
                    }
                    this.usedTouchEvents.free(e);
                }
                len = this.keyEvents.size();
                for (i = 0; i < len; i++) {
                    this.usedKeyEvents.free(this.keyEvents.get(i));
                }
            }
            if (this.touchEvents.size() == 0) {
                for (i = 0; i < this.deltaX.length; i++) {
                    this.deltaX[0] = 0;
                    this.deltaY[0] = 0;
                }
            }
            this.keyEvents.clear();
            this.touchEvents.clear();
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        if (this.requestFocus && view != null) {
            view.setFocusableInTouchMode(true);
            view.requestFocus();
            this.requestFocus = false;
        }
        this.touchHandler.onTouch(event, this);
        if (this.sleepTime != 0) {
            try {
                Thread.sleep((long) this.sleepTime);
            } catch (InterruptedException e) {
            }
        }
        return true;
    }

    public void onTap(int x, int y) {
        postTap(x, y);
    }

    public void onDrop(int x, int y) {
        postTap(x, y);
    }

    protected void postTap(int x, int y) {
        synchronized (this) {
            TouchEvent event = (TouchEvent) this.usedTouchEvents.obtain();
            event.timeStamp = System.nanoTime();
            event.pointer = 0;
            event.x = x;
            event.y = y;
            event.type = 0;
            this.touchEvents.add(event);
            event = (TouchEvent) this.usedTouchEvents.obtain();
            event.timeStamp = System.nanoTime();
            event.pointer = 0;
            event.x = x;
            event.y = y;
            event.type = 1;
            this.touchEvents.add(event);
        }
        Gdx.app.getGraphics().requestRendering();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onKey(android.view.View r13, int r14, android.view.KeyEvent r15) {
        /*
        r12 = this;
        r5 = 0;
        r7 = r12.keyListeners;
        r6 = r7.size();
    L_0x0007:
        if (r5 >= r6) goto L_0x001c;
    L_0x0009:
        r7 = r12.keyListeners;
        r7 = r7.get(r5);
        r7 = (android.view.View.OnKeyListener) r7;
        r7 = r7.onKey(r13, r14, r15);
        if (r7 == 0) goto L_0x0019;
    L_0x0017:
        r7 = 1;
    L_0x0018:
        return r7;
    L_0x0019:
        r5 = r5 + 1;
        goto L_0x0007;
    L_0x001c:
        monitor-enter(r12);
        r4 = 0;
        r7 = r15.getKeyCode();	 Catch:{ all -> 0x005d }
        if (r7 != 0) goto L_0x0060;
    L_0x0024:
        r7 = r15.getAction();	 Catch:{ all -> 0x005d }
        r10 = 2;
        if (r7 != r10) goto L_0x0060;
    L_0x002b:
        r3 = r15.getCharacters();	 Catch:{ all -> 0x005d }
        r5 = 0;
    L_0x0030:
        r7 = r3.length();	 Catch:{ all -> 0x005d }
        if (r5 >= r7) goto L_0x005a;
    L_0x0036:
        r7 = r12.usedKeyEvents;	 Catch:{ all -> 0x005d }
        r7 = r7.obtain();	 Catch:{ all -> 0x005d }
        r0 = r7;
        r0 = (com.badlogic.gdx.backends.android.AndroidInput.KeyEvent) r0;	 Catch:{ all -> 0x005d }
        r4 = r0;
        r10 = java.lang.System.nanoTime();	 Catch:{ all -> 0x005d }
        r4.timeStamp = r10;	 Catch:{ all -> 0x005d }
        r7 = 0;
        r4.keyCode = r7;	 Catch:{ all -> 0x005d }
        r7 = r3.charAt(r5);	 Catch:{ all -> 0x005d }
        r4.keyChar = r7;	 Catch:{ all -> 0x005d }
        r7 = 2;
        r4.type = r7;	 Catch:{ all -> 0x005d }
        r7 = r12.keyEvents;	 Catch:{ all -> 0x005d }
        r7.add(r4);	 Catch:{ all -> 0x005d }
        r5 = r5 + 1;
        goto L_0x0030;
    L_0x005a:
        r7 = 0;
        monitor-exit(r12);	 Catch:{ all -> 0x005d }
        goto L_0x0018;
    L_0x005d:
        r7 = move-exception;
        monitor-exit(r12);	 Catch:{ all -> 0x005d }
        throw r7;
    L_0x0060:
        r7 = r15.getUnicodeChar();	 Catch:{ all -> 0x005d }
        r2 = (char) r7;	 Catch:{ all -> 0x005d }
        r7 = 67;
        if (r14 != r7) goto L_0x006b;
    L_0x0069:
        r2 = 8;
    L_0x006b:
        r7 = r15.getKeyCode();	 Catch:{ all -> 0x005d }
        if (r7 < 0) goto L_0x0079;
    L_0x0071:
        r7 = r15.getKeyCode();	 Catch:{ all -> 0x005d }
        r10 = 260; // 0x104 float:3.64E-43 double:1.285E-321;
        if (r7 < r10) goto L_0x007c;
    L_0x0079:
        r7 = 0;
        monitor-exit(r12);	 Catch:{ all -> 0x005d }
        goto L_0x0018;
    L_0x007c:
        r7 = r15.getAction();	 Catch:{ all -> 0x005d }
        switch(r7) {
            case 0: goto L_0x0093;
            case 1: goto L_0x00d7;
            default: goto L_0x0083;
        };	 Catch:{ all -> 0x005d }
    L_0x0083:
        r7 = r12.app;	 Catch:{ all -> 0x005d }
        r7 = r7.getGraphics();	 Catch:{ all -> 0x005d }
        r7.requestRendering();	 Catch:{ all -> 0x005d }
        monitor-exit(r12);	 Catch:{ all -> 0x005d }
        r7 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r14 != r7) goto L_0x0154;
    L_0x0091:
        r7 = 1;
        goto L_0x0018;
    L_0x0093:
        r7 = r12.usedKeyEvents;	 Catch:{ all -> 0x005d }
        r7 = r7.obtain();	 Catch:{ all -> 0x005d }
        r0 = r7;
        r0 = (com.badlogic.gdx.backends.android.AndroidInput.KeyEvent) r0;	 Catch:{ all -> 0x005d }
        r4 = r0;
        r10 = java.lang.System.nanoTime();	 Catch:{ all -> 0x005d }
        r4.timeStamp = r10;	 Catch:{ all -> 0x005d }
        r7 = 0;
        r4.keyChar = r7;	 Catch:{ all -> 0x005d }
        r7 = r15.getKeyCode();	 Catch:{ all -> 0x005d }
        r4.keyCode = r7;	 Catch:{ all -> 0x005d }
        r7 = 0;
        r4.type = r7;	 Catch:{ all -> 0x005d }
        r7 = 4;
        if (r14 != r7) goto L_0x00bc;
    L_0x00b2:
        r7 = r15.isAltPressed();	 Catch:{ all -> 0x005d }
        if (r7 == 0) goto L_0x00bc;
    L_0x00b8:
        r14 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r4.keyCode = r14;	 Catch:{ all -> 0x005d }
    L_0x00bc:
        r7 = r12.keyEvents;	 Catch:{ all -> 0x005d }
        r7.add(r4);	 Catch:{ all -> 0x005d }
        r7 = r12.keys;	 Catch:{ all -> 0x005d }
        r10 = r4.keyCode;	 Catch:{ all -> 0x005d }
        r7 = r7[r10];	 Catch:{ all -> 0x005d }
        if (r7 != 0) goto L_0x0083;
    L_0x00c9:
        r7 = r12.keyCount;	 Catch:{ all -> 0x005d }
        r7 = r7 + 1;
        r12.keyCount = r7;	 Catch:{ all -> 0x005d }
        r7 = r12.keys;	 Catch:{ all -> 0x005d }
        r10 = r4.keyCode;	 Catch:{ all -> 0x005d }
        r11 = 1;
        r7[r10] = r11;	 Catch:{ all -> 0x005d }
        goto L_0x0083;
    L_0x00d7:
        r8 = java.lang.System.nanoTime();	 Catch:{ all -> 0x005d }
        r7 = r12.usedKeyEvents;	 Catch:{ all -> 0x005d }
        r7 = r7.obtain();	 Catch:{ all -> 0x005d }
        r0 = r7;
        r0 = (com.badlogic.gdx.backends.android.AndroidInput.KeyEvent) r0;	 Catch:{ all -> 0x005d }
        r4 = r0;
        r4.timeStamp = r8;	 Catch:{ all -> 0x005d }
        r7 = 0;
        r4.keyChar = r7;	 Catch:{ all -> 0x005d }
        r7 = r15.getKeyCode();	 Catch:{ all -> 0x005d }
        r4.keyCode = r7;	 Catch:{ all -> 0x005d }
        r7 = 1;
        r4.type = r7;	 Catch:{ all -> 0x005d }
        r7 = 4;
        if (r14 != r7) goto L_0x0100;
    L_0x00f6:
        r7 = r15.isAltPressed();	 Catch:{ all -> 0x005d }
        if (r7 == 0) goto L_0x0100;
    L_0x00fc:
        r14 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r4.keyCode = r14;	 Catch:{ all -> 0x005d }
    L_0x0100:
        r7 = r12.keyEvents;	 Catch:{ all -> 0x005d }
        r7.add(r4);	 Catch:{ all -> 0x005d }
        r7 = r12.usedKeyEvents;	 Catch:{ all -> 0x005d }
        r7 = r7.obtain();	 Catch:{ all -> 0x005d }
        r0 = r7;
        r0 = (com.badlogic.gdx.backends.android.AndroidInput.KeyEvent) r0;	 Catch:{ all -> 0x005d }
        r4 = r0;
        r4.timeStamp = r8;	 Catch:{ all -> 0x005d }
        r4.keyChar = r2;	 Catch:{ all -> 0x005d }
        r7 = 0;
        r4.keyCode = r7;	 Catch:{ all -> 0x005d }
        r7 = 2;
        r4.type = r7;	 Catch:{ all -> 0x005d }
        r7 = r12.keyEvents;	 Catch:{ all -> 0x005d }
        r7.add(r4);	 Catch:{ all -> 0x005d }
        r7 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        if (r14 != r7) goto L_0x0139;
    L_0x0122:
        r7 = r12.keys;	 Catch:{ all -> 0x005d }
        r10 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r7 = r7[r10];	 Catch:{ all -> 0x005d }
        if (r7 == 0) goto L_0x0083;
    L_0x012a:
        r7 = r12.keyCount;	 Catch:{ all -> 0x005d }
        r7 = r7 + -1;
        r12.keyCount = r7;	 Catch:{ all -> 0x005d }
        r7 = r12.keys;	 Catch:{ all -> 0x005d }
        r10 = 255; // 0xff float:3.57E-43 double:1.26E-321;
        r11 = 0;
        r7[r10] = r11;	 Catch:{ all -> 0x005d }
        goto L_0x0083;
    L_0x0139:
        r7 = r12.keys;	 Catch:{ all -> 0x005d }
        r10 = r15.getKeyCode();	 Catch:{ all -> 0x005d }
        r7 = r7[r10];	 Catch:{ all -> 0x005d }
        if (r7 == 0) goto L_0x0083;
    L_0x0143:
        r7 = r12.keyCount;	 Catch:{ all -> 0x005d }
        r7 = r7 + -1;
        r12.keyCount = r7;	 Catch:{ all -> 0x005d }
        r7 = r12.keys;	 Catch:{ all -> 0x005d }
        r10 = r15.getKeyCode();	 Catch:{ all -> 0x005d }
        r11 = 0;
        r7[r10] = r11;	 Catch:{ all -> 0x005d }
        goto L_0x0083;
    L_0x0154:
        r7 = r12.catchBack;
        if (r7 == 0) goto L_0x015e;
    L_0x0158:
        r7 = 4;
        if (r14 != r7) goto L_0x015e;
    L_0x015b:
        r7 = 1;
        goto L_0x0018;
    L_0x015e:
        r7 = r12.catchMenu;
        if (r7 == 0) goto L_0x0169;
    L_0x0162:
        r7 = 82;
        if (r14 != r7) goto L_0x0169;
    L_0x0166:
        r7 = 1;
        goto L_0x0018;
    L_0x0169:
        r7 = 0;
        goto L_0x0018;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.backends.android.AndroidInput.onKey(android.view.View, int, android.view.KeyEvent):boolean");
    }

    public void setOnscreenKeyboardVisible(final boolean visible) {
        this.handle.post(new Runnable() {
            public void run() {
                InputMethodManager manager = (InputMethodManager) AndroidInput.this.context.getSystemService("input_method");
                if (visible) {
                    View view = ((AndroidGraphics) AndroidInput.this.app.getGraphics()).getView();
                    view.setFocusable(true);
                    view.setFocusableInTouchMode(true);
                    manager.showSoftInput(((AndroidGraphics) AndroidInput.this.app.getGraphics()).getView(), 0);
                    return;
                }
                manager.hideSoftInputFromWindow(((AndroidGraphics) AndroidInput.this.app.getGraphics()).getView().getWindowToken(), 0);
            }
        });
    }

    public void setCatchBackKey(boolean catchBack) {
        this.catchBack = catchBack;
    }

    public boolean isCatchBackKey() {
        return this.catchBack;
    }

    public void setCatchMenuKey(boolean catchMenu) {
        this.catchMenu = catchMenu;
    }

    public void vibrate(int milliseconds) {
        this.vibrator.vibrate((long) milliseconds);
    }

    public void vibrate(long[] pattern, int repeat) {
        this.vibrator.vibrate(pattern, repeat);
    }

    public void cancelVibrate() {
        this.vibrator.cancel();
    }

    public boolean justTouched() {
        return this.justTouched;
    }

    public boolean isButtonPressed(int button) {
        boolean z = true;
        synchronized (this) {
            if (this.hasMultitouch) {
                int pointer = 0;
                while (pointer < NUM_TOUCHES) {
                    if (this.touched[pointer] && this.button[pointer] == button) {
                        break;
                    }
                    pointer++;
                }
            }
            if (!(this.touched[0] && this.button[0] == button)) {
                z = false;
            }
        }
        return z;
    }

    private void updateOrientation() {
        if (SensorManager.getRotationMatrix(this.R, null, this.accelerometerValues, this.magneticFieldValues)) {
            SensorManager.getOrientation(this.R, this.orientation);
            this.azimuth = (float) Math.toDegrees((double) this.orientation[0]);
            this.pitch = (float) Math.toDegrees((double) this.orientation[1]);
            this.roll = (float) Math.toDegrees((double) this.orientation[2]);
        }
    }

    public void getRotationMatrix(float[] matrix) {
        SensorManager.getRotationMatrix(matrix, null, this.accelerometerValues, this.magneticFieldValues);
    }

    public float getAzimuth() {
        if (!this.compassAvailable) {
            return 0.0f;
        }
        updateOrientation();
        return this.azimuth;
    }

    public float getPitch() {
        if (!this.compassAvailable) {
            return 0.0f;
        }
        updateOrientation();
        return this.pitch;
    }

    public float getRoll() {
        if (!this.compassAvailable) {
            return 0.0f;
        }
        updateOrientation();
        return this.roll;
    }

    void registerSensorListeners() {
        if (this.config.useAccelerometer) {
            this.manager = (SensorManager) this.context.getSystemService("sensor");
            if (this.manager.getSensorList(1).size() == 0) {
                this.accelerometerAvailable = false;
            } else {
                Sensor accelerometer = (Sensor) this.manager.getSensorList(1).get(0);
                this.accelerometerListener = new SensorListener(this.nativeOrientation, this.accelerometerValues, this.magneticFieldValues);
                this.accelerometerAvailable = this.manager.registerListener(this.accelerometerListener, accelerometer, 1);
            }
        } else {
            this.accelerometerAvailable = false;
        }
        if (this.config.useCompass) {
            if (this.manager == null) {
                this.manager = (SensorManager) this.context.getSystemService("sensor");
            }
            Sensor sensor = this.manager.getDefaultSensor(2);
            if (sensor != null) {
                this.compassAvailable = this.accelerometerAvailable;
                if (this.compassAvailable) {
                    this.compassListener = new SensorListener(this.nativeOrientation, this.accelerometerValues, this.magneticFieldValues);
                    this.compassAvailable = this.manager.registerListener(this.compassListener, sensor, 1);
                }
            } else {
                this.compassAvailable = false;
            }
        } else {
            this.compassAvailable = false;
        }
        Gdx.app.log("AndroidInput", "sensor listener setup");
    }

    void unregisterSensorListeners() {
        if (this.manager != null) {
            if (this.accelerometerListener != null) {
                this.manager.unregisterListener(this.accelerometerListener);
                this.accelerometerListener = null;
            }
            if (this.compassListener != null) {
                this.manager.unregisterListener(this.compassListener);
                this.compassListener = null;
            }
            this.manager = null;
        }
        Gdx.app.log("AndroidInput", "sensor listener tear down");
    }

    public InputProcessor getInputProcessor() {
        return this.processor;
    }

    public boolean isPeripheralAvailable(Peripheral peripheral) {
        if (peripheral == Peripheral.Accelerometer) {
            return this.accelerometerAvailable;
        }
        if (peripheral == Peripheral.Compass) {
            return this.compassAvailable;
        }
        if (peripheral == Peripheral.HardwareKeyboard) {
            return this.keyboardAvailable;
        }
        if (peripheral == Peripheral.OnscreenKeyboard) {
            return true;
        }
        if (peripheral != Peripheral.Vibrator) {
            return peripheral == Peripheral.MultitouchScreen ? this.hasMultitouch : false;
        } else {
            if (this.vibrator == null) {
                return false;
            }
            return true;
        }
    }

    public int getFreePointerIndex() {
        int len = this.realId.length;
        for (int i = 0; i < len; i++) {
            if (this.realId[i] == -1) {
                return i;
            }
        }
        this.realId = resize(this.realId);
        this.touchX = resize(this.touchX);
        this.touchY = resize(this.touchY);
        this.deltaX = resize(this.deltaX);
        this.deltaY = resize(this.deltaY);
        this.touched = resize(this.touched);
        this.button = resize(this.button);
        return len;
    }

    private int[] resize(int[] orig) {
        int[] tmp = new int[(orig.length + 2)];
        System.arraycopy(orig, 0, tmp, 0, orig.length);
        return tmp;
    }

    private boolean[] resize(boolean[] orig) {
        boolean[] tmp = new boolean[(orig.length + 2)];
        System.arraycopy(orig, 0, tmp, 0, orig.length);
        return tmp;
    }

    public int lookUpPointerIndex(int pointerId) {
        int i;
        int len = this.realId.length;
        for (i = 0; i < len; i++) {
            if (this.realId[i] == pointerId) {
                return i;
            }
        }
        StringBuffer buf = new StringBuffer();
        for (i = 0; i < len; i++) {
            buf.append(i + ":" + this.realId[i] + " ");
        }
        Gdx.app.log("AndroidInput", "Pointer ID lookup failed: " + pointerId + ", " + buf.toString());
        return -1;
    }

    public int getRotation() {
        int orientation;
        if (this.context instanceof Activity) {
            orientation = ((Activity) this.context).getWindowManager().getDefaultDisplay().getRotation();
        } else {
            orientation = ((WindowManager) this.context.getSystemService("window")).getDefaultDisplay().getRotation();
        }
        switch (orientation) {
            case Encoder.EMatchFinderTypeBT2 /*0*/:
                return 0;
            case BuildConfig.VERSION_CODE /*1*/:
                return 90;
            case Base.kNumLenToPosStatesBits /*2*/:
                return 180;
            case Base.kNumMidLenBits /*3*/:
                return 270;
            default:
                return 0;
        }
    }

    public Orientation getNativeOrientation() {
        return this.nativeOrientation;
    }

    public void setCursorCatched(boolean catched) {
    }

    public boolean isCursorCatched() {
        return false;
    }

    public int getDeltaX() {
        return this.deltaX[0];
    }

    public int getDeltaX(int pointer) {
        return this.deltaX[pointer];
    }

    public int getDeltaY() {
        return this.deltaY[0];
    }

    public int getDeltaY(int pointer) {
        return this.deltaY[pointer];
    }

    public void setCursorPosition(int x, int y) {
    }

    public long getCurrentEventTime() {
        return this.currentEventTimeStamp;
    }

    public void addKeyListener(OnKeyListener listener) {
        this.keyListeners.add(listener);
    }

    public void onPause() {
        unregisterSensorListeners();
        Arrays.fill(this.realId, -1);
        Arrays.fill(this.touched, false);
    }

    public void onResume() {
        registerSensorListeners();
    }
}
