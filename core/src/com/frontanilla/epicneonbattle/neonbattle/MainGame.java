package com.frontanilla.epicneonbattle.neonbattle;

import com.badlogic.gdx.Game;
import com.frontanilla.epicneonbattle.assets.Colors;

public class MainGame extends Game {
    public void create() {
        Colors.create();
        setScreen(new SplashScreen(this));
    }
}
