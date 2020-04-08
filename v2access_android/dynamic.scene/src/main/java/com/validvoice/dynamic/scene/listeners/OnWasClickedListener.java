package com.validvoice.dynamic.scene.listeners;

import android.view.MotionEvent;
import android.view.View;

public abstract class OnWasClickedListener implements View.OnClickListener, View.OnTouchListener {

    private boolean mWasTouched;

    public OnWasClickedListener(View view) {
        mWasTouched = false;
        if( view != null ) {
            view.setOnTouchListener(this);
            view.setOnClickListener(this);
        }
    }

    public abstract void onWasClicked(View view);

    @Override
    final public void onClick(View view) {
        if( mWasTouched ) {
            mWasTouched = false;
            onWasClicked(view);
        }
    }

    @Override
    final public boolean onTouch(View view, MotionEvent event) {
        mWasTouched = true;
        return false;
    }
}
