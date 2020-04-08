package com.validvoice.dynamic.scene;



import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.validvoice.dynamic.db.ContractController;

public class SceneDialogFragment extends DialogFragment implements ISceneFragment {

    private SceneController mSceneController;

    @Override
    public int getSceneId() {
        return 0;
    }

    @Override
    public void onRefresh() {
        throw new IllegalStateException();
    }

    @Override
    public void onClose() {
        throw new IllegalStateException();
    }

    @Override
    public boolean onBackPressed() {
        throw new IllegalStateException();
    }

    @Override
    public void setSceneController(SceneController sceneController) {
        mSceneController = sceneController;
    }

    @Override
    public SceneController getSceneController() {
        return mSceneController;
    }

    @Override
    public SceneDirector getSceneDirector() {
        return mSceneController.getSceneDirector();
    }

    @Override
    public ContractController getContractController() {
        return mSceneController.getContractController();
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    public boolean dispatchAction(int actorId, int actionId, Object data) {
        return mSceneController.dispatchAction(actorId, actionId, data);
    }

    public boolean dispatchBypassAction(int actorId, int actionId, Object data) {
        return mSceneController.dispatchBypassAction(actorId, actionId, data);
    }
}
