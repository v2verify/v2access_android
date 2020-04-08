package com.validvoice.dynamic.scene.listeners;

import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;

public abstract class OnWasCheckedChangeListener implements CompoundButton.OnCheckedChangeListener, View.OnTouchListener {

    private CompoundButton mCompoundButton;
    private boolean mWasTouched;

    public OnWasCheckedChangeListener(CompoundButton button) {
        mCompoundButton = button;
        mWasTouched = false;
        if( mCompoundButton != null ) {
            mCompoundButton.setOnTouchListener(this);
            mCompoundButton.setOnCheckedChangeListener(this);
        }
    }

    public CompoundButton getCompoundButton() {
        return mCompoundButton;
    }

    public abstract void onWasCheckedChanged(CompoundButton buttonView, boolean isChecked);

    @Override
    final public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if( mWasTouched ) {
            mWasTouched = false;
            onWasCheckedChanged(buttonView, isChecked);
        }
    }

    @Override
    final public boolean onTouch(View v, MotionEvent event) {
        mWasTouched = true;
        return false;
    }
}
