package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ChannelDescriptor;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ChannelInitializer;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import java.util.Arrays;

public class ParticleChannels {
    public static final ChannelDescriptor Acceleration = new ChannelDescriptor(newGlobalId(), Float.TYPE, WOffset);
    public static final int AlphaOffset = 3;
    public static final ChannelDescriptor AngularVelocity2D = new ChannelDescriptor(newGlobalId(), Float.TYPE, YOffset);
    public static final ChannelDescriptor AngularVelocity3D = new ChannelDescriptor(newGlobalId(), Float.TYPE, WOffset);
    public static final int BlueOffset = 2;
    public static final ChannelDescriptor Color = new ChannelDescriptor(newGlobalId(), Float.TYPE, HalfWidthOffset);
    public static final int CosineOffset = 0;
    public static final int CurrentLifeOffset = 0;
    public static final int GreenOffset = 1;
    public static final int HalfHeightOffset = 5;
    public static final int HalfWidthOffset = 4;
    public static final ChannelDescriptor Interpolation = new ChannelDescriptor(-1, Float.TYPE, ZOffset);
    public static final ChannelDescriptor Interpolation4 = new ChannelDescriptor(-1, Float.TYPE, HalfWidthOffset);
    public static final ChannelDescriptor Interpolation6 = new ChannelDescriptor(-1, Float.TYPE, 6);
    public static final int InterpolationDiffOffset = 1;
    public static final int InterpolationStartOffset = 0;
    public static final ChannelDescriptor Life = new ChannelDescriptor(newGlobalId(), Float.TYPE, WOffset);
    public static final int LifePercentOffset = 2;
    public static final ChannelDescriptor ModelInstance = new ChannelDescriptor(newGlobalId(), ModelInstance.class, YOffset);
    public static final ChannelDescriptor ParticleController = new ChannelDescriptor(newGlobalId(), ParticleController.class, YOffset);
    public static final ChannelDescriptor Position = new ChannelDescriptor(newGlobalId(), Float.TYPE, WOffset);
    public static final ChannelDescriptor PreviousPosition = new ChannelDescriptor(newGlobalId(), Float.TYPE, WOffset);
    public static final int RedOffset = 0;
    public static final ChannelDescriptor Rotation2D = new ChannelDescriptor(newGlobalId(), Float.TYPE, ZOffset);
    public static final ChannelDescriptor Rotation3D = new ChannelDescriptor(newGlobalId(), Float.TYPE, HalfWidthOffset);
    public static final ChannelDescriptor Scale = new ChannelDescriptor(newGlobalId(), Float.TYPE, YOffset);
    public static final int SineOffset = 1;
    public static final ChannelDescriptor TextureRegion = new ChannelDescriptor(newGlobalId(), Float.TYPE, 6);
    public static final int TotalLifeOffset = 1;
    public static final int U2Offset = 2;
    public static final int UOffset = 0;
    public static final int V2Offset = 3;
    public static final int VOffset = 1;
    public static final int VelocityPhiDiffOffset = 3;
    public static final int VelocityPhiStartOffset = 2;
    public static final int VelocityStrengthDiffOffset = 1;
    public static final int VelocityStrengthStartOffset = 0;
    public static final int VelocityThetaDiffOffset = 1;
    public static final int VelocityThetaStartOffset = 0;
    public static final int WOffset = 3;
    public static final int XOffset = 0;
    public static final int YOffset = 1;
    public static final int ZOffset = 2;
    private static int currentGlobalId;
    private int currentId;

    public static class ColorInitializer implements ChannelInitializer<FloatChannel> {
        private static ColorInitializer instance;

        public static ColorInitializer get() {
            if (instance == null) {
                instance = new ColorInitializer();
            }
            return instance;
        }

        public void init(FloatChannel channel) {
            Arrays.fill(channel.data, ParticleChannels.XOffset, channel.data.length, 1.0f);
        }
    }

    public static class Rotation2dInitializer implements ChannelInitializer<FloatChannel> {
        private static Rotation2dInitializer instance;

        public static Rotation2dInitializer get() {
            if (instance == null) {
                instance = new Rotation2dInitializer();
            }
            return instance;
        }

        public void init(FloatChannel channel) {
            int i = ParticleChannels.XOffset;
            int c = channel.data.length;
            while (i < c) {
                channel.data[i + ParticleChannels.XOffset] = 1.0f;
                channel.data[i + ParticleChannels.YOffset] = 0.0f;
                i += channel.strideSize;
            }
        }
    }

    public static class Rotation3dInitializer implements ChannelInitializer<FloatChannel> {
        private static Rotation3dInitializer instance;

        public static Rotation3dInitializer get() {
            if (instance == null) {
                instance = new Rotation3dInitializer();
            }
            return instance;
        }

        public void init(FloatChannel channel) {
            int i = ParticleChannels.XOffset;
            int c = channel.data.length;
            while (i < c) {
                float[] fArr = channel.data;
                int i2 = i + ParticleChannels.XOffset;
                float[] fArr2 = channel.data;
                int i3 = i + ParticleChannels.YOffset;
                channel.data[i + ParticleChannels.ZOffset] = 0.0f;
                fArr2[i3] = 0.0f;
                fArr[i2] = 0.0f;
                channel.data[i + ParticleChannels.WOffset] = 1.0f;
                i += channel.strideSize;
            }
        }
    }

    public static class ScaleInitializer implements ChannelInitializer<FloatChannel> {
        private static ScaleInitializer instance;

        public static ScaleInitializer get() {
            if (instance == null) {
                instance = new ScaleInitializer();
            }
            return instance;
        }

        public void init(FloatChannel channel) {
            Arrays.fill(channel.data, ParticleChannels.XOffset, channel.data.length, 1.0f);
        }
    }

    public static class TextureRegionInitializer implements ChannelInitializer<FloatChannel> {
        private static TextureRegionInitializer instance;

        public static TextureRegionInitializer get() {
            if (instance == null) {
                instance = new TextureRegionInitializer();
            }
            return instance;
        }

        public void init(FloatChannel channel) {
            int i = ParticleChannels.XOffset;
            int c = channel.data.length;
            while (i < c) {
                channel.data[i + ParticleChannels.XOffset] = 0.0f;
                channel.data[i + ParticleChannels.YOffset] = 0.0f;
                channel.data[i + ParticleChannels.ZOffset] = 1.0f;
                channel.data[i + ParticleChannels.WOffset] = 1.0f;
                channel.data[i + ParticleChannels.HalfWidthOffset] = 0.5f;
                channel.data[i + ParticleChannels.HalfHeightOffset] = 0.5f;
                i += channel.strideSize;
            }
        }
    }

    public static int newGlobalId() {
        int i = currentGlobalId;
        currentGlobalId = i + YOffset;
        return i;
    }

    public ParticleChannels() {
        resetIds();
    }

    public int newId() {
        int i = this.currentId;
        this.currentId = i + YOffset;
        return i;
    }

    protected void resetIds() {
        this.currentId = currentGlobalId;
    }
}
