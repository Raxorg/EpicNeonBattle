package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class DynamicsModifier extends Influencer {
    protected static final Quaternion TMP_Q = new Quaternion();
    protected static final Vector3 TMP_V1 = new Vector3();
    protected static final Vector3 TMP_V2 = new Vector3();
    protected static final Vector3 TMP_V3 = new Vector3();
    public boolean isGlobal = false;
    protected FloatChannel lifeChannel;

    public static abstract class Strength extends DynamicsModifier {
        protected FloatChannel strengthChannel;
        public ScaledNumericValue strengthValue = new ScaledNumericValue();

        public Strength(Strength rotation) {
            super(rotation);
            this.strengthValue.load(rotation.strengthValue);
        }

        public void allocateChannels() {
            super.allocateChannels();
            ParticleChannels.Interpolation.id = this.controller.particleChannels.newId();
            this.strengthChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Interpolation);
        }

        public void activateParticles(int startIndex, int count) {
            int i = startIndex * this.strengthChannel.strideSize;
            int c = i + (this.strengthChannel.strideSize * count);
            while (i < c) {
                float start = this.strengthValue.newLowValue();
                float diff = this.strengthValue.newHighValue();
                if (!this.strengthValue.isRelative()) {
                    diff -= start;
                }
                this.strengthChannel.data[i + 0] = start;
                this.strengthChannel.data[i + 1] = diff;
                i += this.strengthChannel.strideSize;
            }
        }

        public void write(Json json) {
            super.write(json);
            json.writeValue("strengthValue", this.strengthValue);
        }

        public void read(Json json, JsonValue jsonData) {
            super.read(json, jsonData);
            this.strengthValue = (ScaledNumericValue) json.readValue("strengthValue", ScaledNumericValue.class, jsonData);
        }
    }

    public static abstract class Angular extends Strength {
        protected FloatChannel angularChannel;
        public ScaledNumericValue phiValue = new ScaledNumericValue();
        public ScaledNumericValue thetaValue = new ScaledNumericValue();

        public Angular(Angular value) {
            super(value);
            this.thetaValue.load(value.thetaValue);
            this.phiValue.load(value.phiValue);
        }

        public void allocateChannels() {
            super.allocateChannels();
            ParticleChannels.Interpolation4.id = this.controller.particleChannels.newId();
            this.angularChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Interpolation4);
        }

        public void activateParticles(int startIndex, int count) {
            super.activateParticles(startIndex, count);
            int i = startIndex * this.angularChannel.strideSize;
            int c = i + (this.angularChannel.strideSize * count);
            while (i < c) {
                float start = this.thetaValue.newLowValue();
                float diff = this.thetaValue.newHighValue();
                if (!this.thetaValue.isRelative()) {
                    diff -= start;
                }
                this.angularChannel.data[i + 0] = start;
                this.angularChannel.data[i + 1] = diff;
                start = this.phiValue.newLowValue();
                diff = this.phiValue.newHighValue();
                if (!this.phiValue.isRelative()) {
                    diff -= start;
                }
                this.angularChannel.data[i + 2] = start;
                this.angularChannel.data[i + 3] = diff;
                i += this.angularChannel.strideSize;
            }
        }

        public void write(Json json) {
            super.write(json);
            json.writeValue("thetaValue", this.thetaValue);
            json.writeValue("phiValue", this.phiValue);
        }

        public void read(Json json, JsonValue jsonData) {
            super.read(json, jsonData);
            this.thetaValue = (ScaledNumericValue) json.readValue("thetaValue", ScaledNumericValue.class, jsonData);
            this.phiValue = (ScaledNumericValue) json.readValue("phiValue", ScaledNumericValue.class, jsonData);
        }
    }

    public static class BrownianAcceleration extends Strength {
        FloatChannel accelerationChannel;

        public BrownianAcceleration(BrownianAcceleration rotation) {
            super(rotation);
        }

        public void allocateChannels() {
            super.allocateChannels();
            this.accelerationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Acceleration);
        }

        public void update() {
            int lifeOffset = 2;
            int strengthOffset = 0;
            int forceOffset = 0;
            int i = 0;
            int c = this.controller.particles.size;
            while (i < c) {
                TMP_V3.set(MathUtils.random(-1.0f, 1.0f), MathUtils.random(-1.0f, 1.0f), MathUtils.random(-1.0f, 1.0f)).nor().scl(this.strengthChannel.data[strengthOffset + 0] + (this.strengthChannel.data[strengthOffset + 1] * this.strengthValue.getScale(this.lifeChannel.data[lifeOffset])));
                float[] fArr = this.accelerationChannel.data;
                int i2 = forceOffset + 0;
                fArr[i2] = fArr[i2] + TMP_V3.x;
                fArr = this.accelerationChannel.data;
                i2 = forceOffset + 1;
                fArr[i2] = fArr[i2] + TMP_V3.y;
                fArr = this.accelerationChannel.data;
                i2 = forceOffset + 2;
                fArr[i2] = fArr[i2] + TMP_V3.z;
                i++;
                strengthOffset += this.strengthChannel.strideSize;
                forceOffset += this.accelerationChannel.strideSize;
                lifeOffset += this.lifeChannel.strideSize;
            }
        }

        public BrownianAcceleration copy() {
            return new BrownianAcceleration(this);
        }
    }

    public static class CentripetalAcceleration extends Strength {
        FloatChannel accelerationChannel;
        FloatChannel positionChannel;

        public CentripetalAcceleration(CentripetalAcceleration rotation) {
            super(rotation);
        }

        public void allocateChannels() {
            super.allocateChannels();
            this.accelerationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Acceleration);
            this.positionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Position);
        }

        public void update() {
            float cx = 0.0f;
            float cy = 0.0f;
            float cz = 0.0f;
            if (!this.isGlobal) {
                float[] val = this.controller.transform.val;
                cx = val[12];
                cy = val[13];
                cz = val[14];
            }
            int lifeOffset = 2;
            int strengthOffset = 0;
            int positionOffset = 0;
            int forceOffset = 0;
            int i = 0;
            int c = this.controller.particles.size;
            while (i < c) {
                TMP_V3.set(this.positionChannel.data[positionOffset + 0] - cx, this.positionChannel.data[positionOffset + 1] - cy, this.positionChannel.data[positionOffset + 2] - cz).nor().scl(this.strengthChannel.data[strengthOffset + 0] + (this.strengthChannel.data[strengthOffset + 1] * this.strengthValue.getScale(this.lifeChannel.data[lifeOffset])));
                float[] fArr = this.accelerationChannel.data;
                int i2 = forceOffset + 0;
                fArr[i2] = fArr[i2] + TMP_V3.x;
                fArr = this.accelerationChannel.data;
                i2 = forceOffset + 1;
                fArr[i2] = fArr[i2] + TMP_V3.y;
                fArr = this.accelerationChannel.data;
                i2 = forceOffset + 2;
                fArr[i2] = fArr[i2] + TMP_V3.z;
                i++;
                positionOffset += this.positionChannel.strideSize;
                strengthOffset += this.strengthChannel.strideSize;
                forceOffset += this.accelerationChannel.strideSize;
                lifeOffset += this.lifeChannel.strideSize;
            }
        }

        public CentripetalAcceleration copy() {
            return new CentripetalAcceleration(this);
        }
    }

    public static class FaceDirection extends DynamicsModifier {
        FloatChannel accellerationChannel;
        FloatChannel rotationChannel;

        public FaceDirection(FaceDirection rotation) {
            super(rotation);
        }

        public void allocateChannels() {
            this.rotationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Rotation3D);
            this.accellerationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Acceleration);
        }

        public void update() {
            int i = 0;
            int accelOffset = 0;
            int c = 0 + (this.controller.particles.size * this.rotationChannel.strideSize);
            while (i < c) {
                Vector3 axisZ = TMP_V1.set(this.accellerationChannel.data[accelOffset + 0], this.accellerationChannel.data[accelOffset + 1], this.accellerationChannel.data[accelOffset + 2]).nor();
                Vector3 axisY = TMP_V2.set(TMP_V1).crs(Vector3.Y).nor().crs(TMP_V1).nor();
                Vector3 axisX = TMP_V3.set(axisY).crs(axisZ).nor();
                TMP_Q.setFromAxes(false, axisX.x, axisY.x, axisZ.x, axisX.y, axisY.y, axisZ.y, axisX.z, axisY.z, axisZ.z);
                this.rotationChannel.data[i + 0] = TMP_Q.x;
                this.rotationChannel.data[i + 1] = TMP_Q.y;
                this.rotationChannel.data[i + 2] = TMP_Q.z;
                this.rotationChannel.data[i + 3] = TMP_Q.w;
                i += this.rotationChannel.strideSize;
                accelOffset += this.accellerationChannel.strideSize;
            }
        }

        public ParticleControllerComponent copy() {
            return new FaceDirection(this);
        }
    }

    public static class PolarAcceleration extends Angular {
        FloatChannel directionalVelocityChannel;

        public PolarAcceleration(PolarAcceleration rotation) {
            super(rotation);
        }

        public void allocateChannels() {
            super.allocateChannels();
            this.directionalVelocityChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Acceleration);
        }

        public void update() {
            int i = 0;
            int l = 2;
            int s = 0;
            int a = 0;
            int c = 0 + (this.controller.particles.size * this.directionalVelocityChannel.strideSize);
            while (i < c) {
                float lifePercent = this.lifeChannel.data[l];
                float strength = this.strengthChannel.data[s + 0] + (this.strengthChannel.data[s + 1] * this.strengthValue.getScale(lifePercent));
                float phi = this.angularChannel.data[a + 2] + (this.angularChannel.data[a + 3] * this.phiValue.getScale(lifePercent));
                float theta = this.angularChannel.data[a + 0] + (this.angularChannel.data[a + 1] * this.thetaValue.getScale(lifePercent));
                float cosTheta = MathUtils.cosDeg(theta);
                float sinTheta = MathUtils.sinDeg(theta);
                float cosPhi = MathUtils.cosDeg(phi);
                float sinPhi = MathUtils.sinDeg(phi);
                TMP_V3.set(cosTheta * sinPhi, cosPhi, sinTheta * sinPhi).nor().scl(strength);
                float[] fArr = this.directionalVelocityChannel.data;
                int i2 = i + 0;
                fArr[i2] = fArr[i2] + TMP_V3.x;
                fArr = this.directionalVelocityChannel.data;
                i2 = i + 1;
                fArr[i2] = fArr[i2] + TMP_V3.y;
                fArr = this.directionalVelocityChannel.data;
                i2 = i + 2;
                fArr[i2] = fArr[i2] + TMP_V3.z;
                s += this.strengthChannel.strideSize;
                i += this.directionalVelocityChannel.strideSize;
                a += this.angularChannel.strideSize;
                l += this.lifeChannel.strideSize;
            }
        }

        public PolarAcceleration copy() {
            return new PolarAcceleration(this);
        }
    }

    public static class Rotational2D extends Strength {
        FloatChannel rotationalVelocity2dChannel;

        public Rotational2D(Rotational2D rotation) {
            super(rotation);
        }

        public void allocateChannels() {
            super.allocateChannels();
            this.rotationalVelocity2dChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.AngularVelocity2D);
        }

        public void update() {
            int i = 0;
            int l = 2;
            int s = 0;
            int c = 0 + (this.controller.particles.size * this.rotationalVelocity2dChannel.strideSize);
            while (i < c) {
                float[] fArr = this.rotationalVelocity2dChannel.data;
                fArr[i] = fArr[i] + (this.strengthChannel.data[s + 0] + (this.strengthChannel.data[s + 1] * this.strengthValue.getScale(this.lifeChannel.data[l])));
                s += this.strengthChannel.strideSize;
                i += this.rotationalVelocity2dChannel.strideSize;
                l += this.lifeChannel.strideSize;
            }
        }

        public Rotational2D copy() {
            return new Rotational2D(this);
        }
    }

    public static class Rotational3D extends Angular {
        FloatChannel rotationChannel;
        FloatChannel rotationalForceChannel;

        public Rotational3D(Rotational3D rotation) {
            super(rotation);
        }

        public void allocateChannels() {
            super.allocateChannels();
            this.rotationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Rotation3D);
            this.rotationalForceChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.AngularVelocity3D);
        }

        public void update() {
            int i = 0;
            int l = 2;
            int s = 0;
            int a = 0;
            int c = this.controller.particles.size * this.rotationalForceChannel.strideSize;
            while (i < c) {
                float lifePercent = this.lifeChannel.data[l];
                float strength = this.strengthChannel.data[s + 0] + (this.strengthChannel.data[s + 1] * this.strengthValue.getScale(lifePercent));
                float phi = this.angularChannel.data[a + 2] + (this.angularChannel.data[a + 3] * this.phiValue.getScale(lifePercent));
                float theta = this.angularChannel.data[a + 0] + (this.angularChannel.data[a + 1] * this.thetaValue.getScale(lifePercent));
                float cosTheta = MathUtils.cosDeg(theta);
                float sinTheta = MathUtils.sinDeg(theta);
                float cosPhi = MathUtils.cosDeg(phi);
                float sinPhi = MathUtils.sinDeg(phi);
                TMP_V3.set(cosTheta * sinPhi, cosPhi, sinTheta * sinPhi);
                TMP_V3.scl(MathUtils.degreesToRadians * strength);
                float[] fArr = this.rotationalForceChannel.data;
                int i2 = i + 0;
                fArr[i2] = fArr[i2] + TMP_V3.x;
                fArr = this.rotationalForceChannel.data;
                i2 = i + 1;
                fArr[i2] = fArr[i2] + TMP_V3.y;
                fArr = this.rotationalForceChannel.data;
                i2 = i + 2;
                fArr[i2] = fArr[i2] + TMP_V3.z;
                s += this.strengthChannel.strideSize;
                i += this.rotationalForceChannel.strideSize;
                a += this.angularChannel.strideSize;
                l += this.lifeChannel.strideSize;
            }
        }

        public Rotational3D copy() {
            return new Rotational3D(this);
        }
    }

    public static class TangentialAcceleration extends Angular {
        FloatChannel directionalVelocityChannel;
        FloatChannel positionChannel;

        public TangentialAcceleration(TangentialAcceleration rotation) {
            super(rotation);
        }

        public void allocateChannels() {
            super.allocateChannels();
            this.directionalVelocityChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Acceleration);
            this.positionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Position);
        }

        public void update() {
            int i = 0;
            int l = 2;
            int s = 0;
            int a = 0;
            int positionOffset = 0;
            int c = 0 + (this.controller.particles.size * this.directionalVelocityChannel.strideSize);
            while (i < c) {
                float lifePercent = this.lifeChannel.data[l];
                float strength = this.strengthChannel.data[s + 0] + (this.strengthChannel.data[s + 1] * this.strengthValue.getScale(lifePercent));
                float phi = this.angularChannel.data[a + 2] + (this.angularChannel.data[a + 3] * this.phiValue.getScale(lifePercent));
                float theta = this.angularChannel.data[a + 0] + (this.angularChannel.data[a + 1] * this.thetaValue.getScale(lifePercent));
                float cosTheta = MathUtils.cosDeg(theta);
                float sinTheta = MathUtils.sinDeg(theta);
                float cosPhi = MathUtils.cosDeg(phi);
                float sinPhi = MathUtils.sinDeg(phi);
                TMP_V3.set(cosTheta * sinPhi, cosPhi, sinTheta * sinPhi).crs(this.positionChannel.data[positionOffset + 0], this.positionChannel.data[positionOffset + 1], this.positionChannel.data[positionOffset + 2]).nor().scl(strength);
                float[] fArr = this.directionalVelocityChannel.data;
                int i2 = i + 0;
                fArr[i2] = fArr[i2] + TMP_V3.x;
                fArr = this.directionalVelocityChannel.data;
                i2 = i + 1;
                fArr[i2] = fArr[i2] + TMP_V3.y;
                fArr = this.directionalVelocityChannel.data;
                i2 = i + 2;
                fArr[i2] = fArr[i2] + TMP_V3.z;
                s += this.strengthChannel.strideSize;
                i += this.directionalVelocityChannel.strideSize;
                a += this.angularChannel.strideSize;
                l += this.lifeChannel.strideSize;
                positionOffset += this.positionChannel.strideSize;
            }
        }

        public TangentialAcceleration copy() {
            return new TangentialAcceleration(this);
        }
    }

    public DynamicsModifier(DynamicsModifier modifier) {
        this.isGlobal = modifier.isGlobal;
    }

    public void allocateChannels() {
        this.lifeChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Life);
    }

    public void write(Json json) {
        super.write(json);
        json.writeValue("isGlobal", Boolean.valueOf(this.isGlobal));
    }

    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.isGlobal = ((Boolean) json.readValue("isGlobal", Boolean.TYPE, jsonData)).booleanValue();
    }
}
