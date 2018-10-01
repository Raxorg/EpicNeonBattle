package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;

public class DefaultShaderProvider extends BaseShaderProvider {
    public final Config config;

    public DefaultShaderProvider(Config config) {
        if (config == null) {
            config = new Config();
        }
        this.config = config;
    }

    public DefaultShaderProvider(String vertexShader, String fragmentShader) {
        this(new Config(vertexShader, fragmentShader));
    }

    public DefaultShaderProvider(FileHandle vertexShader, FileHandle fragmentShader) {
        this(vertexShader.readString(), fragmentShader.readString());
    }

    public DefaultShaderProvider() {
        this(null);
    }

    protected Shader createShader(Renderable renderable) {
        return new DefaultShader(renderable, this.config);
    }
}
