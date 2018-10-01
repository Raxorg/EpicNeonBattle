package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;

public class ParticleEffect implements Disposable {
    private BoundingBox bounds;
    private final Array<ParticleEmitter> emitters;
    private boolean ownsTexture;

    public ParticleEffect() {
        this.emitters = new Array(8);
    }

    public ParticleEffect(ParticleEffect effect) {
        this.emitters = new Array(true, effect.emitters.size);
        int n = effect.emitters.size;
        for (int i = 0; i < n; i++) {
            this.emitters.add(new ParticleEmitter((ParticleEmitter) effect.emitters.get(i)));
        }
    }

    public void start() {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).start();
        }
    }

    public void reset() {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).reset();
        }
    }

    public void update(float delta) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).update(delta);
        }
    }

    public void draw(Batch spriteBatch) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).draw(spriteBatch);
        }
    }

    public void draw(Batch spriteBatch, float delta) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).draw(spriteBatch, delta);
        }
    }

    public void allowCompletion() {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).allowCompletion();
        }
    }

    public boolean isComplete() {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            if (!((ParticleEmitter) this.emitters.get(i)).isComplete()) {
                return false;
            }
        }
        return true;
    }

    public void setDuration(int duration) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ParticleEmitter emitter = (ParticleEmitter) this.emitters.get(i);
            emitter.setContinuous(false);
            emitter.duration = (float) duration;
            emitter.durationTimer = 0.0f;
        }
    }

    public void setPosition(float x, float y) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).setPosition(x, y);
        }
    }

    public void setFlip(boolean flipX, boolean flipY) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).setFlip(flipX, flipY);
        }
    }

    public void flipY() {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).flipY();
        }
    }

    public Array<ParticleEmitter> getEmitters() {
        return this.emitters;
    }

    public ParticleEmitter findEmitter(String name) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ParticleEmitter emitter = (ParticleEmitter) this.emitters.get(i);
            if (emitter.getName().equals(name)) {
                return emitter;
            }
        }
        return null;
    }

    public void save(Writer output) throws IOException {
        int i = 0;
        int n = this.emitters.size;
        int index = 0;
        while (i < n) {
            ParticleEmitter emitter = (ParticleEmitter) this.emitters.get(i);
            int index2 = index + 1;
            if (index > 0) {
                output.write("\n\n");
            }
            emitter.save(output);
            i++;
            index = index2;
        }
    }

    public void load(FileHandle effectFile, FileHandle imagesDir) {
        loadEmitters(effectFile);
        loadEmitterImages(imagesDir);
    }

    public void load(FileHandle effectFile, TextureAtlas atlas) {
        load(effectFile, atlas, null);
    }

    public void load(FileHandle effectFile, TextureAtlas atlas, String atlasPrefix) {
        loadEmitters(effectFile);
        loadEmitterImages(atlas, atlasPrefix);
    }

    public void loadEmitters(FileHandle effectFile) {
        IOException ex;
        Throwable th;
        InputStream input = effectFile.read();
        this.emitters.clear();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(input), GL20.GL_NEVER);
            do {
                try {
                    this.emitters.add(new ParticleEmitter(reader2));
                    if (reader2.readLine() == null) {
                        break;
                    }
                } catch (IOException e) {
                    ex = e;
                    reader = reader2;
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                }
            } while (reader2.readLine() != null);
            StreamUtils.closeQuietly(reader2);
        } catch (IOException e2) {
            ex = e2;
            try {
                throw new GdxRuntimeException("Error loading effect: " + effectFile, ex);
            } catch (Throwable th3) {
                th = th3;
                StreamUtils.closeQuietly(reader);
                throw th;
            }
        }
    }

    public void loadEmitterImages(TextureAtlas atlas) {
        loadEmitterImages(atlas, null);
    }

    public void loadEmitterImages(TextureAtlas atlas, String atlasPrefix) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ParticleEmitter emitter = (ParticleEmitter) this.emitters.get(i);
            String imagePath = emitter.getImagePath();
            if (imagePath != null) {
                String imageName = new File(imagePath.replace('\\', '/')).getName();
                int lastDotIndex = imageName.lastIndexOf(46);
                if (lastDotIndex != -1) {
                    imageName = imageName.substring(0, lastDotIndex);
                }
                if (atlasPrefix != null) {
                    imageName = atlasPrefix + imageName;
                }
                Sprite sprite = atlas.createSprite(imageName);
                if (sprite == null) {
                    throw new IllegalArgumentException("SpriteSheet missing image: " + imageName);
                }
                emitter.setSprite(sprite);
            }
        }
    }

    public void loadEmitterImages(FileHandle imagesDir) {
        this.ownsTexture = true;
        HashMap<String, Sprite> loadedSprites = new HashMap(this.emitters.size);
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ParticleEmitter emitter = (ParticleEmitter) this.emitters.get(i);
            String imagePath = emitter.getImagePath();
            if (imagePath != null) {
                String imageName = new File(imagePath.replace('\\', '/')).getName();
                Sprite sprite = (Sprite) loadedSprites.get(imageName);
                if (sprite == null) {
                    sprite = new Sprite(loadTexture(imagesDir.child(imageName)));
                    loadedSprites.put(imageName, sprite);
                }
                emitter.setSprite(sprite);
            }
        }
    }

    protected Texture loadTexture(FileHandle file) {
        return new Texture(file, false);
    }

    public void dispose() {
        if (this.ownsTexture) {
            int n = this.emitters.size;
            for (int i = 0; i < n; i++) {
                ((ParticleEmitter) this.emitters.get(i)).getSprite().getTexture().dispose();
            }
        }
    }

    public BoundingBox getBoundingBox() {
        if (this.bounds == null) {
            this.bounds = new BoundingBox();
        }
        BoundingBox bounds = this.bounds;
        bounds.inf();
        Iterator it = this.emitters.iterator();
        while (it.hasNext()) {
            bounds.ext(((ParticleEmitter) it.next()).getBoundingBox());
        }
        return bounds;
    }

    public void scaleEffect(float scaleFactor) {
        Iterator it = this.emitters.iterator();
        while (it.hasNext()) {
            ParticleEmitter particleEmitter = (ParticleEmitter) it.next();
            particleEmitter.getScale().setHigh(particleEmitter.getScale().getHighMin() * scaleFactor, particleEmitter.getScale().getHighMax() * scaleFactor);
            particleEmitter.getScale().setLow(particleEmitter.getScale().getLowMin() * scaleFactor, particleEmitter.getScale().getLowMax() * scaleFactor);
            particleEmitter.getVelocity().setHigh(particleEmitter.getVelocity().getHighMin() * scaleFactor, particleEmitter.getVelocity().getHighMax() * scaleFactor);
            particleEmitter.getVelocity().setLow(particleEmitter.getVelocity().getLowMin() * scaleFactor, particleEmitter.getVelocity().getLowMax() * scaleFactor);
            particleEmitter.getGravity().setHigh(particleEmitter.getGravity().getHighMin() * scaleFactor, particleEmitter.getGravity().getHighMax() * scaleFactor);
            particleEmitter.getGravity().setLow(particleEmitter.getGravity().getLowMin() * scaleFactor, particleEmitter.getGravity().getLowMax() * scaleFactor);
            particleEmitter.getWind().setHigh(particleEmitter.getWind().getHighMin() * scaleFactor, particleEmitter.getWind().getHighMax() * scaleFactor);
            particleEmitter.getWind().setLow(particleEmitter.getWind().getLowMin() * scaleFactor, particleEmitter.getWind().getLowMax() * scaleFactor);
            particleEmitter.getSpawnWidth().setHigh(particleEmitter.getSpawnWidth().getHighMin() * scaleFactor, particleEmitter.getSpawnWidth().getHighMax() * scaleFactor);
            particleEmitter.getSpawnWidth().setLow(particleEmitter.getSpawnWidth().getLowMin() * scaleFactor, particleEmitter.getSpawnWidth().getLowMax() * scaleFactor);
            particleEmitter.getSpawnHeight().setHigh(particleEmitter.getSpawnHeight().getHighMin() * scaleFactor, particleEmitter.getSpawnHeight().getHighMax() * scaleFactor);
            particleEmitter.getSpawnHeight().setLow(particleEmitter.getSpawnHeight().getLowMin() * scaleFactor, particleEmitter.getSpawnHeight().getLowMax() * scaleFactor);
            particleEmitter.getXOffsetValue().setLow(particleEmitter.getXOffsetValue().getLowMin() * scaleFactor, particleEmitter.getXOffsetValue().getLowMax() * scaleFactor);
            particleEmitter.getYOffsetValue().setLow(particleEmitter.getYOffsetValue().getLowMin() * scaleFactor, particleEmitter.getYOffsetValue().getLowMax() * scaleFactor);
        }
    }

    public void setEmittersCleanUpBlendFunction(boolean cleanUpBlendFunction) {
        int n = this.emitters.size;
        for (int i = 0; i < n; i++) {
            ((ParticleEmitter) this.emitters.get(i)).setCleansUpBlendFunction(cleanUpBlendFunction);
        }
    }
}
