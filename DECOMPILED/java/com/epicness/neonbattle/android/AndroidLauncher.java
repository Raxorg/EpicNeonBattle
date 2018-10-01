package com.epicness.neonbattle.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.MainGame;

public class AndroidLauncher extends AndroidApplication {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(Base.kNumFullDistances);
        initialize(new MainGame(), new AndroidApplicationConfiguration());
    }
}
