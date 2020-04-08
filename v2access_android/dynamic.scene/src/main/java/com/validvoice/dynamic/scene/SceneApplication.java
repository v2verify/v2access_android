package com.validvoice.dynamic.scene;

import android.app.Application;

public class SceneApplication extends Application {

    private SceneDirector mSceneDirector = null;

    public final void SetSceneManager(SceneDirector sceneManager) {
        if(mSceneDirector == null) {
            mSceneDirector = sceneManager;
        }
    }

    public final SceneDirector GetSceneDirector() {
        return mSceneDirector;
    }

}
