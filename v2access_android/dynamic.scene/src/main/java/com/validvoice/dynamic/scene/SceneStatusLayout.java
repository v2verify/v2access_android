package com.validvoice.dynamic.scene;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SceneStatusLayout extends LinearLayout {

    public SceneStatusLayout(Context context) {
        super(context);
    }

    public SceneStatusLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SceneStatusLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SceneStatusLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
