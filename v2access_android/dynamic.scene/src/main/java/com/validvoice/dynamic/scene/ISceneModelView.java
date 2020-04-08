package com.validvoice.dynamic.scene;

import android.support.annotation.IdRes;

public interface ISceneModelView {

    void onOpeningModelView(@IdRes int fromViewId);

    void onOpenedModelView(@IdRes int fromViewId);

    void onClosingModelView(@IdRes int toViewId);

    void onClosedModelView(@IdRes int toViewId);

}
