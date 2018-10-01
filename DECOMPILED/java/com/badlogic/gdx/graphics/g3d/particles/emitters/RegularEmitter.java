package com.badlogic.gdx.graphics.g3d.particles.emitters;

import com.badlogic.gdx.graphics.g3d.particles.ParallelArray;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleControllerComponent;
import com.badlogic.gdx.graphics.g3d.particles.values.RangedNumericValue;
import com.badlogic.gdx.graphics.g3d.particles.values.ScaledNumericValue;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

public class RegularEmitter extends Emitter implements Serializable {
    private boolean continuous;
    protected float delay;
    protected float delayTimer;
    public RangedNumericValue delayValue;
    protected float duration;
    protected float durationTimer;
    public RangedNumericValue durationValue;
    protected int emission;
    protected int emissionDelta;
    protected int emissionDiff;
    private EmissionMode emissionMode;
    public ScaledNumericValue emissionValue;
    protected int life;
    private FloatChannel lifeChannel;
    protected int lifeDiff;
    protected int lifeOffset;
    protected int lifeOffsetDiff;
    public ScaledNumericValue lifeOffsetValue;
    public ScaledNumericValue lifeValue;

    public enum EmissionMode {
        Enabled,
        EnabledUntilCycleEnd,
        Disabled
    }

    public RegularEmitter() {
        this.delayValue = new RangedNumericValue();
        this.durationValue = new RangedNumericValue();
        this.lifeOffsetValue = new ScaledNumericValue();
        this.lifeValue = new ScaledNumericValue();
        this.emissionValue = new ScaledNumericValue();
        this.durationValue.setActive(true);
        this.emissionValue.setActive(true);
        this.lifeValue.setActive(true);
        this.continuous = true;
        this.emissionMode = EmissionMode.Enabled;
    }

    public RegularEmitter(RegularEmitter regularEmitter) {
        this();
        set(regularEmitter);
    }

    public void allocateChannels() {
        this.lifeChannel = (FloatChannel) this.controller.particles.addChannel(ParticleChannels.Life);
    }

    public void start() {
        this.delay = this.delayValue.active ? this.delayValue.newLowValue() : 0.0f;
        this.delayTimer = 0.0f;
        this.durationTimer = 0.0f;
        this.duration = this.durationValue.newLowValue();
        this.percent = this.durationTimer / this.duration;
        this.emission = (int) this.emissionValue.newLowValue();
        this.emissionDiff = (int) this.emissionValue.newHighValue();
        if (!this.emissionValue.isRelative()) {
            this.emissionDiff -= this.emission;
        }
        this.life = (int) this.lifeValue.newLowValue();
        this.lifeDiff = (int) this.lifeValue.newHighValue();
        if (!this.lifeValue.isRelative()) {
            this.lifeDiff -= this.life;
        }
        this.lifeOffset = this.lifeOffsetValue.active ? (int) this.lifeOffsetValue.newLowValue() : 0;
        this.lifeOffsetDiff = (int) this.lifeOffsetValue.newHighValue();
        if (!this.lifeOffsetValue.isRelative()) {
            this.lifeOffsetDiff -= this.lifeOffset;
        }
    }

    public void init() {
        super.init();
        this.emissionDelta = 0;
        this.durationTimer = this.duration;
    }

    public void activateParticles(int startIndex, int count) {
        int currentTotaLife = this.life + ((int) (((float) this.lifeDiff) * this.lifeValue.getScale(this.percent)));
        int currentLife = currentTotaLife;
        int offsetTime = (int) (((float) this.lifeOffset) + (((float) this.lifeOffsetDiff) * this.lifeOffsetValue.getScale(this.percent)));
        if (offsetTime > 0) {
            if (offsetTime >= currentLife) {
                offsetTime = currentLife - 1;
            }
            currentLife -= offsetTime;
        }
        float lifePercent = 1.0f - (((float) currentLife) / ((float) currentTotaLife));
        int i = startIndex * this.lifeChannel.strideSize;
        int c = i + (this.lifeChannel.strideSize * count);
        while (i < c) {
            this.lifeChannel.data[i + 0] = (float) currentLife;
            this.lifeChannel.data[i + 1] = (float) currentTotaLife;
            this.lifeChannel.data[i + 2] = lifePercent;
            i += this.lifeChannel.strideSize;
        }
    }

