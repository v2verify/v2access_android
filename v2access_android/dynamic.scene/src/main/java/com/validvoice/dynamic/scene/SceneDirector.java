package com.validvoice.dynamic.scene;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

import com.validvoice.dynamic.db.ContractController;

import java.util.HashMap;

public class SceneDirector {

    private static final String TAG = "SceneDirector";

    private SceneLayout mSceneLayout;
    private ContractController mContractController;
    private SceneController mSceneController;
    private SparseArray<ISceneControllerFactory> mSceneControllers;
    private HashMap<String, Object> mSceneData;
    private int mHomeSceneId = -1;

    public SceneDirector(@NonNull SceneLayout sceneLayout) {
        mSceneLayout = sceneLayout;
        mContractController = ContractController.getContractController(sceneLayout.getContext());
        mSceneController = null;
        mSceneControllers = new SparseArray<>();
        mSceneData = new HashMap<>();
        ((SceneApplication)mSceneLayout
                .getContext()
                .getApplicationContext())
                .SetSceneManager(this);
    }

    public final boolean registerSceneControllerFactory(int scene_id, @NonNull ISceneControllerFactory factory) {
        if(mSceneControllers.indexOfKey(scene_id) >= 0) return false;
        Log.i(TAG, "registerSceneControllerFactory(id: " + scene_id + ", factory: " + factory.getClass().getName() + ")");
        mSceneControllers.put(scene_id, factory);
        return true;
    }

    public final void unregisterSceneControllerFactory(int scene_id) {
        mSceneControllers.remove(scene_id);
    }

    public final SceneController currentController() {
        return mSceneController;
    }

    public final ContractController getContractController() {
        return mContractController;
    }

    public final void setData(String name, Object data) {
        mSceneData.put(name, data);
    }

    public final Object getData(String name) {
        return mSceneData.get(name);
    }

    public final Object getData(String name, boolean clear) {
        Object data = mSceneData.get(name);
        if(clear) {
            removeData(name);
        }
        return data;
    }

    public final boolean hasData(String name) {
        return mSceneData.containsKey(name);
    }

    public final void removeData(String name) {
        mSceneData.remove(name);
    }

    public final int getHomeSceneId() {
        return mHomeSceneId;
    }

    public final boolean setHomeScene(int home_id) {
        if(mHomeSceneId == -1) {
            mHomeSceneId = home_id;
            return changeScene(home_id);
        }
        return false;
    }

    public final boolean changeHomeScene(int prev_home_id, int new_home_id) {
        if(mHomeSceneId == prev_home_id) {
            mHomeSceneId = new_home_id;
            return changeScene(new_home_id);
        }
        return false;
    }

    public final boolean isHomeSceneActive() {
        return currentController() != null && currentController().getId() == mHomeSceneId;
    }

    public final boolean setHomeSceneActive() {
        return mHomeSceneId != -1 && changeScene(mHomeSceneId);
    }

    public final boolean changeScene(int scene_id) {
        if( mSceneController == null || mSceneController.getId() != scene_id ) {
            if (mSceneController != null) {
                mSceneController.close();
                mSceneController = null;
            }
            ISceneControllerFactory factory = mSceneControllers.get(scene_id);
            if (factory == null) return false;
            mSceneController = factory.CreateSceneController(this, mSceneLayout);
            if (mSceneController == null) return false;
            mSceneController.open();
        } else if(mSceneController.getActiveActorId() != mSceneController.getHomeActorId()) {
            mSceneController.dispatchSwap(mSceneController.getActiveActorId(), mSceneController.getHomeActorId());
        } else {
            mSceneController.refresh();
        }
        return true;
    }

    public final boolean onBackPressed() {
        return mSceneController != null && mSceneController.onBackPressed();
    }

    public final void onResume() {
        if(mSceneController != null) {
            mSceneController.onResume();
        }
    }

    public final void onPause() {
        if(mSceneController != null) {
            mSceneController.onPause();
        }
    }

    public final boolean onCreateOptionsMenu(Menu menu) {
        return mSceneController != null && mSceneController.onCreateOptionsMenu(menu);
    }

    public final boolean onOptionsItemSelected(MenuItem item) {
        return mSceneController != null && mSceneController.onOptionsItemSelected(item);
    }

    public final boolean onPrepareOptionsMenu(Menu menu) {
        return mSceneController != null && mSceneController.onPrepareOptionsMenu(menu);
    }

    public final void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(mSceneController != null) {
            mSceneController.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

}
