package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ObjectChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ParticleControllerFinalizerInfluencer extends Influencer {
    ObjectChannel<ParticleController> controllerChannel;
    boolean hasRotation;
    boolean hasScale;
    FloatChannel positionChannel;
    FloatChannel rotationChannel;
    FloatChannel scaleChannel;

    public void init() {
        boolean z = true;
        this.controllerChannel = (ObjectChannel) this.controller.particles.getChannel(ParticleChannels.ParticleController);
        if (this.controllerChannel == null) {
            throw new GdxRuntimeException("ParticleController channel not found, specify an influencer which will allocate it please.");
        }
        boolean z2;
        this.scaleChannel = (FloatChannel) this.controller.particles.getChannel(ParticleChannels.Scale);
        this.rotationChannel = (FloatChannel) this.controller.particles.getChannel(ParticleChannels.Rotation3D);
        if (this.scaleChannel != null) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.hasScale = z2;
        if (this.rotationChannel == null) {
            z = false;
        }
        this.hasRotation = z;
    }

    public void allocateChannels() {
        this.positionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Position);
    }

    public void update() {
        int i = 0;
        int positionOffset = 0;
        int c = this.controller.particles.size;
        while (i < c) {
            ParticleController particleController = ((ParticleController[]) this.controllerChannel.data)[i];
            float scale = this.hasScale ? this.scaleChannel.data[i] : 1.0f;
            float qx = 0.0f;
            float qy = 0.0f;
            float qz = 0.0f;
            float qw = 1.0f;
            if (this.hasRotation) {
                int rotationOffset = i * this.rotationChannel.strideSize;
                qx = this.rotationChannel.data[rotationOffset + 0];
                qy = this.rotationChannel.data[rotationOffset + 1];
                qz = this.rotationChannel.data[rotationOffset + 2];
                qw = this.rotationChannel.data[rotationOffset + 3];
            }
            particleController.setTransform(this.positionChannel.data[positionOffset + 0], this.positionChannel.data[positionOffset + 1], this.positionChannel.data[positionOffset + 2], qx, qy, qz, qw, scale);
            particleController.update();
            i++;
            positionOffset += this.positionChannel.strideSize;
        }
    }

    public ParticleControllerFinalizerInfluencer copy() {
        return new ParticleControllerFinalizerInfluencer();
    }
}
