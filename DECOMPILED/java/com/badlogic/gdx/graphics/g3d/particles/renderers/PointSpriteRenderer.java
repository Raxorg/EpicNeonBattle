package com.badlogic.gdx.graphics.g3d.particles.renderers;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.ColorInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.Rotation2dInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.ScaleInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels.TextureRegionInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;

public class PointSpriteRenderer extends ParticleControllerRenderer<PointSpriteControllerRenderData, PointSpriteParticleBatch> {
    public PointSpriteRenderer() {
        super(new PointSpriteControllerRenderData());
    }

    public PointSpriteRenderer(PointSpriteParticleBatch batch) {
        this();
        setBatch(batch);
    }

    public void allocateChannels() {
        ((PointSpriteControllerRenderData) this.renderData).positionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Position);
        ((PointSpriteControllerRenderData) this.renderData).regionChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.TextureRegion, TextureRegionInitializer.get());
        ((PointSpriteControllerRenderData) this.renderData).colorChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Color, ColorInitializer.get());
        ((PointSpriteControllerRenderData) this.renderData).scaleChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Scale, ScaleInitializer.get());
        ((PointSpriteControllerRenderData) this.renderData).rotationChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Rotation2D, Rotation2dInitializer.get());
    }

    public boolean isCompatible(ParticleBatch<?> batch) {
        return batch instanceof PointSpriteParticleBatch;
    }

    public ParticleControllerComponent copy() {
        return new PointSpriteRenderer((PointSpriteParticleBatch) this.batch);
    }
}
