package com.badlogic.gdx.backends.android;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Build.VERSION;
import android.text.ClipboardManager;
import com.badlogic.gdx.utils.Clipboard;

public class AndroidClipboard implements Clipboard {
    Context context;

    public AndroidClipboard(Context context) {
        this.context = context;
    }

    public String getContents() {
        if (VERSION.SDK_INT < 11) {
            ClipboardManager clipboard = (ClipboardManager) this.context.getSystemService("clipboard");
            if (clipboard.getText() == null) {
                return null;
            }
            return clipboard.getText().toString();
        }
        ClipData clip = ((android.content.ClipboardManager) this.context.getSystemService("clipboard")).getPrimaryClip();
        if (clip == null) {
            return null;
        }
        CharSequence text = clip.getItemAt(0).getText();
        if (text != null) {
            return text.toString();
        }
        return null;
    }

    public void setContents(final String contents) {
        try {
            ((Activity) this.context).runOnUiThread(new Runnable() {
                public void run() {
                    if (VERSION.SDK_INT < 11) {
                        ((ClipboardManager) AndroidClipboard.this.context.getSystemService("clipboard")).setText(contents);
                    } else {
                        ((android.content.ClipboardManager) AndroidClipboard.this.context.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(contents, contents));
                    }
                }
            });
        } catch (Exception e) {
        }
    }
}
