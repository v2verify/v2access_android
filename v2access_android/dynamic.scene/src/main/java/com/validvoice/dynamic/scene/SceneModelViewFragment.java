package com.validvoice.dynamic.scene;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class SceneModelViewFragment extends SceneFragment {

    private static final String TAG = "SceneModelViewFragment";

    private @IdRes int mInitialSceneModelViewId = R.id.model_view_none;
    private @IdRes int mCurrentSceneModelViewId = R.id.model_view_none;
    private SparseArray<ISceneModelView> mSceneModelViews = new SparseArray<>();

    private Handler mHandler;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        mHandler = new Handler();

        try {
            View view = onCreateModelView(inflater, container, savedInstanceState);
            initializeModelView();
            return view;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public abstract View onCreateModelView(@NonNull LayoutInflater inflater, ViewGroup container,
                                           Bundle savedInstanceState) throws Exception;

    public Handler getModelViewHandler() {
        return mHandler;
    }

    public final boolean addInitialModelView(@IdRes int view_id, @NonNull ISceneModelView view) {
        if(mInitialSceneModelViewId == R.id.model_view_none) {
            if(addModelView(view_id, view)) {
                mInitialSceneModelViewId = view_id;
                return true;
            }
        }
        return false;
    }

    public final boolean addModelView(@IdRes int view_id, @NonNull ISceneModelView view) {
        if(mSceneModelViews.get(view_id) != null) {
            return false;
        }
        mSceneModelViews.put(view_id, view);
        return true;
    }

    public final void removeModelView(@IdRes int view_id) {
        mSceneModelViews.remove(view_id);
    }

    public final @IdRes int getModelViewId() {
        return mCurrentSceneModelViewId;
    }

    public final void changeModelView(@IdRes int toViewId, @NonNull String fromFunction) {

        try {
            if (mCurrentSceneModelViewId == -1) {
                throw new Exception("SceneModelViewFragment not initialized");
            }

            @IdRes int fromViewId = mCurrentSceneModelViewId;
            @IdRes int overrideViewId = onOverrideModelView(fromViewId, toViewId);
            if(overrideViewId != toViewId) {
                String toViewName = getResources().getResourceName(toViewId);
                String overrideViewName = getResources().getResourceName(overrideViewId);
                Log.d(TAG, "changeSceneModelView(" + fromFunction + "): Override: " + toViewName + " - to - " + overrideViewName);
                toViewId = overrideViewId;
            }

            ISceneModelView fromView = mSceneModelViews.get(fromViewId);
            if (fromView == null) {
                throw new Exception("Unable to find 'from' view: " + getResources().getResourceName(fromViewId));
            }

            ISceneModelView toView = mSceneModelViews.get(toViewId);
            if (toView == null) {
                throw new Exception("Unable to find 'to' view: " + getResources().getResourceName(toViewId));
            }

            dispatchOnChangingModelView(fromViewId, fromView, toViewId, toView, fromFunction);
        } catch(Exception ex) {
            ex.printStackTrace();

            // Unable to call dispatchOnChangingModelView due to exception
            // Reinitialize the Model View Controller

            try {
                initializeModelView();
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    public @IdRes int onOverrideModelView(@IdRes int fromViewId, @IdRes int toViewId) {
        return toViewId;
    }

    public void onChangingModelView(@IdRes int fromViewId, @IdRes int toViewId) {

    }

    public void onChangedModelView(@IdRes int fromViewId, @IdRes int toViewId) {

    }

    private void initializeModelView() throws Exception {
        if(mInitialSceneModelViewId == R.id.model_view_none) {
            throw new Exception("Initial Model View never defined");
        }
        ISceneModelView initialView = mSceneModelViews.get(mInitialSceneModelViewId);
        if(initialView == null) {
            throw new Exception("Unable to find 'initial' view: " + mInitialSceneModelViewId);
        }
        dispatchOnInitializeModelView(mInitialSceneModelViewId, initialView);
    }

    private void dispatchOnInitializeModelView(final @IdRes int initialViewId,
                                               final ISceneModelView initialView) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "initializeModelView: initializing " + getResources().getResourceName(initialViewId));
                dispatchOnInitializingModelView(initialViewId, initialView);
            }
        });
    }

    private void dispatchOnInitializingModelView(final @IdRes int initialViewId,
                                                 final ISceneModelView initialView) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                initialView.onOpeningModelView(-1);
                dispatchOnInitializedModelView(initialViewId, initialView);
            }
        });
    }

    private void dispatchOnInitializedModelView(final @IdRes int initialViewId,
                                                final ISceneModelView initialView) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                initialView.onOpenedModelView(-1);
                Log.d(TAG, "initializeModelView: initialized " + getResources().getResourceName(initialViewId));
                mCurrentSceneModelViewId = initialViewId;
            }
        });
    }

    private void dispatchOnChangingModelView(final @IdRes int fromViewId, final ISceneModelView fromView,
                                        final @IdRes int toViewId, final ISceneModelView toView,
                                        final String fromFunction) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String fromName = getResources().getResourceName(fromViewId);
                String toName = getResources().getResourceName(toViewId);
                Log.d(TAG, "changeSceneModelView(" + fromFunction + "): " + fromName + " - changing to - " + toName);
                onChangingModelView(fromViewId, toViewId);
                dispatchOnClosingModelView(fromViewId, fromView, toViewId, toView, fromFunction);
            }
        });
    }

    private void dispatchOnClosingModelView(final @IdRes int fromViewId, final ISceneModelView fromView,
                                        final @IdRes int toViewId, final ISceneModelView toView,
                                        final String fromFunction) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fromView.onClosingModelView(toViewId);
                dispatchOnClosedModelView(fromViewId, fromView, toViewId, toView, fromFunction);
            }
        });
    }

    private void dispatchOnClosedModelView(final @IdRes int fromViewId, final ISceneModelView fromView,
                                        final @IdRes int toViewId, final ISceneModelView toView,
                                        final String fromFunction) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                fromView.onClosedModelView(toViewId);
                dispatchOnOpeningModelView(fromViewId, toViewId, toView, fromFunction);
            }
        });
    }

    private void dispatchOnOpeningModelView(final @IdRes int fromViewId,
                                            final @IdRes int toViewId,
                                            final ISceneModelView toView,
                                            final String fromFunction) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                toView.onOpeningModelView(fromViewId);
                dispatchOnOpenedModelView(fromViewId, toViewId, toView, fromFunction);
            }
        });
    }

    private void dispatchOnOpenedModelView(final @IdRes int fromViewId,
                                           final @IdRes int toViewId,
                                           final ISceneModelView toView,
                                           final String fromFunction) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                toView.onOpenedModelView(fromViewId);
                dispatchOnChangedModelView(fromViewId, toViewId, fromFunction);
            }
        });
    }

    private void dispatchOnChangedModelView(final @IdRes int fromViewId,
                                            final @IdRes int toViewId,
                                            final String fromFunction) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onChangedModelView(fromViewId, toViewId);
                String fromName = getResources().getResourceName(fromViewId);
                String toName = getResources().getResourceName(toViewId);
                Log.d(TAG, "changeModelView(" + fromFunction + "): " + fromName + " - changed - " + toName);
            }
        });
    }
}

