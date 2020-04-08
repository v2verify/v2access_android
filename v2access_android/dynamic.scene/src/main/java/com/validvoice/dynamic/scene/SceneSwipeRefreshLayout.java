package com.validvoice.dynamic.scene;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class SceneSwipeRefreshLayout extends SwipeRefreshLayout {

    private ViewGroup mContainer;

    public SceneSwipeRefreshLayout(Context context) {
        super(context);
    }

    public SceneSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {

        // The swipe refresh layout has 2 children; the circle refresh indicator
        // and the view container. The container is needed here.
        ViewGroup container = getContainer();

        // The container has 2 children; the empty view and the scrollable view.
        if(container.getChildCount() != 2) {
            throw new RuntimeException("Container must have an Empty view and Content view.");
        }

        View view = container.getChildAt(0);
        if(view.getVisibility() != View.VISIBLE) {
            view = container.getChildAt(1);
        }

        return ViewCompat.canScrollVertically(view, -1);
    }

    private ViewGroup getContainer() {

        // Cache this view
        if(mContainer != null) {
            return mContainer;
        }

        // The container may not be the first view. Need to iterate to find it
        for(int i = 0; i < getChildCount(); ++i) {
            View view = getChildAt(i);
            if(view instanceof ViewGroup) {
                mContainer = (ViewGroup)view;
                break;
            }
        }

        if(mContainer == null) {
            throw new RuntimeException("Container view not found");
        }

        return mContainer;
    }


}
