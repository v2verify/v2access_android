package com.validvoice.dynamic.scene;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class ShadowedLayout extends LinearLayout {

    private static final int DEFAULT_SHADOW_HEIGHT = 4; // dp;

    private View mTopShadowedView;
    private int mTopShadowViewResId = -1;
    private int mTopShadowHeight = -1;
    private Drawable mTopShadowDrawable;

    private View mLeftShadowedView;
    private int mLeftShadowViewResId = -1;
    private int mLeftShadowHeight = -1;
    private Drawable mLeftShadowDrawable;

    private View mRightShadowedView;
    private int mRightShadowViewResId = -1;
    private int mRightShadowHeight = -1;
    private Drawable mRightShadowDrawable;

    private View mBottomShadowedView;
    private int mBottomShadowViewResId = -1;
    private int mBottomShadowHeight = -1;
    private Drawable mBottomShadowDrawable;

    public ShadowedLayout(Context context) {
        super(context);
        initLayout(context, null);
    }

    public ShadowedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context, attrs);
    }

    public ShadowedLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initLayout(context, attrs);
    }

    private void initLayout(Context context, AttributeSet attrs) {

        if (attrs != null) {

            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ShadowedLayout);

            if (ta != null) {
                mTopShadowHeight = ta.getDimensionPixelSize(R.styleable.ShadowedLayout_topShadowedHeight, -1);
                mTopShadowViewResId = ta.getResourceId(R.styleable.ShadowedLayout_topShadowedView, -1);
                mLeftShadowHeight = ta.getDimensionPixelSize(R.styleable.ShadowedLayout_leftShadowedHeight, -1);
                mLeftShadowViewResId = ta.getResourceId(R.styleable.ShadowedLayout_leftShadowedView, -1);
                mRightShadowHeight = ta.getDimensionPixelSize(R.styleable.ShadowedLayout_rightShadowedHeight, -1);
                mRightShadowViewResId = ta.getResourceId(R.styleable.ShadowedLayout_rightShadowedView, -1);
                mBottomShadowHeight = ta.getDimensionPixelSize(R.styleable.ShadowedLayout_bottomShadowedHeight, -1);
                mBottomShadowViewResId = ta.getResourceId(R.styleable.ShadowedLayout_bottomShadowedView, -1);
                ta.recycle();
            }
        }

        final float density = context.getResources().getDisplayMetrics().density;
        if (mTopShadowViewResId != -1 && mTopShadowHeight == -1) {
            mTopShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);
        }
        if (mLeftShadowViewResId != -1 && mLeftShadowHeight == -1) {
            mLeftShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);
        }
        if (mRightShadowViewResId != -1 && mRightShadowHeight == -1) {
            mRightShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);
        }
        if (mBottomShadowViewResId != -1 && mBottomShadowHeight == -1) {
            mBottomShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);
        }

        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(Build.VERSION.SDK_INT >= 21) {
            Resources.Theme theme = getContext().getTheme();
            if (mTopShadowViewResId != -1) {
                mTopShadowedView = findViewById(mTopShadowViewResId);
                setTopShadowDrawable(getResources().getDrawable(R.drawable.top_shadow, theme));
            }
            if (mLeftShadowViewResId != -1) {
                mLeftShadowedView = findViewById(mLeftShadowViewResId);
                setLeftShadowDrawable(getResources().getDrawable(R.drawable.left_shadow, theme));
            }
            if (mRightShadowViewResId != -1) {
                mRightShadowedView = findViewById(mRightShadowViewResId);
                setRightShadowDrawable(getResources().getDrawable(R.drawable.right_shadow, theme));
            }
            if (mBottomShadowViewResId != -1) {
                mBottomShadowedView = findViewById(mBottomShadowViewResId);
                setBottomShadowDrawable(getResources().getDrawable(R.drawable.bottom_shadow, theme));
            }
        } else {
            if (mTopShadowViewResId != -1) {
                mTopShadowedView = findViewById(mTopShadowViewResId);
                setTopShadowDrawable(getResources().getDrawable(R.drawable.top_shadow));
            }
            if (mLeftShadowViewResId != -1) {
                mLeftShadowedView = findViewById(mLeftShadowViewResId);
                setLeftShadowDrawable(getResources().getDrawable(R.drawable.left_shadow));
            }
            if (mRightShadowViewResId != -1) {
                mRightShadowedView = findViewById(mRightShadowViewResId);
                setRightShadowDrawable(getResources().getDrawable(R.drawable.right_shadow));
            }
            if (mBottomShadowViewResId != -1) {
                mBottomShadowedView = findViewById(mBottomShadowViewResId);
                setBottomShadowDrawable(getResources().getDrawable(R.drawable.bottom_shadow));
            }
        }
    }

    public void setTopShadowDrawable(Drawable drawable) {
        mTopShadowDrawable = drawable;
    }

    public void setLeftShadowDrawable(Drawable drawable) {
        mLeftShadowDrawable = drawable;
    }

    public void setRightShadowDrawable(Drawable drawable) {
        mRightShadowDrawable = drawable;
    }

    public void setBottomShadowDrawable(Drawable drawable) {
        mBottomShadowDrawable = drawable;
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);

        if ( mTopShadowDrawable != null && mTopShadowedView != null) {
            final int right = mTopShadowedView.getRight();
            final int top = mTopShadowedView.getBottom();
            final int bottom = mTopShadowedView.getBottom() + mTopShadowHeight;
            final int left = mTopShadowedView.getLeft();
            mTopShadowDrawable.setBounds(left, top, right, bottom);
            mTopShadowDrawable.draw(c);
        }

        if (mLeftShadowDrawable != null && mLeftShadowedView != null) {
            final int right = mLeftShadowedView.getRight() - mLeftShadowHeight;
            final int top = mLeftShadowedView.getTop();
            final int bottom = mLeftShadowedView.getBottom();
            final int left = mLeftShadowedView.getRight();
            mLeftShadowDrawable.setBounds(left, top, right, bottom);
            mLeftShadowDrawable.draw(c);
        }

        if (mRightShadowDrawable != null && mRightShadowedView != null) {
            final int right = mRightShadowedView.getLeft();
            final int top = mRightShadowedView.getTop();
            final int bottom = mRightShadowedView.getTop();
            final int left = mRightShadowedView.getLeft() + mRightShadowHeight;
            mRightShadowDrawable.setBounds(left, top, right, bottom);
            mRightShadowDrawable.draw(c);
        }

        if (mBottomShadowDrawable != null && mBottomShadowedView != null) {
            final int right = mBottomShadowedView.getRight();
            final int top = mBottomShadowedView.getTop() - mBottomShadowHeight;
            final int bottom = mBottomShadowedView.getTop();
            final int left = mBottomShadowedView.getLeft();
            mBottomShadowDrawable.setBounds(left, top, right, bottom);
            mBottomShadowDrawable.draw(c);
        }
    }
}
