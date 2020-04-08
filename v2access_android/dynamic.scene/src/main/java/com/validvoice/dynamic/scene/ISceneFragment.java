package com.validvoice.dynamic.scene;

import android.support.v4.app.Fragment;

import com.validvoice.dynamic.db.ContractController;

public interface ISceneFragment {

    int getSceneId();
    void onRefresh();
    void onClose();
    boolean onBackPressed();
    void setSceneController(SceneController sceneController);
    SceneController getSceneController();
    SceneDirector getSceneDirector();
    ContractController getContractController();
    Fragment getFragment();

}