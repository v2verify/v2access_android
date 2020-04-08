package com.validvoice.dynamic.scene;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SceneLayout extends LinearLayout implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_ALLOW_THEME_ANIMATIONS = "pref_allow_theme_animations";

    private static final int SCENE_INFO_TAG_ID = 0xbeadface;

    private static final int DEFAULT_SHADOW_HEIGHT = 4; // dp;

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    private static final float DEFAULT_MAX_SCRIM_OPACITY = 0.8f;

    /**
     * Header View Details
     */
    private ViewGroup mHeaderView;
    private int mHeaderViewResId = -1;
    private int mHeaderShadowHeight = -1;
    private Drawable mHeaderShadowDrawable;
    private SparseArray<SceneViewInfo> mHeaderActors;
    private SceneViewInfo mHeaderLiveSceneViewInfo;

    /**
     * Footer View Details
     */
    private ViewGroup mFooterView;
    private int mFooterViewResId = -1;
    private int mFooterShadowHeight = -1;
    private Drawable mFooterShadowDrawable;
    private SparseArray<SceneViewInfo> mFooterActors;
    private SceneViewInfo mFooterLiveSceneViewInfo;

    /**
     * Content View Details
     */
    private int mContentViewResId = -1;
    private SparseArray<SceneFragmentInfo> mContentScenes;

    /**
     *
     */
    private SceneController mSceneController;

    /**
     *
     */
    private SparseArray<SceneActor> mSceneActors;

    /**
     *
     */
    private List<ISceneActionListener> mSceneActionListeners;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private float mScrimOpacity;
    private float mMaxScrimOpacity;
    private Paint mScrimPaint = new Paint();
    private final Rect mScrimZone = new Rect();

    private boolean mUseScrim = false;
    private boolean mUpdateScrim = false;
    //private boolean mScrimFragment = false;
    //private View mScrimFragmentView;

    private Queue<SceneAction> mPendingActions = new LinkedList<>();
    final private Object mPendingActionsLock = new Object();

    private boolean mAllowAnimations = true;

    private LayoutInflater mInflater;

    private class SceneViewInfo {
        int id;
        SceneActor sceneActor;
        View view;
        boolean expanded;
        boolean changing;
        boolean hidden;
        int top, bottom, left, right;
        ValueAnimator animator;
        SceneViewInfo(int id, SceneActor sceneActor, View view) {
            this.id = id;
            this.sceneActor = sceneActor;
            this.view = view;
            this.expanded = false;
            this.changing = false;
            this.hidden = false;
            this.top = this.bottom = this.left = this.right = 0;
            this.animator = null;
        }
    }

    private class SceneFragmentInfo {
        int id;
        SceneActor sceneActor;
        ISceneFragment fragment;
        boolean expanded;
        boolean hidden;
        SceneFragmentInfo(int id, SceneActor sceneActor, ISceneFragment fragment) {
            this.id = id;
            this.sceneActor = sceneActor;
            this.fragment = fragment;
            this.expanded = false;
            this.hidden = false;
        }
    }

    private class SceneAction {

        static final int DATA = 0;
        static final int EXPAND = 1;
        static final int COLLAPSE = 2;
        static final int CANCEL = 3;
        static final int HIDE_HEADER = 4;
        static final int SHOW_HEADER = 5;
        static final int HIDE_FOOTER = 6;
        static final int SHOW_FOOTER = 7;

        int     sceneActorId;
        int     sceneActionType;
        int     sceneActionId;
        Object  sceneActionData;

        SceneAction(int a, int b) {
            sceneActorId = a;
            sceneActionType = b;
            sceneActionId = -1;
            sceneActionData = null;
        }

        SceneAction(int a, int b, int c) {
            sceneActorId = a;
            sceneActionType = b;
            sceneActionId = c;
            sceneActionData = null;
        }

        SceneAction(int a, int b, int c, Object d) {
            sceneActorId = a;
            sceneActionType = b;
            sceneActionId = c;
            sceneActionData = d;
        }

    }

    protected abstract static class SceneActor {

        private static final int ACTOR_UNKNOWN = 0;
        private static final int ACTOR_OPENING = 1;
        private static final int ACTOR_OPENED = 2;
        private static final int ACTOR_CLOSING = 3;
        private static final int ACTOR_CLOSED = 4;

        protected static final int ACTION_ACTOR = 0;
        protected static final int HEADER_ACTOR = 1;
        protected static final int FOOTER_ACTOR = 2;
        protected static final int FRAGMENT_ACTOR = 3;
        protected static final int ACTIVITY_ACTOR = 4;
        protected static final int BACKGROUND_ACTOR = 5;

        protected static final int OVERLAY_NONE = 0;
        protected static final int OVERLAY_VIEW = 1;

        protected static final int SCRIM_OFF = 0;
        protected static final int SCRIM_ON = 1;
        protected static final int SCRIM_ON_LOCKED = 2;

        int id;
        int type;
        int scrim_type;
        int status = ACTOR_UNKNOWN;
        SceneController controller;
        SceneViewInfo overlainViewInfo;

        SceneActor(SceneController controller, int id, int type, int scrim_type) {
            this.controller = controller;
            this.id = id;
            this.type = type;
            this.scrim_type = scrim_type;
            this.overlainViewInfo = null;
        }
        public int getType() { return type; }
        public int getStatus() { return status; }
        public boolean isActorAvailable() { return status >= ACTOR_OPENING && status <= ACTOR_CLOSED; }
        public boolean allowOverlay(int id) { return false; }
        boolean doAllowOverlay(int id) {
            return allowOverlay(controller.resolveOutId(id));
        }
        public int overlayType() { return OVERLAY_NONE; }
        public void onActorOpening() { }
        public void onActorOpened() { }
        public void onActorClosing() { }
        public void onActorClosed() {}
        public void onActorShowing() { }
        public void onActorVisible() { }
        public void onActorHiding() { }
        public void onActorGone() { }
        public abstract boolean onActorAction(int id, Object data);

        void setOverlayViewInfo(SceneViewInfo overlayViewInfo) {
            overlainViewInfo = overlayViewInfo;
        }

        SceneViewInfo getOverlayViewInfo() {
            return overlainViewInfo;
        }

        boolean hasOverlayView() {
            return overlainViewInfo != null;
        }
    }

    public abstract static class SceneActorView extends SceneActor {
        SceneActorView(SceneController controller, int id, int type, int scrim_type) {
            super(controller, id, type, scrim_type);
        }
        public abstract View onCreateView(LayoutInflater inflater, ViewGroup parent, boolean addToRoot);
        public abstract void onDestroyView(View view);
        @Override public boolean onActorAction(int id, Object data) { return false; }
    }

    public abstract static class SceneActorHeader extends SceneActorView {

        private int mToolbarId;
        private float mElevation;

        public SceneActorHeader(SceneController controller, int id, int scrim_type) {
            super(controller, id, HEADER_ACTOR, scrim_type);
        }

        public void setBlendedToolbarId(@IdRes int toolbar_id) {
            mToolbarId = toolbar_id;
        }

        public int getBlendedToolbarId() {
            return mToolbarId;
        }

        @CallSuper
        @Override
        public void onActorOpening() {
            AppCompatActivity activity = controller.getActivity();
            if(Build.VERSION.SDK_INT >= 21 && mToolbarId != 0) {
                AppBarLayout view = activity.findViewById(mToolbarId);
                if(view == null) return;
                mElevation = view.getElevation();
                view.setElevation(0);
            }
            activity.invalidateOptionsMenu();
        }

        @CallSuper
        @Override
        public void onActorClosing() {
            AppCompatActivity activity = controller.getActivity();
            if(Build.VERSION.SDK_INT >= 21 && mToolbarId != 0) {
                AppBarLayout view = (AppBarLayout)activity.findViewById(mToolbarId);
                if(view == null) return;
                view.setElevation(mElevation);
            }
            activity.invalidateOptionsMenu();
        }
    }

    public static class FreeScrimSceneActorHeader extends SceneActorHeader {
        public FreeScrimSceneActorHeader(SceneController controller, int id) {
            super(controller, id, SceneActor.SCRIM_ON);
        }
        public final View onCreateView(LayoutInflater inflater, ViewGroup parent, boolean addToRoot) { return null; }
        public final void onDestroyView(View view) {}
    }

    public abstract static class SceneActorFooter extends SceneActorView {
        public SceneActorFooter(SceneController controller, int id, int scrim_type) {
            super(controller, id, FOOTER_ACTOR, scrim_type);
        }
    }

    public abstract static class SceneActionActor extends SceneActor {
        public SceneActionActor(SceneController controller, int id) {
            super(controller, id, ACTION_ACTOR, SceneActor.SCRIM_OFF);
        }
    }

    public abstract static class FragmentActor <T extends SceneController, U extends SceneFragment> extends SceneActor {

        public static final int TRANSITION_TOP_DOWN = 1;
        public static final int TRANSITION_BOTTOM_UP = 2;
        public static final int TRANSITION_LEFT_TO_RIGHT = 3;
        public static final int TRANSITION_RIGHT_TO_LEFT = 4;

        Class<U> clazz;
        boolean home;
        int transition;
        boolean retainSceneState;

        FragmentActor(T controller, int id, int type, Class<U> clazz, int transition, boolean home) {
            super(controller, id, type, SceneActor.SCRIM_OFF);
            this.clazz = clazz;
            this.home = home;
            this.transition = transition;
            this.retainSceneState = false;
        }
        public final boolean isHomeFragment() { return home; }

        public final void retainSceneState(boolean retainState) {
            retainSceneState = retainState;
        }

        public abstract void onAttachingFragment(U fragment);
        public abstract void onDetachingFragment(U fragment);

        public abstract void onAttachedFragment();
        public abstract void onDetachedFragment();

        public void onSaveSceneState(Bundle outState) {}
    }

    public abstract static class FragmentHomeActor <T extends SceneController, U extends SceneFragment> extends FragmentActor<T, U> {

        private U mFragment;
        private int mToolbarId;
        private float mElevation;

        public FragmentHomeActor(T controller, int id, Class<U> clazz) {
            super(controller, id, FRAGMENT_ACTOR, clazz, TRANSITION_RIGHT_TO_LEFT, true);
        }

        public void setBlendedToolbarId(@IdRes int toolbar_id) {
            mToolbarId = toolbar_id;
        }

        public int getBlendedToolbarId() {
            return mToolbarId;
        }

        @Override
        public void onAttachingFragment(U fragment) {
            mFragment = fragment;
            if(Build.VERSION.SDK_INT >= 21 && mToolbarId != 0) {
                AppBarLayout view = (AppBarLayout)controller.getActivity().findViewById(mToolbarId);
                if(view == null) return;
                mElevation = view.getElevation();
                view.setElevation(0);
            }
        }

        @Override
        public void onAttachedFragment() {

        }

        @CallSuper
        @Override
        public void onDetachingFragment(U fragment) {
            mFragment = null;
            if(Build.VERSION.SDK_INT >= 21 && mToolbarId != 0) {
                AppBarLayout view = (AppBarLayout)controller.getActivity().findViewById(mToolbarId);
                if(view == null) return;
                view.setElevation(mElevation);
            }
        }

        @Override
        public void onDetachedFragment() {
        }

        @Override
        public boolean onActorAction(int id, Object data) {
            if(mFragment != null) {
                if(id == R.id.action_permission && data instanceof SceneController.PermissionAction) {
                    SceneController.PermissionAction pa = (SceneController.PermissionAction)data;
                    mFragment.onRequestPermissionsResult(pa.RequestCode, pa.Permissions, pa.GrantResults);
                    return true;
                }
                return mFragment.onActorAction(id, data);
            }
            return false;
        }

        public U getFragment() {
            return mFragment;
        }
    }

    public abstract static class FragmentSceneActor <T extends SceneController, U extends SceneFragment> extends FragmentActor<T, U> {

        private U mFragment;
        private int mToolbarId;
        private float mElevation;

        public FragmentSceneActor(T controller, int id, Class<U> clazz, int transition) {
            super(controller, id, FRAGMENT_ACTOR, clazz, transition, false);
            mToolbarId = 0;
            mElevation = 0;
        }

        public void setBlendedToolbarId(@IdRes int toolbar_id) {
            mToolbarId = toolbar_id;
        }

        public int getBlendedToolbarId() {
            return mToolbarId;
        }

        @CallSuper
        @Override
        public void onAttachingFragment(U fragment) {
            mFragment = fragment;
            if(Build.VERSION.SDK_INT >= 21 && mToolbarId != 0) {
                AppBarLayout view = (AppBarLayout)controller.getActivity().findViewById(mToolbarId);
                if(view == null) return;
                mElevation = view.getElevation();
                view.setElevation(0);
            }
        }

        @Override
        public void onAttachedFragment() {

        }

        @CallSuper
        @Override
        public void onDetachingFragment(U fragment) {
            if(Build.VERSION.SDK_INT >= 21 && mToolbarId != 0) {
                AppBarLayout view = (AppBarLayout)controller.getActivity().findViewById(mToolbarId);
                if(view == null) return;
                view.setElevation(mElevation);
            }
            mFragment = null;
        }

        @Override
        public void onDetachedFragment() {

        }

        @CallSuper
        @Override
        public boolean onActorAction(int id, Object data) {
            if(mFragment != null) {
                if(id == R.id.action_permission && data instanceof SceneController.PermissionAction) {
                    SceneController.PermissionAction pa = (SceneController.PermissionAction)data;
                    mFragment.onRequestPermissionsResult(pa.RequestCode, pa.Permissions, pa.GrantResults);
                    return true;
                }
                return mFragment.onActorAction(id, data);
            }
            return false;
        }

        public U getFragment() {
            return mFragment;
        }
    }

    public abstract static class BackgroundActor <T extends SceneController, U extends RetainedSceneFragment> extends SceneActor {

        String tag;
        Class<U> clazz;

        public BackgroundActor(T controller, int id, String tag, Class<U> clazz) {
            super(controller, id, BACKGROUND_ACTOR, SceneActor.SCRIM_OFF);
            this.clazz = clazz;
            this.tag = tag;
        }

    }

    //public static class SceneScrimFragment extends SceneFragment {
    //
    //    public SceneScrimFragment() {
    //        // Required empty public constructor
    //    }
    //
    //    @Override
    //    public Animator onCreateAnimator( int transit, boolean enter, int nextAnim ) {
    //        Animator animator = super.onCreateAnimator( transit, enter, nextAnim );
    //        final View view = getView();
    //        if(view != null) {
    //            final int start;
    //            final int end;
    //            if (enter) {
    //                final int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    //                final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
    //                view.measure(widthSpec, heightSpec);
    //                start = 0;
    //                end = view.getMeasuredHeight();
    //            } else {
    //                start = view.getHeight();
    //                end = 0;
    //            }
    //            final ValueAnimator valueAnimator = getSceneController().getSceneLayout().slideFragmentAnimator(view, start, end, enter);
    //            if (valueAnimator != null) {
    //                valueAnimator.start();
    //            }
    //        }
    //        return animator;
    //    }
    //
    //}

    //public abstract static class ActivitySceneActor <T extends SceneController, U extends TSceneActivity<T>> extends SceneActor {
    //    Class<U> clazz;
    //    public ActivitySceneActor(T controller, int id, Class<U> clazz) {
    //        super(controller, id, ACTIVITY_ACTOR, false);
    //        this.clazz = clazz;
    //    }
    //
    //    public abstract Intent onNewInstanceIntent(Intent createIntent);
    //}

    public static class SimpleSceneActionListener implements ISceneActionListener {

        @Override
        public void onActorOpening(int actorId) {

        }

        @Override
        public void onActorOpened(int actorId) {

        }

        @Override
        public void onActorClosing(int actorId) {

        }

        @Override
        public void onActorClosed(int actorId) {

        }

        @Override
        public void onActorShowing(int actorId) {

        }

        @Override
        public void onActorVisible(int actorId) {

        }

        @Override
        public void onActorHiding(int actorId) {

        }

        @Override
        public void onActorGone(int actorId) {

        }
    }

    public SceneLayout(Context context) {
        super(context);
        initLayout(context, null);
    }

    public SceneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context, attrs);
    }

    public SceneLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SceneLayout(Context context, AttributeSet attrs,  int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initLayout(context, attrs);
    }

    private void initLayout(Context context, AttributeSet attrs) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp != null) {
            mAllowAnimations = sp.getBoolean(PREF_ALLOW_THEME_ANIMATIONS, true);
            sp.registerOnSharedPreferenceChangeListener(this);
        }

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (attrs != null) {

            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SceneLayout);

            if (ta != null) {
                mHeaderShadowHeight = ta.getDimensionPixelSize(R.styleable.SceneLayout_headerShadowedHeight, -1);
                mHeaderViewResId = ta.getResourceId(R.styleable.SceneLayout_headerView, -1);
                mHeaderShadowDrawable = ta.getDrawable(R.styleable.SceneLayout_headerShadowDrawable);
                mFooterShadowHeight = ta.getDimensionPixelSize(R.styleable.SceneLayout_footerShadowedHeight, -1);
                mFooterViewResId = ta.getResourceId(R.styleable.SceneLayout_footerView, -1);
                mContentViewResId = ta.getResourceId(R.styleable.SceneLayout_contentView, -1);
                mFooterShadowDrawable = ta.getDrawable(R.styleable.SceneLayout_footerShadowDrawable);
                mScrimColor = ta.getColor(R.styleable.SceneLayout_scrimColor, DEFAULT_SCRIM_COLOR);
                mMaxScrimOpacity = ta.getFloat(R.styleable.SceneLayout_maxScrimOpacity, DEFAULT_MAX_SCRIM_OPACITY);
                ta.recycle();
            }
        }

        if(mContentViewResId == -1) {
            throw new IllegalArgumentException("contentView MUST be a value a valid content view id within the SceneLayout");
        }

        final float density = context.getResources().getDisplayMetrics().density;
        if (mHeaderViewResId != -1 && mHeaderShadowHeight == -1) {
            mHeaderShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);
        }
        if (mFooterViewResId != -1 && mFooterShadowHeight == -1) {
            mFooterShadowHeight = (int) (DEFAULT_SHADOW_HEIGHT * density + 0.5f);
        }
        if(mMaxScrimOpacity < 0 || mMaxScrimOpacity > 1.0f) {
            throw new IllegalArgumentException("maxScrimOpacity MUST be a value between 0.0f and 1.0f");
        }

        mSceneActors = new SparseArray<>();
        mContentScenes = new SparseArray<>();
        mSceneActionListeners = new ArrayList<>();

        setWillNotDraw(false);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mHeaderViewResId != -1) {
            View headerView = findViewById(mHeaderViewResId);
            if(!(headerView instanceof ViewGroup)) {
                throw new IllegalStateException("StackedLayout.headerView must be a ViewGroup container.");
            }
            headerView.setVisibility(GONE);
            mHeaderView = (ViewGroup)headerView;
            mHeaderActors = new SparseArray<>();
            setHeaderShadowResourceId(R.drawable.top_shadow);
        }
        if (mFooterViewResId != -1) {
            View footerView = findViewById(mFooterViewResId);
            if(!(footerView instanceof ViewGroup)) {
                throw new IllegalStateException("StackedLayout.footerView must be a ViewGroup container.");
            }
            footerView.setVisibility(GONE);
            mFooterView = (ViewGroup)footerView;
            mFooterActors = new SparseArray<>();
            setFooterShadowResourceId(R.drawable.bottom_shadow);
        }
    }

    public void setHeaderShadowResourceId(int resourceId) {
        if(mHeaderShadowDrawable == null) {
            if(Build.VERSION.SDK_INT >= 21) {
                mHeaderShadowDrawable = getResources().getDrawable(resourceId, getContext().getTheme());
            } else {
                mHeaderShadowDrawable = getResources().getDrawable(resourceId);
            }
        }
    }

    @SuppressWarnings("unused")
    public void setHeaderShadowDrawable(Drawable drawable) {
        if(mHeaderShadowDrawable == null) {
            mHeaderShadowDrawable = drawable;
        }
    }

    public void setFooterShadowResourceId(int resourceId) {
        if(mFooterShadowDrawable == null) {
            if(Build.VERSION.SDK_INT >= 21) {
                mFooterShadowDrawable = getResources().getDrawable(resourceId, getContext().getTheme());
            } else {
                mFooterShadowDrawable = getResources().getDrawable(resourceId);
            }
        }
    }

    @SuppressWarnings("unused")
    public void setFooterShadowDrawable(Drawable drawable) {
        if(mFooterShadowDrawable == null) {
            mFooterShadowDrawable = drawable;
        }
    }

    @SuppressWarnings("unused")
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    @SuppressWarnings("unused")
    public int getScrimColor() {
        return mScrimColor;
    }

    public int getContentViewId() {
        return mContentViewResId;
    }

    public void setSceneController(SceneController controller) {
        mSceneController = controller;
    }

    public void addSceneActionListener(ISceneActionListener listener) {
        if(mSceneActionListeners.contains(listener)) return;
        mSceneActionListeners.add(listener);
    }

    public void removeSceneActionListener(ISceneActionListener listener) {
        mSceneActionListeners.remove(listener);
    }

    private void dispatchOnActorOpening(SceneActor sceneActor) {
        sceneActor.status = SceneActor.ACTOR_OPENING;
        sceneActor.onActorOpening();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorOpening(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private void dispatchOnActorOpened(SceneActor sceneActor) {
        sceneActor.status = SceneActor.ACTOR_OPENED;
        sceneActor.onActorOpened();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorOpened(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private void dispatchOnActorClosing(SceneActor sceneActor) {
        sceneActor.status = SceneActor.ACTOR_CLOSING;
        sceneActor.onActorClosing();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorClosing(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private void dispatchOnActorClosed(SceneActor sceneActor) {
        sceneActor.status = SceneActor.ACTOR_CLOSED;
        sceneActor.onActorClosed();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorClosed(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private void dispatchOnActorHiding(SceneActor sceneActor) {
        sceneActor.onActorHiding();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorHiding(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private void dispatchOnActorGone(SceneActor sceneActor) {
        sceneActor.onActorGone();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorGone(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private void dispatchOnActorShowing(SceneActor sceneActor) {
        sceneActor.onActorShowing();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorShowing(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private void dispatchOnActorVisible(SceneActor sceneActor) {
        sceneActor.onActorVisible();
        for(ISceneActionListener listener : mSceneActionListeners) {
            listener.onActorVisible(sceneActor.controller.resolveOutId(sceneActor.id));
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    private boolean isTouchInScrimZone(int x, int y) {
        return mScrimZone.contains(x, y);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mUseScrim) {
            if (mHeaderLiveSceneViewInfo != null && mHeaderLiveSceneViewInfo.sceneActor.scrim_type != SceneActor.SCRIM_OFF) {
                final int action = ev.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (isTouchInScrimZone((int) ev.getX(), (int) ev.getY())) {
                        if (mHeaderLiveSceneViewInfo.sceneActor.scrim_type == SceneActor.SCRIM_ON && mSceneController != null) {
                            mSceneController.onCloseScene(false);
                        }
                        return true;
                    }
                }
                return false;
            } else if (mFooterLiveSceneViewInfo != null && mFooterLiveSceneViewInfo.sceneActor.scrim_type != SceneActor.SCRIM_OFF) {
                final int action = ev.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (isTouchInScrimZone((int) ev.getX(), (int) ev.getY())) {
                        if (mFooterLiveSceneViewInfo.sceneActor.scrim_type == SceneActor.SCRIM_ON && mSceneController != null) {
                            mSceneController.onCloseScene(false);
                        }
                        return true;
                    }
                }
                return false;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {

        boolean result;
        final int save = canvas.save();
        boolean drawScrim = false;

        if (mUseScrim) {
            /*
            if( mScrimFragment ) {
                if( mScrimFragmentView != null && child != mScrimFragmentView ) {
                    canvas.getClipBounds(mScrimZone);

                    mScrimZone.top = Math.max(mScrimZone.top, mScrimFragmentView.getBottom());
                    if (mFooterLiveSceneViewInfo != null && mFooterLiveSceneViewInfo.view != null && !mFooterLiveSceneViewInfo.sceneActor.scrim) {
                        mScrimZone.bottom = Math.min(mScrimZone.bottom, mFooterLiveSceneViewInfo.view.getTop());
                    }

                    canvas.clipRect(mScrimZone);
                    if (mScrimOpacity < 1) {
                        drawScrim = true;
                    }
                }
            } else {
            */
            boolean header = false;
            boolean footer = false;
            boolean scrim = true;
            final SceneViewInfo si = (SceneViewInfo)child.getTag(SCENE_INFO_TAG_ID);
            if(si != null) {
                header = si.sceneActor.type == SceneActor.HEADER_ACTOR;
                footer = si.sceneActor.type == SceneActor.FOOTER_ACTOR;
                scrim = si.sceneActor.scrim_type != SceneActor.SCRIM_OFF;
            }
            if( !header && mHeaderLiveSceneViewInfo != null && child != mHeaderView) {
                canvas.getClipBounds(mScrimZone);
                if (mHeaderLiveSceneViewInfo.view != null) {
                    mScrimZone.top = Math.max(mScrimZone.top, mHeaderLiveSceneViewInfo.view.getBottom());
                } else {
                    mScrimZone.top = Math.max(mScrimZone.top, mHeaderView.getBottom());
                }
                if (!scrim && mFooterLiveSceneViewInfo != null && mFooterLiveSceneViewInfo.view != null) {
                    mScrimZone.bottom = Math.min(mScrimZone.bottom, mFooterLiveSceneViewInfo.view.getTop());
                }
                canvas.clipRect(mScrimZone);
                if (mScrimOpacity < 1) {
                    drawScrim = true;
                }
            }

            if( !footer && mFooterLiveSceneViewInfo != null && child != mFooterView) {
                canvas.getClipBounds(mScrimZone);
                if (mFooterLiveSceneViewInfo.view != null) {
                    mScrimZone.bottom = Math.max(mScrimZone.bottom, mFooterLiveSceneViewInfo.view.getTop());
                } else {
                    mScrimZone.bottom = Math.max(mScrimZone.bottom, mFooterView.getTop());
                }
                if (!scrim && mHeaderLiveSceneViewInfo != null && mHeaderLiveSceneViewInfo.view != null) {
                    mScrimZone.top = Math.min(mScrimZone.top, mHeaderLiveSceneViewInfo.view.getBottom());
                }
                canvas.clipRect(mScrimZone);
                if (mScrimOpacity < 1) {
                    drawScrim = true;
                }
            }
            //}
        }

        result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(save);

        if (drawScrim) {
            final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
            final int img = (int) (baseAlpha * mScrimOpacity);
            final int color = img << 24 | (mScrimColor & 0xffffff);
            mScrimPaint.setColor(color);
            canvas.drawRect(mScrimZone, mScrimPaint);
        }

        return result;
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);

        if ( mHeaderShadowDrawable != null && mHeaderView != null) {
            final int right = mHeaderView.getRight();
            final int top = mHeaderView.getBottom();
            final int bottom = mHeaderView.getBottom() + mHeaderShadowHeight;
            final int left = mHeaderView.getLeft();
            mHeaderShadowDrawable.setBounds(left, top, right, bottom);
            mHeaderShadowDrawable.draw(c);
        }

        if (mFooterShadowDrawable != null && mFooterView != null) {
            final int right = mFooterView.getRight();
            final int top = mFooterView.getTop() - mFooterShadowHeight;
            final int bottom = mFooterView.getTop();
            final int left = mFooterView.getLeft();
            mFooterShadowDrawable.setBounds(left, top, right, bottom);
            mFooterShadowDrawable.draw(c);
        }
    }

    public void addActor(SceneActor sceneActor) {
        if(sceneActor.type == SceneActor.HEADER_ACTOR) {
            if(mHeaderView != null) {
                SceneActorView sceneActorView = (SceneActorView) sceneActor;
                View view = sceneActorView.onCreateView(mInflater, mHeaderView, false);
                mHeaderActors.put(sceneActor.id, new SceneViewInfo(sceneActor.id, sceneActor, view));
                mSceneActors.put(sceneActor.id, sceneActor);
                if(view != null) {
                    view.setTag(SCENE_INFO_TAG_ID, sceneActor);
                    mHeaderView.addView(view);
                }
            }
        } else if(sceneActor.type == SceneActor.FOOTER_ACTOR) {
            if(mFooterView != null) {
                SceneActorView sceneActorView = (SceneActorView) sceneActor;
                View view = sceneActorView.onCreateView(mInflater, mFooterView, false);
                mFooterActors.put(sceneActor.id, new SceneViewInfo(sceneActor.id, sceneActor, view));
                mSceneActors.put(sceneActor.id, sceneActor);
                if(view != null) {
                    view.setTag(SCENE_INFO_TAG_ID, sceneActor);
                    mFooterView.addView(view);
                }
            }
        } else if(sceneActor.type == SceneActor.ACTION_ACTOR) {
            mSceneActors.put(sceneActor.id, sceneActor);
        } else if(sceneActor.type == SceneActor.FRAGMENT_ACTOR) {
            try {
                FragmentActor fragmentActor = (FragmentActor) sceneActor;
                ISceneFragment sceneFragment = (ISceneFragment) fragmentActor.clazz.newInstance();
                sceneFragment.setSceneController(sceneActor.controller);
                mContentScenes.put(sceneActor.id, new SceneFragmentInfo(sceneActor.id, sceneActor, sceneFragment));
                mSceneActors.put(sceneActor.id, sceneActor);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if(sceneActor.type == SceneActor.ACTIVITY_ACTOR) {
            mSceneActors.put(sceneActor.id, sceneActor);
        } else if(sceneActor.type == SceneActor.BACKGROUND_ACTOR) {
            try {
                BackgroundActor fragmentActor = (BackgroundActor) sceneActor;
                IRetainedSceneFragment sceneFragment = (IRetainedSceneFragment) fragmentActor.clazz.newInstance();
                sceneActor.controller.addBackgroundActor(sceneFragment, fragmentActor.tag);
                mSceneActors.put(sceneActor.id, sceneActor);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public boolean hasActor(int actorId) {
        return mSceneActors != null && mSceneActors.get(actorId) != null;
    }

    public void removeActor(int actorId) {
        if(mSceneActors != null) {
            SceneActor sceneActor = mSceneActors.get(actorId);
            if(sceneActor != null) {
                mSceneActors.remove(actorId);
                if (sceneActor.type == SceneActor.HEADER_ACTOR) {
                    SceneViewInfo si = mHeaderActors.get(actorId);
                    mHeaderActors.remove(actorId);
                    if (si != null) {
                        SceneActorView sceneActorView = (SceneActorView) sceneActor;
                        sceneActorView.onDestroyView(si.view);
                        mHeaderView.removeView(si.view);
                        si.view = null;
                    }
                } else if (sceneActor.type == SceneActor.FOOTER_ACTOR) {
                    SceneViewInfo si = mFooterActors.get(actorId);
                    mFooterActors.remove(actorId);
                    if (si != null) {
                        SceneActorView sceneActorView = (SceneActorView) sceneActor;
                        sceneActorView.onDestroyView(si.view);
                        mFooterView.removeView(si.view);
                        si.view = null;
                    }
                } else if (sceneActor.type == SceneActor.FRAGMENT_ACTOR) {
                    SceneFragmentInfo si = mContentScenes.get(actorId);
                    mContentScenes.remove(actorId);
                    if (si != null) {
                        si.fragment.onClose();
                        si.fragment = null;
                    }
                }
            }
        }
    }

    public void removeAllActors(SceneController controller) {
        for(int i = mSceneActors.size() - 1; i >= 0; --i) {
            SceneActor sceneActor = mSceneActors.get(mSceneActors.keyAt(i));
            if(sceneActor.controller == controller) {
                if(sceneActor.type == SceneActor.HEADER_ACTOR) {
                    SceneViewInfo si = mHeaderActors.get(sceneActor.id);
                    mHeaderActors.remove(sceneActor.id);
                    if(si != null) {
                        SceneActorView sceneActorView = (SceneActorView) sceneActor;
                        sceneActorView.onDestroyView(si.view);
                        mHeaderView.removeView(si.view);
                        si.view = null;
                    }
                } else if(sceneActor.type == SceneActor.FOOTER_ACTOR) {
                    SceneViewInfo si = mFooterActors.get(sceneActor.id);
                    mFooterActors.remove(sceneActor.id);
                    if (si != null) {
                        SceneActorView sceneActorView = (SceneActorView) sceneActor;
                        sceneActorView.onDestroyView(si.view);
                        mFooterView.removeView(si.view);
                        si.view = null;
                    }
                } else if(sceneActor.type == SceneActor.FRAGMENT_ACTOR) {
                    SceneFragmentInfo si = mContentScenes.get(sceneActor.id);
                    mContentScenes.remove(sceneActor.id);
                    if(si != null) {
                        si.fragment.onClose();
                        si.fragment = null;
                    }
                }
                mSceneActors.removeAt(i);
            }
        }
    }

    public void removeAllActors() {
        for(int i = mSceneActors.size() - 1; i >= 0; --i) {
            SceneActor sceneActor = mSceneActors.get(mSceneActors.keyAt(i));
            if(sceneActor.type == SceneActor.HEADER_ACTOR) {
                SceneViewInfo si = mHeaderActors.get(sceneActor.id);
                if(si != null) {
                    SceneActorView sceneActorView = (SceneActorView) sceneActor;
                    sceneActorView.onDestroyView(si.view);
                    mHeaderView.removeView(si.view);
                    si.view = null;
                }
            } else if(sceneActor.type == SceneActor.FOOTER_ACTOR) {
                SceneViewInfo si = mFooterActors.get(sceneActor.id);
                if(si != null) {
                    SceneActorView sceneActorView = (SceneActorView) sceneActor;
                    sceneActorView.onDestroyView(si.view);
                    mFooterView.removeView(si.view);
                    si.view = null;
                }
            }
        }
        if(mHeaderActors != null) {
            mHeaderActors.clear();
        }
        if(mFooterActors != null) {
            mFooterActors.clear();
        }

        mContentScenes.clear();
        mSceneActors.clear();
    }

    public void resetScene() {
        if(mHeaderLiveSceneViewInfo != null && mHeaderLiveSceneViewInfo.animator != null) {
            mHeaderLiveSceneViewInfo.animator.cancel();
        }
        mHeaderLiveSceneViewInfo = null;
        adjustLayoutParamsHeight(mHeaderView, 0);

        if(mFooterLiveSceneViewInfo != null && mFooterLiveSceneViewInfo.animator != null) {
            mFooterLiveSceneViewInfo.animator.cancel();
        }
        mFooterLiveSceneViewInfo = null;
        adjustLayoutParamsHeight(mFooterView, 0);
    }

    public SceneActor findActorById(int id) {
        return mSceneActors.get(id);
    }

    @SuppressWarnings("unused")
    public void onSaveSceneState(Bundle outState) {
        for(int i = 0; i < mSceneActors.size(); ++i) {
            SceneActor sceneActor = mSceneActors.get(mSceneActors.keyAt(i));
            if(sceneActor.type == SceneActor.FRAGMENT_ACTOR) {
                FragmentActor fragmentActor = (FragmentActor) sceneActor;
                if(fragmentActor.retainSceneState) {
                    fragmentActor.onSaveSceneState(outState);
                }
            }
        }
    }

    public boolean performAction(int sceneId, int actionId, Object data) {
        if(mSceneActors != null) {
            SceneActor sceneActor = mSceneActors.get(sceneId);
            if(sceneActor != null) {
                return sceneActor.onActorAction(actionId, data);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public void performExpand(int sceneId) {
        if(mSceneActors != null) {
            SceneActor sceneActor = mSceneActors.get(sceneId);
            if(sceneActor != null) {
                if (sceneActor.type == SceneActor.HEADER_ACTOR) {
                    if (mHeaderLiveSceneViewInfo != null) {
                        if(mHeaderLiveSceneViewInfo.id == sceneId) {
                            // already expanded
                            return;
                        } else if(!mHeaderLiveSceneViewInfo.sceneActor.doAllowOverlay(sceneId)) {
                            if(mHeaderLiveSceneViewInfo.changing) {
                                synchronized (mPendingActionsLock) {
                                    mPendingActions.add(new SceneAction(sceneId, SceneAction.EXPAND));
                                    return;
                                }
                            } else {
                                throw new IllegalStateException("Can not overlay header sceneActor: [ " + mHeaderLiveSceneViewInfo.sceneActor.getClass().getName()
                                        + " ] by header sceneActor: [ " + sceneActor.getClass().getName() + " ], close the previous header sceneActor.");
                            }
                        } else {
                            if(mHeaderLiveSceneViewInfo.sceneActor.overlayType() == SceneActor.OVERLAY_VIEW) {
                                sceneActor.setOverlayViewInfo(mHeaderLiveSceneViewInfo);
                            } else {
                                mHeaderLiveSceneViewInfo.expanded = false;
                            }
                        }
                    }
                    SceneViewInfo si = mHeaderActors.get(sceneId);
                    if(si == null) {
                        throw new IllegalStateException("Header SceneActor: [ " + sceneActor.getClass().getName() + " ], has no corresponding sceneActor info.");
                    }
                    if (si.expanded) {
                        Log.e(getClass().getName(), "Can not expand header sceneActor: [ " + sceneActor.getClass().getName() + " ], already expanded.");
                        return;
                    }
                    mHeaderLiveSceneViewInfo = performExpandAction(sceneId, mHeaderView, mHeaderActors);
                } else if(sceneActor.type == SceneActor.FOOTER_ACTOR) {
                    if (mFooterLiveSceneViewInfo != null && !mFooterLiveSceneViewInfo.sceneActor.doAllowOverlay(sceneId)) {
                        if(mFooterLiveSceneViewInfo.id == sceneId) {
                            // already expanded
                            return;
                        } else if (!mFooterLiveSceneViewInfo.sceneActor.doAllowOverlay(sceneId)) {
                            if(mFooterLiveSceneViewInfo.changing) {
                                synchronized (mPendingActionsLock) {
                                    mPendingActions.add(new SceneAction(sceneId, SceneAction.EXPAND));
                                    return;
                                }
                            } else {
                                throw new IllegalStateException("Can not overlay footer sceneActor: [ " + mFooterLiveSceneViewInfo.sceneActor.getClass().getName()
                                        + " ] by footer sceneActor: [ " + sceneActor.getClass().getName() + " ], close the previous footer sceneActor.");
                            }
                        } else {
                            if(mFooterLiveSceneViewInfo.sceneActor.overlayType() == SceneActor.OVERLAY_VIEW) {
                                sceneActor.setOverlayViewInfo(mFooterLiveSceneViewInfo);
                            } else {
                                mFooterLiveSceneViewInfo.expanded = false;
                            }
                        }
                    }
                    SceneViewInfo si = mFooterActors.get(sceneId);
                    if(si == null) {
                        throw new IllegalStateException("Footer SceneActor: [ " + sceneActor.getClass().getName() + " ], has no corresponding sceneActor info.");
                    }
                    if (si.expanded) {
                        Log.e(getClass().getName(), "Can not expand footer sceneActor: [ " + sceneActor.getClass().getName() + " ], already expanded.");
                        return;
                    }
                    mFooterLiveSceneViewInfo = performExpandAction(sceneId, mFooterView, mFooterActors);
                } else if(sceneActor.type == SceneActor.FRAGMENT_ACTOR) {
                    SceneFragmentInfo si = mContentScenes.get(sceneId);
                    if(si == null) {
                        throw new IllegalStateException("Fragment SceneActor: [ " + sceneActor.getClass().getName() + " ], has no corresponding sceneActor info.");
                    }
                    if (si.expanded) {
                        Log.e(getClass().getName(), "Can not expand an already expanded fragment sceneActor.");
                        return;
                    }
                    FragmentActor fragmentActor = (FragmentActor) sceneActor;
                    fragmentActor.onAttachingFragment((SceneFragment)si.fragment);
                    if(fragmentActor.isHomeFragment()) {
                        sceneActor.controller.setHomeFragment(si.fragment, fragmentActor.transition);
                    } else {
                        sceneActor.controller.replaceFragment(si.fragment, fragmentActor.getClass().getName(), fragmentActor.transition);
                    }
                    fragmentActor.onAttachedFragment();
                    si.expanded = true;
                }
                //else if(sceneActor.type == SceneActor.ACTIVITY_ACTOR) {
                //    ActivitySceneActor activitySceneActor = (ActivitySceneActor)sceneActor;
                //    Intent intent = new Intent(getContext(), activitySceneActor.clazz);
                //    activitySceneActor.onNewInstanceIntent(intent);
                //    getContext().startActivity(intent);
                //}
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void performCollapse(int sceneId) {
        if(mSceneActors != null) {
            SceneActor sceneActor = mSceneActors.get(sceneId);
            if(sceneActor != null) {
                if (sceneActor.type == SceneActor.HEADER_ACTOR) {
                    if (mHeaderLiveSceneViewInfo == null) {
                        Log.e(getClass().getName(), "Can not collapse a header sceneActor when there is no header sceneActor to begin with.");
                        return;
                    }
                    if (!mHeaderActors.get(sceneId).expanded) {
                        Log.e(getClass().getName(), "Can not collapse header sceneActor: [ " + sceneActor.getClass().getName() + " ], already collapsed.");
                        return;
                    }
                    performCollapseAction(mHeaderView, mHeaderLiveSceneViewInfo);
                } else if(sceneActor.type == SceneActor.FOOTER_ACTOR) {
                    if (mFooterLiveSceneViewInfo == null) {
                        Log.e(getClass().getName(), "Can not collapse a footer sceneActor when there is no footer sceneActor to begin with");
                        return;
                    }
                    if (!mFooterActors.get(sceneId).expanded) {
                        Log.e(getClass().getName(), "Can not collapse footer sceneActor: [ " + sceneActor.getClass().getName() + " ] already collapsed.");
                        return;
                    }
                    performCollapseAction(mFooterView, mFooterLiveSceneViewInfo);
                } else if(sceneActor.type == SceneActor.FRAGMENT_ACTOR) {
                    SceneFragmentInfo si = mContentScenes.get(sceneId);
                    if (!si.expanded) {
                        Log.e(getClass().getName(), "Can not collapse an already collapsed fragment sceneActor.");
                        return;
                    }
                    FragmentActor fragmentActor = (FragmentActor) sceneActor;
                    fragmentActor.onDetachingFragment((SceneFragment)si.fragment);
                    if(!sceneActor.controller.popFragment(si.fragment)) {
                        throw new IllegalStateException("Can not collapse fragment sceneActor [ " + sceneActor.getClass().getName() + " ], it is not the topmost fragment.");
                    }
                    fragmentActor.onDetachedFragment();
                    si.expanded = false;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void performSwap(int fromActorId, int toActorId) {
        if(mSceneActors != null) {
            SceneActor fromSceneActor = mSceneActors.get(fromActorId);
            SceneActor toSceneActor = mSceneActors.get(toActorId);
            if(fromSceneActor != null && toSceneActor != null) {
                if (fromSceneActor.type == SceneActor.HEADER_ACTOR ||
                        fromSceneActor.type == SceneActor.FOOTER_ACTOR ||
                        toSceneActor.type == SceneActor.HEADER_ACTOR ||
                        toSceneActor.type == SceneActor.FOOTER_ACTOR) {
                    performCollapse(fromActorId);
                    performExpand(toActorId);
                } else if(fromSceneActor.type == SceneActor.FRAGMENT_ACTOR &&
                        toSceneActor.type == SceneActor.FRAGMENT_ACTOR) {

                    SceneFragmentInfo fromSi = mContentScenes.get(fromActorId);
                    SceneFragmentInfo toSi = mContentScenes.get(toActorId);

                    if (!fromSi.expanded) {
                        Log.e(getClass().getName(), "Can not collapse an already collapsed fragment sceneActor.");
                        return;
                    } else if(toSi == null) {
                        throw new IllegalStateException("Fragment SceneActor: [ " + toSceneActor.getClass().getName() + " ], has no corresponding sceneActor info.");
                    } else if (toSi.expanded) {
                        Log.e(getClass().getName(), "Can not expand an already expanded fragment sceneActor.");
                        return;
                    }

                    FragmentActor fromFragmentActor = (FragmentActor) fromSceneActor;
                    fromFragmentActor.onDetachingFragment((SceneFragment)fromSi.fragment);

                    FragmentActor toFragmentActor = (FragmentActor) toSceneActor;
                    toFragmentActor.onAttachingFragment((SceneFragment)toSi.fragment);

                    if(toFragmentActor.isHomeFragment()) {
                        toSceneActor.controller.setHomeFragment(toSi.fragment, toFragmentActor.transition);
                    } else {
                        toSceneActor.controller.replaceFragment(
                                fromSi.fragment,
                                fromFragmentActor.getClass().getName(),
                                toSi.fragment,
                                toFragmentActor.getClass().getName(),
                                toFragmentActor.transition
                        );
                    }

                    fromFragmentActor.onDetachedFragment();
                    fromSi.expanded = false;
                    toFragmentActor.onAttachedFragment();
                    toSi.expanded = true;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void performCancel(int sceneId) {
        if(mSceneActors != null) {
            SceneActor sceneActor = mSceneActors.get(sceneId);
            if(sceneActor != null) {
                if (sceneActor.type == SceneActor.HEADER_ACTOR) {
                    SceneViewInfo vi = mHeaderActors.get(sceneId);
                    vi.expanded = false;
                    if(sceneActor.hasOverlayView()) {
                        mHeaderLiveSceneViewInfo = findRootSceneView(sceneActor.getOverlayViewInfo());
                        if(!mHeaderLiveSceneViewInfo.expanded) {
                            mHeaderLiveSceneViewInfo = performExpandAction(mHeaderLiveSceneViewInfo.id, mHeaderView, mHeaderActors);
                        } else if(mHeaderLiveSceneViewInfo.view != null && mHeaderLiveSceneViewInfo.view.getVisibility() != View.VISIBLE) {
                            mHeaderLiveSceneViewInfo.view.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mHeaderLiveSceneViewInfo = null;
                        if(vi.view != null) {
                            vi.view.setVisibility(View.GONE);
                            vi.view.setTop(vi.top);
                            vi.view.setBottom(vi.bottom);
                            vi.view.setLeft(vi.left);
                            vi.view.setRight(vi.right);
                        }
                        adjustLayoutParamsHeight(mHeaderView, 0);
                    }
                    mUseScrim = false;
                    mUpdateScrim = false;
                    mScrimOpacity = 0.0f;
                } else if(sceneActor.type == SceneActor.FOOTER_ACTOR) {
                    SceneViewInfo vi = mFooterActors.get(sceneId);
                    vi.expanded = false;
                    if(sceneActor.hasOverlayView()) {
                        mFooterLiveSceneViewInfo = findRootSceneView(sceneActor.getOverlayViewInfo());
                        if(!mFooterLiveSceneViewInfo.expanded) {
                            mFooterLiveSceneViewInfo = performExpandAction(mFooterLiveSceneViewInfo.id, mFooterView, mFooterActors);
                        } else if(mFooterLiveSceneViewInfo.view != null && mFooterLiveSceneViewInfo.view.getVisibility() != View.VISIBLE) {
                            mFooterLiveSceneViewInfo.view.setVisibility(View.VISIBLE);
                        }
                    } else {
                        mFooterLiveSceneViewInfo = null;
                        if(vi.view != null) {
                            vi.view.setVisibility(View.GONE);
                            vi.view.setTop(vi.top);
                            vi.view.setBottom(vi.bottom);
                            vi.view.setLeft(vi.left);
                            vi.view.setRight(vi.right);
                        }
                        adjustLayoutParamsHeight(mFooterView, 0);
                    }
                } else if(sceneActor.type == SceneActor.FRAGMENT_ACTOR) {
                    SceneFragmentInfo si = mContentScenes.get(sceneId);
                    FragmentActor fragmentActor = (FragmentActor) sceneActor;
                    fragmentActor.onDetachingFragment((SceneFragment)si.fragment);
                    if(!sceneActor.controller.popFragment(si.fragment)) {
                        throw new IllegalStateException("Can not collapse fragment sceneActor [ " + sceneActor.getClass().getName() + " ], it is not the topmost fragment.");
                    }
                    fragmentActor.onDetachedFragment();
                    si.expanded = false;
                }
                invalidate();
            }
        }
    }

    public void performHeaderShow() {
        if(mHeaderLiveSceneViewInfo != null) {
            if(mHeaderLiveSceneViewInfo.sceneActor.scrim_type != SceneActor.SCRIM_OFF) {
                throw new IllegalStateException("Can not show scrim header: [ " + mHeaderLiveSceneViewInfo.sceneActor.getClass().getName() + " ]");
            }
            if(mHeaderLiveSceneViewInfo.view == null) {
                throw new IllegalStateException("Can not show un-viewable header: [ " + mHeaderLiveSceneViewInfo.sceneActor.getClass().getName() + " ]");
            }
            performShowAction(mHeaderView, mHeaderLiveSceneViewInfo);
        }
    }

    public void performHeaderHide() {
        if(mHeaderLiveSceneViewInfo != null) {
            if(mHeaderLiveSceneViewInfo.sceneActor.scrim_type != SceneActor.SCRIM_OFF) {
                throw new IllegalStateException("Can not hide scrim header: [ " + mHeaderLiveSceneViewInfo.sceneActor.getClass().getName() + " ]");
            }
            if(mHeaderLiveSceneViewInfo.view == null) {
                throw new IllegalStateException("Can not hide non-viewed header: [ " + mHeaderLiveSceneViewInfo.sceneActor.getClass().getName() + " ]");
            }
            performHideAction(mHeaderView, mHeaderLiveSceneViewInfo);
        }
    }

    public void performFooterShow() {
        if(mFooterLiveSceneViewInfo != null) {
            if(mFooterLiveSceneViewInfo.view == null) {
                throw new IllegalStateException("Can not show non-viewed footer: [ " + mFooterLiveSceneViewInfo.sceneActor.getClass().getName() + " ]");
            }
            performShowAction(mFooterView, mFooterLiveSceneViewInfo);
        }
    }

    public void performFooterHide() {
        if(mFooterLiveSceneViewInfo != null) {
            if(mFooterLiveSceneViewInfo.view == null) {
                throw new IllegalStateException("Can not hide non-viewed footer: [ " + mFooterLiveSceneViewInfo.sceneActor.getClass().getName() + " ]");
            }
            performHideAction(mFooterView, mFooterLiveSceneViewInfo);
        }
    }

    private SceneViewInfo ensureSceneInfo(int sceneId, SparseArray<SceneViewInfo> collection) {
        SceneViewInfo si = null;
        for(int i = 0; i < collection.size(); ++i) {
            SceneViewInfo info = collection.get(collection.keyAt(i));
            if(info.id == sceneId) {
                if(info.view != null) {
                    info.view.setVisibility(VISIBLE);
                }
                si = info;
            } else {
                if(info.view != null) {
                    info.view.setVisibility(GONE);
                }
            }
        }
        if(si != null &&
                si.sceneActor.hasOverlayView() &&
                si.sceneActor.overlainViewInfo != null &&
                si.sceneActor.overlainViewInfo.view != null) {
            si.sceneActor.overlainViewInfo.view.setVisibility(VISIBLE);
        }
        return si;
    }

    private void performPendingAction() {
        synchronized (mPendingActionsLock) {
            if(!mPendingActions.isEmpty()) {
                SceneAction action = mPendingActions.remove();
                switch(action.sceneActionType) {
                    case SceneAction.DATA: {
                        performAction(action.sceneActorId, action.sceneActionId, action.sceneActionData);
                    } break;
                    case SceneAction.EXPAND: {
                        performExpand(action.sceneActorId);
                    } break;
                    case SceneAction.COLLAPSE: {
                        performCollapse(action.sceneActorId);
                    } break;
                    case SceneAction.CANCEL: {
                        performCancel(action.sceneActorId);
                    } break;
                    case SceneAction.HIDE_HEADER: {
                        performHeaderHide();
                    } break;
                    case SceneAction.SHOW_HEADER: {
                        performHeaderShow();
                    } break;
                    case SceneAction.HIDE_FOOTER: {
                        performFooterHide();
                    } break;
                    case SceneAction.SHOW_FOOTER: {
                        performFooterShow();
                    } break;
                }
            }
        }
    }

    private SceneViewInfo performExpandAction(int sceneId, final View view, SparseArray<SceneViewInfo> collection) {
        if(view != null) {
            final SceneViewInfo si = ensureSceneInfo(sceneId, collection);
            if(si != null) {

                final SceneActor sceneActor = si.sceneActor;
                dispatchOnActorOpening(sceneActor);
                view.requestLayout();

                if(sceneActor.type == SceneActor.HEADER_ACTOR && sceneActor.scrim_type != SceneActor.SCRIM_OFF) {
                    mUseScrim = true;
                    mUpdateScrim = mScrimOpacity == 0.0f;
                } else {
                    mUseScrim = false;
                    mScrimOpacity = 0.0f;
                }

                if(si.view != null) {
                    view.setVisibility(View.VISIBLE);
                    final int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    try {
                        view.measure(widthSpec, heightSpec);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    if (mAllowAnimations) {
                        si.animator = slideViewAnimator(view, 0, view.getMeasuredHeight());
                        si.animator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                si.changing = true;
                                si.left = view.getLeft();
                                si.right = view.getRight();
                                si.top = view.getTop();
                                si.bottom = view.getBottom();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                si.changing = false;
                                si.animator = null;
                                si.expanded = true;
                                dispatchOnActorOpened(sceneActor);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                si.changing = false;
                                si.animator = null;
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
                        si.animator.start();
                    } else {
                        si.left = view.getLeft();
                        si.right = view.getRight();
                        si.top = view.getTop();
                        si.bottom = view.getBottom();
                        adjustLayoutParamsHeight(view, view.getMeasuredHeight());
                        mScrimOpacity = mMaxScrimOpacity;
                        si.expanded = true;
                        dispatchOnActorOpened(sceneActor);
                        invalidate();
                    }
                } else {
                    mScrimOpacity = mMaxScrimOpacity;
                    si.expanded = true;
                    dispatchOnActorOpened(sceneActor);
                    invalidate();
                }
                return si;
            }
        }
        return null;
    }

    private SceneViewInfo findRootSceneView(SceneViewInfo vi) {
        if(vi != null) {
            if (vi.sceneActor.hasOverlayView()) {
                vi.expanded = false;
                return findRootSceneView(vi.sceneActor.getOverlayViewInfo());
            }
        }
        return vi;
    }

    private void performCollapseAction(final View view, final SceneViewInfo si) {
        if(view != null && si != null) {
            final SceneActor sceneActor = si.sceneActor;
            dispatchOnActorClosing(sceneActor);

            if(si.view != null) {
                if(mAllowAnimations) {
                    int finalHeight = view.getHeight();
                    si.animator = slideViewAnimator(view, finalHeight, 0);
                    si.animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            si.changing = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            si.animator = null;
                            adjustLayoutParamsHeight(view, 0);
                            view.setVisibility(View.GONE);
                            si.changing = false;
                            si.expanded = false;
                            view.setTop(si.top);
                            view.setBottom(si.bottom);
                            view.setLeft(si.left);
                            view.setRight(si.right);
                            mUseScrim = false;
                            mUpdateScrim = false;
                            mScrimOpacity = 0.0f;
                            if (si.sceneActor.type == SceneActor.HEADER_ACTOR) {
                                if (si.sceneActor.hasOverlayView()) {
                                    mHeaderLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                                    mHeaderLiveSceneViewInfo = performExpandAction(mHeaderLiveSceneViewInfo.id, mHeaderView, mHeaderActors);
                                } else {
                                    mHeaderLiveSceneViewInfo = null;
                                }
                            } else if (si.sceneActor.type == SceneActor.FOOTER_ACTOR) {
                                if (si.sceneActor.hasOverlayView()) {
                                    mFooterLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                                    mFooterLiveSceneViewInfo = performExpandAction(mFooterLiveSceneViewInfo.id, mFooterView, mFooterActors);
                                } else {
                                    mFooterLiveSceneViewInfo = null;
                                }
                            }
                            dispatchOnActorClosed(sceneActor);
                            SceneLayout.this.refreshDrawableState();
                            performPendingAction();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            si.animator = null;
                            adjustLayoutParamsHeight(view, 0);
                            view.setVisibility(View.GONE);
                            si.changing = false;
                            si.expanded = false;
                            view.setTop(si.top);
                            view.setBottom(si.bottom);
                            view.setLeft(si.left);
                            view.setRight(si.right);
                            mUseScrim = false;
                            mUpdateScrim = false;
                            mScrimOpacity = 0.0f;
                            if (si.sceneActor.type == SceneActor.HEADER_ACTOR) {
                                if (si.sceneActor.hasOverlayView()) {
                                    mHeaderLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                                    mHeaderLiveSceneViewInfo = performExpandAction(mHeaderLiveSceneViewInfo.id, mHeaderView, mHeaderActors);
                                } else {
                                    mHeaderLiveSceneViewInfo = null;
                                }
                            } else if (si.sceneActor.type == SceneActor.FOOTER_ACTOR) {
                                if (si.sceneActor.hasOverlayView()) {
                                    mFooterLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                                    mFooterLiveSceneViewInfo = performExpandAction(mFooterLiveSceneViewInfo.id, mFooterView, mFooterActors);
                                } else {
                                    mFooterLiveSceneViewInfo = null;
                                }
                            }
                            dispatchOnActorClosed(sceneActor);
                            SceneLayout.this.refreshDrawableState();
                            performPendingAction();
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    si.animator.start();
                } else {
                    adjustLayoutParamsHeight(view, 0);
                    view.setVisibility(View.GONE);
                    si.expanded = false;
                    view.setTop(si.top);
                    view.setBottom(si.bottom);
                    view.setLeft(si.left);
                    view.setRight(si.right);
                    mUseScrim = false;
                    mUpdateScrim = false;
                    mScrimOpacity = 0.0f;
                    if (si.sceneActor.type == SceneActor.HEADER_ACTOR) {
                        if (si.sceneActor.hasOverlayView()) {
                            mHeaderLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                            mHeaderLiveSceneViewInfo = performExpandAction(mHeaderLiveSceneViewInfo.id, mHeaderView, mHeaderActors);
                        } else {
                            mHeaderLiveSceneViewInfo = null;
                        }
                    } else if (si.sceneActor.type == SceneActor.FOOTER_ACTOR) {
                        if (si.sceneActor.hasOverlayView()) {
                            mFooterLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                            mFooterLiveSceneViewInfo = performExpandAction(mFooterLiveSceneViewInfo.id, mFooterView, mFooterActors);
                        } else {
                            mFooterLiveSceneViewInfo = null;
                        }
                    }
                    dispatchOnActorClosed(sceneActor);
                    SceneLayout.this.refreshDrawableState();
                    performPendingAction();
                }
            } else {
                if(mUseScrim) {
                    mUseScrim = false;
                    mUpdateScrim = false;
                    mScrimOpacity = 0.0f;
                    si.expanded = false;
                    dispatchOnActorClosed(sceneActor);
                    if (si.sceneActor.type == SceneActor.HEADER_ACTOR) {
                        if (si.sceneActor.hasOverlayView()) {
                            mHeaderLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                            mHeaderLiveSceneViewInfo = performExpandAction(mHeaderLiveSceneViewInfo.id, mHeaderView, mHeaderActors);
                        } else {
                            mHeaderLiveSceneViewInfo = null;
                        }
                    } else if (si.sceneActor.type == SceneActor.FOOTER_ACTOR) {
                        if (si.sceneActor.hasOverlayView()) {
                            mFooterLiveSceneViewInfo = findRootSceneView(si.sceneActor.getOverlayViewInfo());
                            mFooterLiveSceneViewInfo = performExpandAction(mFooterLiveSceneViewInfo.id, mFooterView, mFooterActors);
                        } else {
                            mFooterLiveSceneViewInfo = null;
                        }
                    }
                    invalidate();
                    performPendingAction();
                }
            }
        }
    }

    private void performShowAction(final View view, final SceneViewInfo si) {
        if(view != null) {
            if(si != null) {

                final SceneActor sceneActor = si.sceneActor;
                dispatchOnActorShowing(sceneActor);
                view.requestLayout();

                if(si.view != null) {
                    view.setVisibility(View.VISIBLE);
                    final int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                    final int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

                    try {
                        view.measure(widthSpec, heightSpec);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }

                    if(mAllowAnimations) {
                        si.animator = slideViewAnimator(view, 0, view.getMeasuredHeight());
                        si.animator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                si.changing = true;
                                si.left = view.getLeft();
                                si.right = view.getRight();
                                si.top = view.getTop();
                                si.bottom = view.getBottom();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                si.animator = null;
                                si.changing = false;
                                si.hidden = false;
                                dispatchOnActorVisible(sceneActor);
                                invalidate();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                si.changing = false;
                                si.animator = null;
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
                        si.animator.start();
                    } else {
                        si.left = view.getLeft();
                        si.right = view.getRight();
                        si.top = view.getTop();
                        si.bottom = view.getBottom();
                        adjustLayoutParamsHeight(view, view.getMeasuredHeight());
                        si.hidden = false;
                        dispatchOnActorVisible(sceneActor);
                        invalidate();
                    }
                } else {
                    si.hidden = false;
                    dispatchOnActorVisible(sceneActor);
                    invalidate();
                }
            }
        }
    }

    private void performHideAction(final View view, final SceneViewInfo si) {
        if(view != null && si != null) {
            if(si.view != null) {
                final SceneActor sceneActor = si.sceneActor;
                dispatchOnActorHiding(sceneActor);
                if(mAllowAnimations) {
                    int finalHeight = view.getHeight();
                    si.animator = slideViewAnimator(view, finalHeight, 0);
                    si.animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            si.changing = true;
                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            si.animator = null;
                            view.setVisibility(View.GONE);
                            si.changing = false;
                            si.hidden = true;
                            view.setTop(si.top);
                            view.setBottom(si.bottom);
                            view.setLeft(si.left);
                            view.setRight(si.right);
                            dispatchOnActorGone(sceneActor);
                            SceneLayout.this.refreshDrawableState();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            si.animator = null;
                            si.changing = false;
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    si.animator.start();
                } else {
                    adjustLayoutParamsHeight(view, 0);
                    view.setVisibility(View.GONE);
                    si.hidden = true;
                    view.setTop(si.top);
                    view.setBottom(si.bottom);
                    view.setLeft(si.left);
                    view.setRight(si.right);
                    dispatchOnActorGone(sceneActor);
                    SceneLayout.this.refreshDrawableState();
                }
            }
        }
    }

    private ValueAnimator slideViewAnimator(final View view, int start, int end) {
        final int min = Math.min(start, end);
        final int max = Math.max(start, end);
        if(view != null) {
            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                private float map(int n, int a1, int a2,  float b2) {
                    return (((n-a1)*(b2))/(a2-a1));
                }

                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int value = (Integer) valueAnimator.getAnimatedValue();
                    adjustLayoutParamsHeight(view, value);
                    if(mUseScrim && mUpdateScrim) {
                        mScrimOpacity = map(value, min, max, mMaxScrimOpacity);
                    }
                }
            });
            return animator;
        }
        return null;
    }

    private void adjustLayoutParamsHeight(View view, int height) {
        Object objectParams = view.getLayoutParams();
        if(objectParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)objectParams;
            layoutParams.height = height;
            view.requestLayout();
        } else if(objectParams != null) {
            ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams)objectParams;
            layoutParams.height = height;
            view.requestLayout();
        }
    }

    //private ValueAnimator slideFragmentAnimator(final View view, int start, int end, final boolean enter) {
    //    final int min = Math.min(start, end);
    //    final int max = Math.max(start, end);
    //    if(view != null) {
    //        if(enter) {
    //            mUseScrim = true;
    //            mScrimFragment = true;
    //            mScrimFragmentView = view;
    //            mUpdateScrim = mScrimOpacity == 0.0f;
    //        }
    //        ValueAnimator animator = ValueAnimator.ofInt(start, end);
    //        animator.addListener(new Animator.AnimatorListener() {
    //            @Override
    //            public void onAnimationStart(Animator animation) {
    //            }
    //
    //            @Override
    //            public void onAnimationEnd(Animator animation) {
    //                if(!enter) {
    //                    mUseScrim = false;
    //                    mUpdateScrim = false;
    //                    mScrimFragment = false;
    //                    mScrimOpacity = 0.0f;
    //                    mScrimFragmentView = null;
    //                }
    //            }
    //
    //            @Override
    //            public void onAnimationCancel(Animator animation) {
    //                if(!enter) {
    //                    mUseScrim = false;
    //                    mUpdateScrim = false;
    //                    mScrimFragment = false;
    //                    mScrimOpacity = 0.0f;
    //                    mScrimFragmentView = null;
    //                }
    //            }
    //
    //            @Override
    //            public void onAnimationRepeat(Animator animation) {
    //
    //            }
    //        });
    //        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    //
    //            private float map(int n, int a1, int a2, float b1, float b2) {
    //                return b1 + (((n-a1)*(b2-b1))/(a2-a1));
    //            }
    //
    //            @Override
    //            public void onAnimationUpdate(ValueAnimator valueAnimator) {
    //                int value = (Integer) valueAnimator.getAnimatedValue();
    //                mScrimOpacity = map(value, min, max, 0.0f, mMaxScrimOpacity);
    //            }
    //        });
    //        return animator;
    //    }
    //    return null;
    //}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if( key.contentEquals(PREF_ALLOW_THEME_ANIMATIONS) ) {
            mAllowAnimations = sharedPreferences.getBoolean(PREF_ALLOW_THEME_ANIMATIONS, true);
        }
    }

}
