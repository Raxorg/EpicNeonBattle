package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import java.util.Arrays;

public class DynamicsInfluencer extends Influencer {
    private FloatChannel accellerationChannel;
    private FloatChannel angularVelocityChannel;
    boolean has2dAngularVelocity;
    boolean has3dAngularVelocity;
    boolean hasAcceleration;
    private FloatChannel positionChannel;
    private FloatChannel previousPositionChannel;
    private FloatChannel rotationChannel;
    public Array<DynamicsModifier> velocities;

    public DynamicsInfluencer() {
        this.velocities = new Array(true, 3, DynamicsModifier.class);
    }

    public DynamicsInfluencer(DynamicsModifier... velocities) {
        this.velocities = new Array(true, velocities.length, DynamicsModifier.class);
        for (DynamicsModifier value : velocities) {
            this.velocities.add((DynamicsModifier) value.copy());
        }
    }

    public DynamicsInfluencer(DynamicsInfluencer velocityInfluencer) {
        this((DynamicsModifier[]) velocityInfluencer.velocities.toArray(DynamicsModifier.class));
    }

    public void allocateChannels() {
        boolean z;
        boolean z2 = true;
        for (int k = 0; k < this.velocities.size; k++) {
            ((DynamicsModifier[]) this.velocities.items)[k].allocateChannels();
        }
        this.accellerationChannel = (FloatChannel) this.controller.particles.getChannel(ParticleChannels.Acceleration);
        this.hasAcceleration = this.accellerationChannel != null;
        if (this.hasAcceleration) {
            this.positionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Position);
            this.previousPositionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.PreviousPosition);
        }
        this.angularVelocityChannel = (FloatChannel) this.controller.particles.getChannel(ParticleChannels.AngularVelocity2D);
        if (this.angularVelocityChannel != null) {
            z = true;
        } else {
            z = false;
        }
        this.has2dAngularVelocity = z;
        if (this.has2dAngularVelocity) {
            this.rotationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Rotation2D);
            this.has3dAngularVelocity = false;
            return;
        }
        this.angularVelocityChannel = (FloatChannel) this.controller.particles.getChannel(ParticleChannels.AngularVelocity3D);
        if (this.angularVelocityChannel == null) {
            z2 = false;
        }
        this.has3dAngularVelocity = z2;
        if (this.has3dAngularVelocity) {
            this.rotationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Rotation3D);
        }
    }

    public void set(ParticleController particleController) {
        super.set(particleController);
        for (int k = 0; k < this.velocities.size; k++) {
            ((DynamicsModifier[]) this.velocities.items)[k].set(particleController);
        }
    }

    public void init() {
        for (int k = 0; k < this.velocities.size; k++) {
            ((DynamicsModifier[]) this.velocities.items)[k].init();
        }
    }

    public void activateParticles(int startIndex, int count) {
        int i;
        int c;
        if (this.hasAcceleration) {
            i = startIndex * this.positionChannel.strideSize;
            c = i + (this.positionChannel.strideSize * count);
            while (i < c) {
                this.previousPositionChannel.data[i + 0] = this.positionChannel.data[i + 0];
                this.previousPositionChannel.data[i + 1] = this.positionChannel.data[i + 1];
                this.previousPositionChannel.data[i + 2] = this.positionChannel.data[i + 2];
                i += this.positionChannel.strideSize;
            }
        }
        if (this.has2dAngularVelocity) {
            i = startIndex * this.rotationChannel.strideSize;
            c = i + (this.rotationChannel.strideSize * count);
            while (i < c) {
                this.rotationChannel.data[i + 0] = 1.0f;
                this.rotationChannel.data[i + 1] = 0.0f;
                i += this.rotationChannel.strideSize;
            }
        } else if (this.has3dAngularVelocity) {
            i = startIndex * this.rotationChannel.strideSize;
            c = i + (this.rotationChannel.strideSize * count);
            while (i < c) {
                this.rotationChannel.data[i + 0] = 0.0f;
                this.rotationChannel.data[i + 1] = 0.0f;
                this.rotationChannel.data[i + 2] = 0.0f;
                this.rotationChannel.data[i + 3] = 1.0f;
                i += this.rotationChannel.strideSize;
            }
        }
        for (int k = 0; k < this.velocities.size; k++) {
            ((DynamicsModifier[]) this.velocities.items)[k].activateParticles(startIndex, count);
        }
    }

    public void update() {
        int i;
        int offset;
        if (this.hasAcceleration) {
            Arrays.fill(this.accellerationChannel.data, 0, this.controller.particles.size * this.accellerationChannel.strideSize, 0.0f);
        }
        if (this.has2dAngularVelocity || this.has3dAngularVelocity) {
            Arrays.fill(this.angularVelocityChannel.data, 0, this.controller.particles.size * this.angularVelocityChannel.strideSize, 0.0f);
        }
        for (int k = 0; k < this.velocities.size; k++) {
            ((DynamicsModifier[]) this.velocities.items)[k].update();
        }
        if (this.hasAcceleration) {
            i = 0;
            offset = 0;
            while (i < this.controller.particles.size) {
                float x = this.positionChannel.data[offset + 0];
                float y = this.positionChannel.data[offset + 1];
                float z = this.positionChannel.data[offset + 2];
                this.positionChannel.data[offset + 0] = ((2.0f * x) - this.previousPositionChannel.data[offset + 0]) + (this.accellerationChannel.data[offset + 0] * this.controller.deltaTimeSqr);
                this.positionChannel.data[offset + 1] = ((2.0f * y) - this.previousPositionChannel.data[offset + 1]) + (this.accellerationChannel.data[offset + 1] * this.controller.deltaTimeSqr);
                this.positionChannel.data[offset + 2] = ((2.0f * z) - this.previousPositionChannel.data[offset + 2]) + (this.accellerationChannel.data[offset + 2] * this.controller.deltaTimeSqr);
                this.previousPositionChannel.data[offset + 0] = x;
                this.previousPositionChannel.data[offset + 1] = y;
                this.previousPositionChannel.data[offset + 2] = z;
                i++;
                offset += this.positionChannel.strideSize;
            }
        }
        if (this.has2dAngularVelocity) {
            i = 0;
            offset = 0;
            while (i < this.controller.particles.size) {
                float rotation = this.angularVelocityChannel.data[i] * this.controller.deltaTime;
                if (rotation != 0.0f) {
                    float cosBeta = MathUtils.cosDeg(rotation);
                    float sinBeta = MathUtils.sinDeg(rotation);
                    float currentCosine = this.rotationChannel.data[offset + 0];
                    float currentSine = this.rotationChannel.data[offset + 1];
                    float newSine = (currentSine * cosBeta) + (currentCosine * sinBeta);
                    this.rotationChannel.data[offset + 0] = (currentCosine * cosBeta) - (currentSine * sinBeta);
                    this.rotationChannel.data[offset + 1] = newSine;
                }
                i++;
                offset += this.rotationChannel.strideSize;
            }
        } else if (this.has3dAngularVelocity) {
            i = 0;
            offset = 0;
            int angularOffset = 0;
            while (i < this.controller.particles.size) {
                float wx = this.angularVelocityChannel.data[angularOffset + 0];
                float wy = this.angularVelocityChannel.data[angularOffset + 1];
                float wz = this.angularVelocityChannel.data[angularOffset + 2];
                float qx = this.rotationChannel.data[offset + 0];
                float qy = this.rotationChannel.data[offset + 1];
                float qz = this.rotationChannel.data[offset + 2];
                float qw = this.rotationChannel.data[offset + 3];
                TMP_Q.set(wx, wy, wz, 0.0f).mul(qx, qy, qz, qw).mul(0.5f * this.controller.deltaTime).add(qx, qy, qz, qw).nor();
                this.rotationChannel.data[offset + 0] = TMP_Q.x;
                this.rotationChannel.data[offset + 1] = TMP_Q.y;
                this.rotationChannel.data[offset + 2] = TMP_Q.z;
                this.rotationChannel.data[offset + 3] = TMP_Q.w;
                i++;
                offset += this.rotationChannel.strideSize;
                angularOffset += this.angularVelocityChannel.strideSize;
            }
        }
    }

    public DynamicsInfluencer copy() {
        return new DynamicsInfluencer(this);
    }

    public void write(Json json) {
        json.writeValue("velocities", this.velocities, Array.class, DynamicsModifier.class);
    }

    public void read(Json json, JsonValue jsonData) {
        this.velocities.addAll((Array) json.readValue("velocities", Array.class, DynamicsModifier.class, jsonData));
    }
}
