package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.utils.Array;

public interface GroupPlug {
    void afterGroup();

    void beforeGroup(Array<Decal> array);
}
