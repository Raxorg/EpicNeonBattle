package com.badlogic.gdx.backends.android;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import com.badlogic.gdx.Input.Peripheral;

class AndroidOnscreenKeyboard implements OnKeyListener, OnTouchListener {
    final Context context;
    Dialog dialog;
    final Handler handler;
    final AndroidInput input;
    TextView textView;

    public static class PassThroughEditable implements Editable {
        public char charAt(int index) {
            Log.d("Editable", "charAt");
            return '\u0000';
        }

        public int length() {
            Log.d("Editable", "length");
            return 0;
        }

        public CharSequence subSequence(int start, int end) {
            Log.d("Editable", "subSequence");
            return null;
        }

        public void getChars(int start, int end, char[] dest, int destoff) {
            Log.d("Editable", "getChars");
        }

        public void removeSpan(Object what) {
            Log.d("Editable", "removeSpan");
        }

        public void setSpan(Object what, int start, int end, int flags) {
            Log.d("Editable", "setSpan");
        }

        public int getSpanEnd(Object tag) {
            Log.d("Editable", "getSpanEnd");
            return 0;
        }

        public int getSpanFlags(Object tag) {
            Log.d("Editable", "getSpanFlags");
            return 0;
        }

        public int getSpanStart(Object tag) {
            Log.d("Editable", "getSpanStart");
            return 0;
        }

        public <T> T[] getSpans(int arg0, int arg1, Class<T> cls) {
            Log.d("Editable", "getSpans");
            return null;
        }

        public int nextSpanTransition(int start, int limit, Class type) {
            Log.d("Editable", "nextSpanTransition");
            return 0;
        }

        public Editable append(CharSequence text) {
            Log.d("Editable", "append: " + text);
            return this;
        }

        public Editable append(char text) {
            Log.d("Editable", "append: " + text);
            return this;
        }

        public Editable append(CharSequence text, int start, int end) {
            Log.d("Editable", "append: " + text);
            return this;
        }

        public void clear() {
            Log.d("Editable", "clear");
        }

        public void clearSpans() {
            Log.d("Editable", "clearSpanes");
        }

        public Editable delete(int st, int en) {
            Log.d("Editable", "delete, " + st + ", " + en);
            return this;
        }

        public InputFilter[] getFilters() {
            Log.d("Editable", "getFilters");
            return new InputFilter[0];
        }

        public Editable insert(int where, CharSequence text) {
            Log.d("Editable", "insert: " + text);
            return this;
        }

        public Editable insert(int where, CharSequence text, int start, int end) {
            Log.d("Editable", "insert: " + text);
            return this;
        }

        public Editable replace(int st, int en, CharSequence text) {
            Log.d("Editable", "replace: " + text);
            return this;
        }

        public Editable replace(int st, int en, CharSequence source, int start, int end) {
            Log.d("Editable", "replace: " + source);
            return this;
        }

        public void setFilters(InputFilter[] filters) {
            Log.d("Editable", "setFilters");
        }
    }

    public AndroidOnscreenKeyboard(Context context, Handler handler, AndroidInput input) {
        this.context = context;
        this.handler = handler;
        this.input = input;
    }

    Dialog createDialog() {
        this.textView = createView(this.context);
        this.textView.setOnKeyListener(this);
        this.textView.setLayoutParams(new LayoutParams(-1, -2, 80));
        this.textView.setFocusable(true);
        this.textView.setFocusableInTouchMode(true);
        this.textView.setImeOptions(this.textView.getImeOptions() | 268435456);
        FrameLayout layout = new FrameLayout(this.context);
        layout.setLayoutParams(new ViewGroup.LayoutParams(-1, 0));
        layout.addView(this.textView);
        layout.setOnTouchListener(this);
        this.dialog = new Dialog(this.context, 16973841);
        this.dialog.setContentView(layout);
        return this.dialog;
    }

    public static TextView createView(Context context) {
        return new TextView(context) {
            Editable editable = new PassThroughEditable();

            protected boolean getDefaultEditable() {
                return true;
            }

            public Editable getEditableText() {
                return this.editable;
            }

            protected MovementMethod getDefaultMovementMethod() {
                return ArrowKeyMovementMethod.getInstance();
            }

            public boolean onKeyDown(int keyCode, KeyEvent event) {
                Log.d("Test", "down keycode: " + event.getKeyCode());
                return super.onKeyDown(keyCode, event);
            }

            public boolean onKeyUp(int keyCode, KeyEvent event) {
                Log.d("Test", "up keycode: " + event.getKeyCode());
                return super.onKeyUp(keyCode, event);
            }
        };
    }

    public void setVisible(boolean visible) {
        if (visible && this.dialog != null) {
            this.dialog.dismiss();
            this.dialog = null;
        }
        if (visible && this.dialog == null && !this.input.isPeripheralAvailable(Peripheral.HardwareKeyboard)) {
            this.handler.post(new Runnable() {
                public void run() {
                    AndroidOnscreenKeyboard.this.dialog = AndroidOnscreenKeyboard.this.createDialog();
                    AndroidOnscreenKeyboard.this.dialog.show();
                    AndroidOnscreenKeyboard.this.handler.post(new Runnable() {
                        public void run() {
                            AndroidOnscreenKeyboard.this.dialog.getWindow().setSoftInputMode(32);
                            InputMethodManager input = (InputMethodManager) AndroidOnscreenKeyboard.this.context.getSystemService("input_method");
                            if (input != null) {
                                input.showSoftInput(AndroidOnscreenKeyboard.this.textView, 2);
                            }
                        }
                    });
                    final View content = AndroidOnscreenKeyboard.this.dialog.getWindow().findViewById(16908290);
                    content.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                        private int keyboardHeight;
                        private boolean keyboardShowing;
                        int[] screenloc = new int[2];

                        public boolean onPreDraw() {
                            content.getLocationOnScreen(this.screenloc);
                            this.keyboardHeight = Math.abs(this.screenloc[1]);
                            if (this.keyboardHeight > 0) {
                                this.keyboardShowing = true;
                            }
                            if (this.keyboardHeight == 0 && this.keyboardShowing) {
                                AndroidOnscreenKeyboard.this.dialog.dismiss();
                                AndroidOnscreenKeyboard.this.dialog = null;
                            }
                            return true;
                        }
                    });
                }
            });
        } else if (!visible && this.dialog != null) {
            this.dialog.dismiss();
        }
    }

    public boolean onTouch(View view, MotionEvent e) {
        return false;
    }

    public boolean onKey(View view, int keycode, KeyEvent e) {
        return false;
    }
}
