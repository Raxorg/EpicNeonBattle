package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import com.badlogic.gdx.Application;
import java.util.ArrayList;

public class AndroidInputThreePlus extends AndroidInput implements OnGenericMotionListener {
    ArrayList<OnGenericMotionListener> genericMotionListeners = new ArrayList();
    private final AndroidMouseHandler mouseHandler;

    public AndroidInputThreePlus(Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
        super(activity, context, view, config);
        if (view instanceof View) {
            ((View) view).setOnGenericMotionListener(this);
        }
        this.mouseHandler = new AndroidMouseHandler();
    }

    public boolean onGenericMotion(View view, MotionEvent event) {
        if (this.mouseHandler.onGenericMotion(event, this)) {
            return true;
        }
        int n = this.genericMotionListeners.size();
        for (int i = 0; i < n; i++) {
            if (((OnGenericMotionListener) this.genericMotionListeners.get(i)).onGenericMotion(view, event)) {
                return true;
            }
        }
        return false;
    }

    public void addGenericMotionListener(OnGenericMotionListener listener) {
        this.genericMotionListeners.add(listener);
    }
}
