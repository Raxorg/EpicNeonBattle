package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;

public class ScaleInfluencer extends SimpleInfluencer {
    public ScaleInfluencer() {
        this.valueChannelDescriptor = ParticleChannels.Scale;
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
                float start = this.value.newLowValue() * this.controller.scale.x;
                float diff = this.value.newHighValue() * this.controller.scale.x;
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
            start = this.value.newLowValue() * this.controller.scale.x;
            diff = (this.value.newHighValue() * this.controller.scale.x) - start;
            this.interpolationChannel.data[a + 0] = start;
            this.interpolationChannel.data[a + 1] = diff;
            this.valueChannel.data[i] = (this.value.getScale(0.0f) * diff) + start;
            i += this.valueChannel.strideSize;
            a += this.interpolationChannel.strideSize;
        }
    }

    public ScaleInfluencer(ScaleInfluencer scaleInfluencer) {
        super(scaleInfluencer);
    }

    public ParticleControllerComponent copy() {
        return new ScaleInfluencer(this);
    }
}
