package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.ColorInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.Rotation2dInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.ScaleInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.TextureRegionInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;

public class BillboardRenderer extends ParticleControllerRenderer<BillboardControllerRenderData, BillboardParticleBatch> {
    public BillboardRenderer() {
        super(new BillboardControllerRenderData());
    }

    public BillboardRenderer(BillboardParticleBatch batch) {
        this();
        setBatch(batch);
    }

    public void allocateChannels() {
        ((BillboardControllerRenderData) this.renderData).positionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Position);
        ((BillboardControllerRenderData) this.renderData).regionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.TextureRegion, TextureRegionInitializer.get());
        ((BillboardControllerRenderData) this.renderData).colorChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Color, ColorInitializer.get());
        ((BillboardControllerRenderData) this.renderData).scaleChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Scale, ScaleInitializer.get());
        ((BillboardControllerRenderData) this.renderData).rotationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Rotation2D, Rotation2dInitializer.get());
    }

    public ParticleControllerComponent copy() {
        return new BillboardRenderer((BillboardParticleBatch) this.batch);
    }

    public boolean isCompatible(ParticleBatch<?> batch) {
        return batch instanceof BillboardParticleBatch;
    }
}
