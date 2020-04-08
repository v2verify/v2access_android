package com.validvoice.dynamic.scene;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.validvoice.dynamic.db.ContractController;

public class SceneFragment extends Fragment implements ISceneFragment {

    private static final String TAG = "SceneFragment";

    private boolean mIsClosed = false;
    private SceneController mSceneController;
    private int mSceneId;
    private int mRequestCode;
    private IScenePermissionListener mPermissionListener;

    public SceneFragment() {
        // Required empty public constructor
    }

    @Override
    public final Fragment getFragment() {
        return this;
    }

    @Override
    public final int getSceneId() {
        return mSceneId;
    }

    @Override
    public void onRefresh() {

    }

    public boolean onActorAction(int id, Object data) {
        return false;
    }

    @Override
    public void onClose() {
        mIsClosed = true;
    }

    public final boolean isClosed() {
        return mIsClosed;
    }

    @Override
    public boolean onBackPressed() {
        return false;
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
    public final SceneDirector getSceneDirector() {
        if(mSceneController == null) {
            throw new NullPointerException("SceneController is null. Cannot access SceneDirector.");
        }
        return mSceneController.getSceneDirector();
    }

    @Override
    public ContractController getContractController() {
        return getSceneDirector().getContractController();
    }

    @NonNull
    public final Snackbar makeSnackbar(@NonNull CharSequence text, int duration) {
        return getSceneController().getSceneActivity().makeSnackbar(text, duration);
    }

    @NonNull
    public final Snackbar makeSnackbar(@StringRes int resId, int duration) {
        return getSceneController().getSceneActivity().makeSnackbar(resId, duration);
    }

    public final void dispatchExpand(int actorId) {
        this.mSceneController.dispatchExpand(actorId);
    }

    public final void dispatchExpand(int expandingActorId, int becomingActorId) {
        this.mSceneController.dispatchExpand(expandingActorId, becomingActorId);
    }

    public final void dispatchExpand(int actorId, int actionId, Object data) {
        this.mSceneController.dispatchExpand(actorId, actionId, data);
    }

    public final void dispatchCollapse(int actorId) {
        this.mSceneController.dispatchCollapse(actorId);
    }

    public final void dispatchCollapse(int collapsingActorId, int becomingActorId) {
        this.mSceneController.dispatchCollapse(collapsingActorId, becomingActorId);
    }

    public final void dispatchCollapse(int actorId, int actionId, Object data) {
        this.mSceneController.dispatchCollapse(actorId, actionId, data);
    }

    public final void dispatchCancel(int actorId) {
        this.mSceneController.dispatchCancel(actorId);
    }

    public final boolean dispatchAction(int actorId, int actionId, Object data) {
        return this.mSceneController.dispatchAction(actorId, actionId, data);
    }

    public final boolean dispatchBypassAction(int sceneId, int actionId, Object data) {
        return this.mSceneController.dispatchBypassAction(sceneId, actionId, data);
    }

    public final void dispatchHideHeader() {
        this.mSceneController.dispatchHideHeader();
    }

    public final void dispatchShowHeader() {
        this.mSceneController.dispatchShowHeader();
    }

    public final void dispatchHideFooter() {
        this.mSceneController.dispatchHideFooter();
    }

    public final void dispatchShowFooter() {
        this.mSceneController.dispatchShowFooter();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSceneController != null) {
            mSceneController.getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mSceneController != null) {
            mSceneController.getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        getActivity().startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        getActivity().startActivityForResult(intent, requestCode);
    }

    public void requestPermission(int requestCode, @NonNull String permission,
                                  @NonNull IScenePermissionListener listener) {
        Activity activity = getActivity();
        if(activity == null) {
            throw new NullPointerException("Activity");
        }

        int rc = ActivityCompat.checkSelfPermission(activity, permission);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            listener.onPermissionGranted(permission);
        } else {
            mRequestCode = requestCode;
            mPermissionListener = listener;
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(permissions.length == 1 && grantResults.length == 1) {
            Activity activity = getActivity();
            if(activity == null) {
                throw new NullPointerException("Activity");
            }

            if (requestCode == mRequestCode) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permission granted: Result code: " + grantResults[0]);
                    mPermissionListener.onPermissionGranted(permissions[0]);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
                    Log.e(TAG, "Permission not granted: Show Rationale");
                    mPermissionListener.onPermissionRationale(permissions[0]);
                } else {
                    Log.e(TAG, "Permission not granted: Result code: " + grantResults[0]);
                    mPermissionListener.onPermissionDenied(permissions[0]);
                }
                mRequestCode = -1;
                mPermissionListener = null;
                return;
            } else if (getSceneController() != null) {
                getSceneController().onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
            }
        }
        Log.d(TAG, "Got unexpected permission result: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
