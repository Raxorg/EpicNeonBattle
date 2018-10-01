package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ChannelDescriptor;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class SimpleInfluencer extends Influencer {
    FloatChannel interpolationChannel;
    FloatChannel lifeChannel;
    public ScaledNumericValue value;
    FloatChannel valueChannel;
    ChannelDescriptor valueChannelDescriptor;

    public SimpleInfluencer() {
        this.value = new ScaledNumericValue();
        this.value.setHigh(1.0f);
    }

    public SimpleInfluencer(SimpleInfluencer billboardScaleinfluencer) {
        this();
        set(billboardScaleinfluencer);
    }

    private void set(SimpleInfluencer scaleInfluencer) {
        this.value.load(scaleInfluencer.value);
        this.valueChannelDescriptor = scaleInfluencer.valueChannelDescriptor;
    }

    public void allocateChannels() {
        this.valueChannel = (FloatChannel) this.controller.particles.addChannel(this.valueChannelDescriptor);
        ParticleChannels.Interpolation.id = this.controller.particleChannels.newId();
        this.interpolationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Interpolation);
        this.lifeChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Life);
    }

    public void activateParticles(int startIndex, int count) {
        int i;
        int a;
        int c;
        if (this.value.isRelative()) {
            i = startIndex * this.valueChannel.strideSize;
            a = startIndex * this.interpolationChannel.strideSize;
            c = i + (this.valueChannel.strideSize * count);
            while (i < c) {
                float start = this.value.newLowValue();
                float diff = this.value.newHighValue();
                this.interpolationChannel.data[a + 0] = start;
                this.interpolationChannel.data[a + 1] = diff;
                this.valueChannel.data[i] = (this.value.getScale(0.0f) * diff) + start;
                i += this.valueChannel.strideSize;
                a += this.interpolationChannel.strideSize;
            }
            return;
        }
        i = startIndex * this.valueChannel.strideSize;
        a = startIndex * this.interpolationChannel.strideSize;
        c = i + (this.valueChannel.strideSize * count);
        while (i < c) {
            start = this.value.newLowValue();
            diff = this.value.newHighValue() - start;
            this.interpolationChannel.data[a + 0] = start;
            this.interpolationChannel.data[a + 1] = diff;
            this.valueChannel.data[i] = (this.value.getScale(0.0f) * diff) + start;
            i += this.valueChannel.strideSize;
            a += this.interpolationChannel.strideSize;
        }
    }

    public void update() {
        int i = 0;
        int a = 0;
        int l = 2;
        int c = 0 + (this.controller.particles.size * this.valueChannel.strideSize);
        while (i < c) {
            this.valueChannel.data[i] = this.interpolationChannel.data[a + 0] + (this.interpolationChannel.data[a + 1] * this.value.getScale(this.lifeChannel.data[l]));
            i += this.valueChannel.strideSize;
            a += this.interpolationChannel.strideSize;
            l += this.lifeChannel.strideSize;
        }
    }

    public void write(Json json) {
        json.writeValue("value", this.value);
    }

    public void read(Json json, JsonValue jsonData) {
        this.value = (ScaledNumericValue) json.readValue("value", ScaledNumericValue.class, jsonData);
    }
}
