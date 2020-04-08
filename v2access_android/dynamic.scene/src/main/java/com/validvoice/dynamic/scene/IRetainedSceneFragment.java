package com.validvoice.dynamic.scene;

import android.support.v4.app.Fragment;

import com.validvoice.dynamic.db.ContractController;

public interface IRetainedSceneFragment {

    void onClose();
    SceneController getSceneController();
    ContractController getContractController();
    SceneDirector getSceneDirector();
    Fragment getFragment();

}
