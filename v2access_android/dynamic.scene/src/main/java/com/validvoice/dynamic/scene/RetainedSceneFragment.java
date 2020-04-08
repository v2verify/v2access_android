package com.validvoice.dynamic.scene;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.validvoice.dynamic.db.ContractController;

public class RetainedSceneFragment extends Fragment implements IRetainedSceneFragment {

    private boolean mIsClosed = false;
    private SceneController mSceneController;

    public RetainedSceneFragment() {
        // Required empty public constructor
    }

    @Override
    public final Fragment getFragment() {
        return this;
    }

    @Override
    public void onClose() {
        mIsClosed = true;
    }

    final public boolean isClosed() {
        return mIsClosed;
    }

    @Override
    public SceneController getSceneController() {
        return mSceneController;
    }

    @Override
    public final SceneDirector getSceneDirector() {
        if(mSceneController == null) {
            throw new NullPointerException("SceneController is null. Cannot access SceneDirector.");
        }
        return mSceneController.getSceneDirector();
    }

    public ContractController getContractController() {
        return getSceneDirector().getContractController();
    }

    public boolean dispatchSceneAction(int sceneId, int actionId, Object data) {
        return this.mSceneController.dispatchAction(sceneId, actionId, data);
    }

    public boolean dispatchBypassAction(int sceneId, int actionId, Object data) {
        return this.mSceneController.dispatchBypassAction(sceneId, actionId, data);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mSceneController = ((SceneApplication)context.getApplicationContext())
                .GetSceneDirector()
                .currentController();
    }

    @Override
    public void onDetach() {
        this.mSceneController = null;
        super.onDetach();
    }



}
