package com.badlogic.gdx.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Orientation;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class RemoteInput implements Input, Runnable {
    public static int DEFAULT_PORT = 8190;
    private float[] accel;
    private float[] compass;
    private boolean connected;
    public final String[] ips;
    boolean[] isTouched;
    boolean[] justPressedKeys;
    boolean justTouched;
    int keyCount;
    boolean keyJustPressed;
    boolean[] keys;
    private RemoteInputListener listener;
    private boolean multiTouch;
    private final int port;
    InputProcessor processor;
    private float remoteHeight;
    private float remoteWidth;
    private ServerSocket serverSocket;
    int[] touchX;
    int[] touchY;

    class EventTrigger implements Runnable {
        KeyEvent keyEvent;
        TouchEvent touchEvent;

        public EventTrigger(TouchEvent touchEvent, KeyEvent keyEvent) {
            this.touchEvent = touchEvent;
            this.keyEvent = keyEvent;
        }

        public void run() {
            RemoteInput.this.justTouched = false;
            if (RemoteInput.this.keyJustPressed) {
                RemoteInput.this.keyJustPressed = false;
                for (int i = 0; i < RemoteInput.this.justPressedKeys.length; i++) {
                    RemoteInput.this.justPressedKeys[i] = false;
                }
            }
            RemoteInput remoteInput;
            if (RemoteInput.this.processor != null) {
                if (this.touchEvent != null) {
                    RemoteInput.this.touchX[this.touchEvent.pointer] = this.touchEvent.x;
                    RemoteInput.this.touchY[this.touchEvent.pointer] = this.touchEvent.y;
                    switch (this.touchEvent.type) {
                        case Encoder.EMatchFinderTypeBT2 /*0*/:
                            RemoteInput.this.processor.touchDown(this.touchEvent.x, this.touchEvent.y, this.touchEvent.pointer, 0);
                            RemoteInput.this.isTouched[this.touchEvent.pointer] = true;
                            RemoteInput.this.justTouched = true;
                            break;
                        case BuildConfig.VERSION_CODE /*1*/:
                            RemoteInput.this.processor.touchUp(this.touchEvent.x, this.touchEvent.y, this.touchEvent.pointer, 0);
                            RemoteInput.this.isTouched[this.touchEvent.pointer] = false;
                            break;
                        case Base.kNumLenToPosStatesBits /*2*/:
                            RemoteInput.this.processor.touchDragged(this.touchEvent.x, this.touchEvent.y, this.touchEvent.pointer);
                            break;
                    }
                }
                if (this.keyEvent != null) {
                    switch (this.keyEvent.type) {
                        case Encoder.EMatchFinderTypeBT2 /*0*/:
                            RemoteInput.this.processor.keyDown(this.keyEvent.keyCode);
                            if (!RemoteInput.this.keys[this.keyEvent.keyCode]) {
                                remoteInput = RemoteInput.this;
                                remoteInput.keyCount++;
                                RemoteInput.this.keys[this.keyEvent.keyCode] = true;
                            }
                            RemoteInput.this.keyJustPressed = true;
                            RemoteInput.this.justPressedKeys[this.keyEvent.keyCode] = true;
                            return;
                        case BuildConfig.VERSION_CODE /*1*/:
                            RemoteInput.this.processor.keyUp(this.keyEvent.keyCode);
                            if (RemoteInput.this.keys[this.keyEvent.keyCode]) {
                                remoteInput = RemoteInput.this;
                                remoteInput.keyCount--;
                                RemoteInput.this.keys[this.keyEvent.keyCode] = false;
                                return;
                            }
                            return;
                        case Base.kNumLenToPosStatesBits /*2*/:
                            RemoteInput.this.processor.keyTyped(this.keyEvent.keyChar);
                            return;
                        default:
                            return;
                    }
                }
                return;
            }
            if (this.touchEvent != null) {
                RemoteInput.this.touchX[this.touchEvent.pointer] = this.touchEvent.x;
                RemoteInput.this.touchY[this.touchEvent.pointer] = this.touchEvent.y;
                if (this.touchEvent.type == 0) {
                    RemoteInput.this.isTouched[this.touchEvent.pointer] = true;
                    RemoteInput.this.justTouched = true;
                }
                if (this.touchEvent.type == 1) {
                    RemoteInput.this.isTouched[this.touchEvent.pointer] = false;
                }
            }
            if (this.keyEvent != null) {
                if (this.keyEvent.type == 0) {
                    if (!RemoteInput.this.keys[this.keyEvent.keyCode]) {
                        remoteInput = RemoteInput.this;
                        remoteInput.keyCount++;
                        RemoteInput.this.keys[this.keyEvent.keyCode] = true;
                    }
                    RemoteInput.this.keyJustPressed = true;
                    RemoteInput.this.justPressedKeys[this.keyEvent.keyCode] = true;
                }
                if (this.keyEvent.type == 1 && RemoteInput.this.keys[this.keyEvent.keyCode]) {
                    remoteInput = RemoteInput.this;
                    remoteInput.keyCount--;
                    RemoteInput.this.keys[this.keyEvent.keyCode] = false;
                }
            }
        }
    }

    class KeyEvent {
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

    public interface RemoteInputListener {
        void onConnected();

        void onDisconnected();
    }

    class TouchEvent {
        static final int TOUCH_DOWN = 0;
        static final int TOUCH_DRAGGED = 2;
        static final int TOUCH_UP = 1;
        int pointer;
        long timeStamp;
        int type;
        int x;
        int y;

        TouchEvent() {
        }
    }

    public RemoteInput() {
        this(DEFAULT_PORT);
    }

    public RemoteInput(RemoteInputListener listener) {
        this(DEFAULT_PORT, listener);
    }

    public RemoteInput(int port) {
        this(port, null);
    }

    public RemoteInput(int port, RemoteInputListener listener) {
        this.accel = new float[3];
        this.compass = new float[3];
        this.multiTouch = false;
        this.remoteWidth = 0.0f;
        this.remoteHeight = 0.0f;
        this.connected = false;
        this.keyCount = 0;
        this.keys = new boolean[Usage.BiNormal];
        this.keyJustPressed = false;
        this.justPressedKeys = new boolean[Usage.BiNormal];
        this.touchX = new int[20];
        this.touchY = new int[20];
        this.isTouched = new boolean[20];
        this.justTouched = false;
        this.processor = null;
        this.listener = listener;
        try {
            this.port = port;
            this.serverSocket = new ServerSocket(port);
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
            InetAddress[] allByName = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
            this.ips = new String[allByName.length];
            for (int i = 0; i < allByName.length; i++) {
                this.ips[i] = allByName[i].getHostAddress();
            }
        } catch (Exception e) {
            throw new GdxRuntimeException("Couldn't open listening socket at port '" + port + "'", e);
        }
    }

    public void run() {
        while (true) {
            try {
                this.connected = false;
                if (this.listener != null) {
                    this.listener.onDisconnected();
                }
                System.out.println("listening, port " + this.port);
                Socket socket = this.serverSocket.accept();
                socket.setTcpNoDelay(true);
                socket.setSoTimeout(3000);
                this.connected = true;
                if (this.listener != null) {
                    this.listener.onConnected();
                }
                DataInputStream in = new DataInputStream(socket.getInputStream());
                this.multiTouch = in.readBoolean();
                while (true) {
                    KeyEvent keyEvent = null;
                    TouchEvent touchEvent = null;
                    switch (in.readInt()) {
                        case Encoder.EMatchFinderTypeBT2 /*0*/:
                            keyEvent = new KeyEvent();
                            keyEvent.keyCode = in.readInt();
                            keyEvent.type = 0;
                            break;
                        case BuildConfig.VERSION_CODE /*1*/:
                            keyEvent = new KeyEvent();
                            keyEvent.keyCode = in.readInt();
                            keyEvent.type = 1;
                            break;
                        case Base.kNumLenToPosStatesBits /*2*/:
                            keyEvent = new KeyEvent();
                            keyEvent.keyChar = in.readChar();
                            keyEvent.type = 2;
                            break;
                        case Base.kNumMidLenBits /*3*/:
                            touchEvent = new TouchEvent();
                            touchEvent.x = (int) ((((float) in.readInt()) / this.remoteWidth) * ((float) Gdx.graphics.getWidth()));
                            touchEvent.y = (int) ((((float) in.readInt()) / this.remoteHeight) * ((float) Gdx.graphics.getHeight()));
                            touchEvent.pointer = in.readInt();
                            touchEvent.type = 0;
                            break;
                        case Base.kStartPosModelIndex /*4*/:
                            touchEvent = new TouchEvent();
                            touchEvent.x = (int) ((((float) in.readInt()) / this.remoteWidth) * ((float) Gdx.graphics.getWidth()));
                            touchEvent.y = (int) ((((float) in.readInt()) / this.remoteHeight) * ((float) Gdx.graphics.getHeight()));
                            touchEvent.pointer = in.readInt();
                            touchEvent.type = 1;
                            break;
                        case Encoder.kPropSize /*5*/:
                            touchEvent = new TouchEvent();
                            touchEvent.x = (int) ((((float) in.readInt()) / this.remoteWidth) * ((float) Gdx.graphics.getWidth()));
                            touchEvent.y = (int) ((((float) in.readInt()) / this.remoteHeight) * ((float) Gdx.graphics.getHeight()));
                            touchEvent.pointer = in.readInt();
                            touchEvent.type = 2;
                            break;
                        case com.badlogic.gdx.utils.compression.rangecoder.Encoder.kNumBitPriceShiftBits /*6*/:
                            this.accel[0] = in.readFloat();
                            this.accel[1] = in.readFloat();
                            this.accel[2] = in.readFloat();
                            break;
                        case Matrix4.M31 /*7*/:
                            this.compass[0] = in.readFloat();
                            this.compass[1] = in.readFloat();
                            this.compass[2] = in.readFloat();
                            break;
                        case Base.kNumMidLenSymbols /*8*/:
                            this.remoteWidth = in.readFloat();
                            this.remoteHeight = in.readFloat();
                            break;
                    }
                    Gdx.app.postRunnable(new EventTrigger(touchEvent, keyEvent));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public float getAccelerometerX() {
        return this.accel[0];
    }

    public float getAccelerometerY() {
        return this.accel[1];
    }

    public float getAccelerometerZ() {
        return this.accel[2];
    }

    public int getX() {
        return this.touchX[0];
    }

    public int getX(int pointer) {
        return this.touchX[pointer];
    }

    public int getY() {
        return this.touchY[0];
    }

    public int getY(int pointer) {
        return this.touchY[pointer];
    }

    public boolean isTouched() {
        return this.isTouched[0];
    }

    public boolean justTouched() {
        return this.justTouched;
    }

    public boolean isTouched(int pointer) {
        return this.isTouched[pointer];
    }

    public boolean isButtonPressed(int button) {
        if (button != 0) {
            return false;
        }
        for (boolean z : this.isTouched) {
            if (z) {
                return true;
            }
        }
        return false;
    }

    public boolean isKeyPressed(int key) {
        if (key == -1) {
            if (this.keyCount > 0) {
                return true;
            }
            return false;
        } else if (key < 0 || key > Keys.F12) {
            return false;
        } else {
            return this.keys[key];
        }
    }

    public boolean isKeyJustPressed(int key) {
        if (key == -1) {
            return this.keyJustPressed;
        }
        if (key < 0 || key > Keys.F12) {
            return false;
        }
        return this.justPressedKeys[key];
    }

    public void getTextInput(TextInputListener listener, String title, String text, String hint) {
        Gdx.app.getInput().getTextInput(listener, title, text, hint);
    }

    public void setOnscreenKeyboardVisible(boolean visible) {
    }

    public void vibrate(int milliseconds) {
    }

    public void vibrate(long[] pattern, int repeat) {
    }

    public void cancelVibrate() {
    }

    public float getAzimuth() {
        return this.compass[0];
    }

    public float getPitch() {
        return this.compass[1];
    }

    public float getRoll() {
        return this.compass[2];
    }

    public void setCatchBackKey(boolean catchBack) {
    }

    public boolean isCatchBackKey() {
        return false;
    }

    public void setInputProcessor(InputProcessor processor) {
        this.processor = processor;
    }

    public InputProcessor getInputProcessor() {
        return this.processor;
    }

    public String[] getIPs() {
        return this.ips;
    }

    public boolean isPeripheralAvailable(Peripheral peripheral) {
        if (peripheral == Peripheral.Accelerometer || peripheral == Peripheral.Compass) {
            return true;
        }
        if (peripheral == Peripheral.MultitouchScreen) {
            return this.multiTouch;
        }
        return false;
    }

    public int getRotation() {
        return 0;
    }

    public Orientation getNativeOrientation() {
        return Orientation.Landscape;
    }

    public void setCursorCatched(boolean catched) {
    }

    public boolean isCursorCatched() {
        return false;
    }

    public int getDeltaX() {
        return 0;
    }

    public int getDeltaX(int pointer) {
        return 0;
    }

    public int getDeltaY() {
        return 0;
    }

    public int getDeltaY(int pointer) {
        return 0;
    }

    public void setCursorPosition(int x, int y) {
    }

    public void setCatchMenuKey(boolean catchMenu) {
    }

    public long getCurrentEventTime() {
        return 0;
    }

    public void getRotationMatrix(float[] matrix) {
    }
}
