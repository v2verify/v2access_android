package com.validvoice.dynamic.scene.listeners;

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

public abstract class OnItemWasSelectedListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener{

    private boolean mWasTouched;

    public OnItemWasSelectedListener(AdapterView adapterView) {
        mWasTouched=false;
        if( adapterView != null ) {
            adapterView.setOnTouchListener(this);
            adapterView.setOnItemSelectedListener(this);
        }
    }

    public abstract void onItemWasSelected(AdapterView<?> parent, View view, int position, long id);

    @Override
    final public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        if(mWasTouched) {
            mWasTouched = false;
            onItemWasSelected(parent, view, position, id);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){

    }

    @Override
    final public boolean onTouch(View v, MotionEvent event){
        mWasTouched = true;
        return false;
    }
}
