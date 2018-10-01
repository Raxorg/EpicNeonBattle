package com.badlogic.gdx.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.InputProcessor;
import java.io.DataOutputStream;
import java.net.Socket;

public class RemoteSender implements InputProcessor {
    public static final int ACCEL = 6;
    public static final int COMPASS = 7;
    public static final int KEY_DOWN = 0;
    public static final int KEY_TYPED = 2;
    public static final int KEY_UP = 1;
    public static final int SIZE = 8;
    public static final int TOUCH_DOWN = 3;
    public static final int TOUCH_DRAGGED = 5;
    public static final int TOUCH_UP = 4;
    private boolean connected = false;
    private DataOutputStream out;

    public RemoteSender(String ip, int port) {
        try {
            Socket socket = new Socket(ip, port);
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(3000);
            this.out = new DataOutputStream(socket.getOutputStream());
            this.out.writeBoolean(Gdx.input.isPeripheralAvailable(Peripheral.MultitouchScreen));
            this.connected = true;
            Gdx.input.setInputProcessor(this);
        } catch (Exception e) {
            Gdx.app.log("RemoteSender", "couldn't connect to " + ip + ":" + port);
        }
    }

    public void sendUpdate() {
        synchronized (this) {
            if (this.connected) {
                try {
                    this.out.writeInt(ACCEL);
                    this.out.writeFloat(Gdx.input.getAccelerometerX());
                    this.out.writeFloat(Gdx.input.getAccelerometerY());
                    this.out.writeFloat(Gdx.input.getAccelerometerZ());
                    this.out.writeInt(COMPASS);
                    this.out.writeFloat(Gdx.input.getAzimuth());
                    this.out.writeFloat(Gdx.input.getPitch());
                    this.out.writeFloat(Gdx.input.getRoll());
                    this.out.writeInt(SIZE);
                    this.out.writeFloat((float) Gdx.graphics.getWidth());
                    this.out.writeFloat((float) Gdx.graphics.getHeight());
                    return;
                } catch (Throwable th) {
                    this.out = null;
                    this.connected = false;
                    return;
                }
            }
        }
    }

    public boolean keyDown(int keycode) {
        synchronized (this) {
            if (this.connected) {
                try {
                    this.out.writeInt(KEY_DOWN);
                    this.out.writeInt(keycode);
                } catch (Throwable th) {
                    synchronized (this) {
                        this.connected = false;
                    }
                }
            }
        }
        return false;
    }

    public boolean keyUp(int keycode) {
        synchronized (this) {
            if (this.connected) {
                try {
                    this.out.writeInt(KEY_UP);
                    this.out.writeInt(keycode);
                } catch (Throwable th) {
                    synchronized (this) {
                        this.connected = false;
                    }
                }
            }
        }
        return false;
    }

    public boolean keyTyped(char character) {
        synchronized (this) {
            if (this.connected) {
                try {
                    this.out.writeInt(KEY_TYPED);
                    this.out.writeChar(character);
                } catch (Throwable th) {
                    synchronized (this) {
                        this.connected = false;
                    }
                }
            }
        }
        return false;
    }

    public boolean touchDown(int x, int y, int pointer, int button) {
        synchronized (this) {
            if (this.connected) {
                try {
                    this.out.writeInt(TOUCH_DOWN);
                    this.out.writeInt(x);
                    this.out.writeInt(y);
                    this.out.writeInt(pointer);
                } catch (Throwable th) {
                    synchronized (this) {
                        this.connected = false;
                    }
                }
            }
        }
        return false;
    }

    public boolean touchUp(int x, int y, int pointer, int button) {
        synchronized (this) {
            if (this.connected) {
                try {
                    this.out.writeInt(TOUCH_UP);
                    this.out.writeInt(x);
                    this.out.writeInt(y);
                    this.out.writeInt(pointer);
                } catch (Throwable th) {
                    synchronized (this) {
                        this.connected = false;
                    }
                }
            }
        }
        return false;
    }

    public boolean touchDragged(int x, int y, int pointer) {
        synchronized (this) {
            if (this.connected) {
                try {
                    this.out.writeInt(TOUCH_DRAGGED);
                    this.out.writeInt(x);
                    this.out.writeInt(y);
                    this.out.writeInt(pointer);
                } catch (Throwable th) {
                    synchronized (this) {
                        this.connected = false;
                    }
                }
            }
        }
        return false;
    }

    public boolean mouseMoved(int x, int y) {
        return false;
    }

    public boolean scrolled(int amount) {
        return false;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this) {
            z = this.connected;
        }
        return z;
    }
}
