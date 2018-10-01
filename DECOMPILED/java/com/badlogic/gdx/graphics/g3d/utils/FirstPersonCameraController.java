package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.IntIntMap;

public class FirstPersonCameraController extends InputAdapter {
    private int BACKWARD = 47;
    private int DOWN = 33;
    private int FORWARD = 51;
    private int STRAFE_LEFT = 29;
    private int STRAFE_RIGHT = 32;
    private int UP = 45;
    private final Camera camera;
    private float degreesPerPixel = 0.5f;
    private final IntIntMap keys = new IntIntMap();
    private final Vector3 tmp = new Vector3();
    private float velocity = 5.0f;

    public FirstPersonCameraController(Camera camera) {
        this.camera = camera;
    }

    public boolean keyDown(int keycode) {
        this.keys.put(keycode, keycode);
        return true;
    }

    public boolean keyUp(int keycode) {
        this.keys.remove(keycode, 0);
        return true;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public void setDegreesPerPixel(float degreesPerPixel) {
        this.degreesPerPixel = degreesPerPixel;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        float deltaY = ((float) (-Gdx.input.getDeltaY())) * this.degreesPerPixel;
        this.camera.direction.rotate(this.camera.up, ((float) (-Gdx.input.getDeltaX())) * this.degreesPerPixel);
        this.tmp.set(this.camera.direction).crs(this.camera.up).nor();
        this.camera.direction.rotate(this.tmp, deltaY);
        return true;
    }

    public void update() {
        update(Gdx.graphics.getDeltaTime());
    }

    public void update(float deltaTime) {
        if (this.keys.containsKey(this.FORWARD)) {
            this.tmp.set(this.camera.direction).nor().scl(this.velocity * deltaTime);
            this.camera.position.add(this.tmp);
        }
        if (this.keys.containsKey(this.BACKWARD)) {
            this.tmp.set(this.camera.direction).nor().scl((-deltaTime) * this.velocity);
            this.camera.position.add(this.tmp);
        }
        if (this.keys.containsKey(this.STRAFE_LEFT)) {
            this.tmp.set(this.camera.direction).crs(this.camera.up).nor().scl((-deltaTime) * this.velocity);
            this.camera.position.add(this.tmp);
        }
        if (this.keys.containsKey(this.STRAFE_RIGHT)) {
            this.tmp.set(this.camera.direction).crs(this.camera.up).nor().scl(this.velocity * deltaTime);
            this.camera.position.add(this.tmp);
        }
        if (this.keys.containsKey(this.UP)) {
            this.tmp.set(this.camera.up).nor().scl(this.velocity * deltaTime);
            this.camera.position.add(this.tmp);
        }
        if (this.keys.containsKey(this.DOWN)) {
            this.tmp.set(this.camera.up).nor().scl((-deltaTime) * this.velocity);
            this.camera.position.add(this.tmp);
        }
        this.camera.update(true);
    }
}
