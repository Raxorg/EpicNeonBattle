package com.epicness.neonbattle;

import com.badlogic.gdx.Game;
import com.epicness.assets.Colors;

public class MainGame extends Game {
    public void create() {
        Colors.create();
        setScreen(new SplashScreen(this));
    }
}
