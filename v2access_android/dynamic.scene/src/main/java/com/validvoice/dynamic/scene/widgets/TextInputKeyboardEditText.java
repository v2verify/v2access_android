package com.validvoice.dynamic.scene.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

public class TextInputKeyboardEditText extends TextInputEditText {

    /**
     * Keyboard Listener
     */
    private KeyboardListener mKeyboardListener;

    public TextInputKeyboardEditText(Context context) {
        super(context);
        init();
    }

    public TextInputKeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextInputKeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
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
                        mKeyboardListener.onStateChanged(TextInputKeyboardEditText.this, false);
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
        void onStateChanged(TextInputKeyboardEditText textInputKeyboardEditText, boolean showing);
    }

}