    public void update() {
        int deltaMillis = (int) (this.controller.deltaTime * 1000.0f);
        if (this.delayTimer < this.delay) {
            this.delayTimer += (float) deltaMillis;
        } else {
            boolean emit = this.emissionMode != EmissionMode.Disabled;
            if (this.durationTimer < this.duration) {
                this.durationTimer += (float) deltaMillis;
                this.percent = this.durationTimer / this.duration;
            } else if (this.continuous && emit && this.emissionMode == EmissionMode.Enabled) {
                this.controller.start();
            } else {
                emit = false;
            }
            if (emit) {
                this.emissionDelta += deltaMillis;
                float emissionTime = ((float) this.emission) + (((float) this.emissionDiff) * this.emissionValue.getScale(this.percent));
                if (emissionTime > 0.0f) {
                    emissionTime = 1000.0f / emissionTime;
                    if (((float) this.emissionDelta) >= emissionTime) {
                        int emitCount = Math.min((int) (((float) this.emissionDelta) / emissionTime), this.maxParticleCount - this.controller.particles.size);
                        this.emissionDelta = (int) (((float) this.emissionDelta) - (((float) emitCount) * emissionTime));
                        this.emissionDelta = (int) (((float) this.emissionDelta) % emissionTime);
                        addParticles(emitCount);
                    }
                }
                if (this.controller.particles.size < this.minParticleCount) {
                    addParticles(this.minParticleCount - this.controller.particles.size);
                }
            }
        }
        int activeParticles = this.controller.particles.size;
        int i = 0;
        int k = 0;
        while (i < this.controller.particles.size) {
            float[] fArr = this.lifeChannel.data;
            int i2 = k + 0;
            float f = fArr[i2] - ((float) deltaMillis);
            fArr[i2] = f;
            if (f <= 0.0f) {
                this.controller.particles.removeElement(i);
            } else {
                this.lifeChannel.data[k + 2] = 1.0f - (this.lifeChannel.data[k + 0] / this.lifeChannel.data[k + 1]);
                i++;
                k += this.lifeChannel.strideSize;
            }
        }
        if (this.controller.particles.size < activeParticles) {
            this.controller.killParticles(this.controller.particles.size, activeParticles - this.controller.particles.size);
        }
    }

    private void addParticles(int count) {
        count = Math.min(count, this.maxParticleCount - this.controller.particles.size);
        if (count > 0) {
            this.controller.activateParticles(this.controller.particles.size, count);
            ParallelArray parallelArray = this.controller.particles;
            parallelArray.size += count;
        }
    }

    public ScaledNumericValue getLife() {
        return this.lifeValue;
    }

    public ScaledNumericValue getEmission() {
        return this.emissionValue;
    }

    public RangedNumericValue getDuration() {
        return this.durationValue;
    }

    public RangedNumericValue getDelay() {
        return this.delayValue;
    }

    public ScaledNumericValue getLifeOffset() {
        return this.lifeOffsetValue;
    }

    public boolean isContinuous() {
        return this.continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public EmissionMode getEmissionMode() {
        return this.emissionMode;
    }

    public void setEmissionMode(EmissionMode emissionMode) {
        this.emissionMode = emissionMode;
    }

    public boolean isComplete() {
        if (this.delayTimer >= this.delay && this.durationTimer >= this.duration && this.controller.particles.size == 0) {
            return true;
        }
        return false;
    }

    public float getPercentComplete() {
        if (this.delayTimer < this.delay) {
            return 0.0f;
        }
        return Math.min(1.0f, this.durationTimer / this.duration);
    }

    public void set(RegularEmitter emitter) {
        super.set(emitter);
        this.delayValue.load(emitter.delayValue);
        this.durationValue.load(emitter.durationValue);
        this.lifeOffsetValue.load(emitter.lifeOffsetValue);
        this.lifeValue.load(emitter.lifeValue);
        this.emissionValue.load(emitter.emissionValue);
        this.emission = emitter.emission;
        this.emissionDiff = emitter.emissionDiff;
        this.emissionDelta = emitter.emissionDelta;
        this.lifeOffset = emitter.lifeOffset;
        this.lifeOffsetDiff = emitter.lifeOffsetDiff;
        this.life = emitter.life;
        this.lifeDiff = emitter.lifeDiff;
        this.duration = emitter.duration;
        this.delay = emitter.delay;
        this.durationTimer = emitter.durationTimer;
        this.delayTimer = emitter.delayTimer;
        this.continuous = emitter.continuous;
    }

    public ParticleControllerComponent copy() {
        return new RegularEmitter(this);
    }

    public void write(Json json) {
        super.write(json);
        json.writeValue("continous", Boolean.valueOf(this.continuous));
        json.writeValue("emission", this.emissionValue);
        json.writeValue("delay", this.delayValue);
        json.writeValue("duration", this.durationValue);
        json.writeValue("life", this.lifeValue);
        json.writeValue("lifeOffset", this.lifeOffsetValue);
    }

    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.continuous = ((Boolean) json.readValue("continous", Boolean.TYPE, jsonData)).booleanValue();
        this.emissionValue = (ScaledNumericValue) json.readValue("emission", ScaledNumericValue.class, jsonData);
        this.delayValue = (RangedNumericValue) json.readValue("delay", RangedNumericValue.class, jsonData);
        this.durationValue = (RangedNumericValue) json.readValue("duration", RangedNumericValue.class, jsonData);
        this.lifeValue = (ScaledNumericValue) json.readValue("life", ScaledNumericValue.class, jsonData);
        this.lifeOffsetValue = (ScaledNumericValue) json.readValue("lifeOffset", ScaledNumericValue.class, jsonData);
    }
}
