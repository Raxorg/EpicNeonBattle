package com.frontanilla.epicneonbattle;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.frontanilla.epicneonbattle.neonbattle.MainGame;

public class AndroidLauncher extends AndroidApplication {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        initialize(new MainGame(), new AndroidApplicationConfiguration());
    }
}
