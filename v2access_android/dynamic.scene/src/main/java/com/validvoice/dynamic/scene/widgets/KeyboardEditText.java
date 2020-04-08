package com.validvoice.dynamic.scene.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public class KeyboardEditText extends AppCompatEditText {

    /**
     * Keyboard Listener
     */
    private KeyboardListener mKeyboardListener;

    public KeyboardEditText(Context context) {
        super(context);
        init();
    }

    public KeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (mKeyboardListener != null) {
                        clearFocus();
                        mKeyboardListener.onStateChanged(KeyboardEditText.this, false);
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused && mKeyboardListener != null && getVisibility() == VISIBLE) {
            mKeyboardListener.onStateChanged(this, true);
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK &&
                event.getAction() == KeyEvent.ACTION_UP) {
            if (mKeyboardListener != null) {
                clearFocus();
                mKeyboardListener.onStateChanged(this, false);
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnKeyboardListener(KeyboardListener keyboardListener) {
        mKeyboardListener = keyboardListener;
    }

    public interface KeyboardListener {
        void onStateChanged(KeyboardEditText keyboardEditText, boolean showing);
    }

}

