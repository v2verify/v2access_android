package com.validvoice.dynamic.scene;

import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SceneActivity extends AppCompatActivity {

    private static final String TAG = "SceneActivity";

    ///
    ///
    ///
    private SceneLayout mSceneLayout;
    private SceneDirector mSceneDirector;
    private int mRequestCode;
    private IScenePermissionListener mPermissionListener;

    ///
    ///
    ///

    protected final void setSceneLayout(@IdRes int sceneLayoutId, GradientDrawable background) {
        mSceneLayout = findViewById(sceneLayoutId);
        mSceneLayout.setBackground(background);
        mSceneDirector = new SceneDirector(mSceneLayout);
    }

    public final SceneLayout getSceneLayout() {
        return mSceneLayout;
    }

    public final SceneDirector getSceneDirector() {
        return mSceneDirector;
    }

    public final void setHomeScene(int scene_id) {
        if(mSceneDirector != null) {
            mSceneDirector.setHomeScene(scene_id);
        }
    }

    public final boolean isHomeSceneActive() {
        return mSceneDirector != null && mSceneDirector.isHomeSceneActive();
    }

    public final void setHomeSceneActive() {
        if(mSceneDirector != null) {
            mSceneDirector.setHomeSceneActive();
        }
    }

    public final void changeScene(int scene_id) {
        if(mSceneDirector != null) {
            mSceneDirector.changeScene(scene_id);
        }
    }

    public final boolean addSceneActionActor(@NonNull SceneLayout.SceneActionActor sceneActionActor) {
        if(mSceneLayout != null) {
            mSceneLayout.addActor(sceneActionActor);
            return true;
        }
        return false;
    }

    public final boolean removeSceneActionActor(@NonNull SceneLayout.SceneActionActor sceneActionActor) {
        if(mSceneLayout != null) {
            mSceneLayout.removeActor(sceneActionActor.id);
            return true;
        }
        return false;
    }

    public final boolean registerSceneControllerFactory(int scene_id, @NonNull ISceneControllerFactory factory) {
        return mSceneDirector != null && mSceneDirector.registerSceneControllerFactory(scene_id, factory);
    }

    public final void unregisterSceneControllerFactory(int scene_id) {
        if(mSceneDirector != null) {
            mSceneDirector.unregisterSceneControllerFactory(scene_id);
        }
    }

    public final SceneController currentSceneController() {
        return mSceneDirector != null ? mSceneDirector.currentController() : null;
    }

    public boolean sceneBackPressed() {
        return mSceneDirector != null && mSceneDirector.onBackPressed();
    }

    @NonNull
    public Snackbar makeSnackbar(@NonNull CharSequence text, int duration) {
        return Snackbar.make(mSceneLayout, text, duration);
    }

    @NonNull
    public Snackbar makeSnackbar(@StringRes int resId, int duration) {
        return Snackbar.make(mSceneLayout, resId, duration);
    }

    public void requestPermission(int requestCode, @NonNull String permission,
                                  @NonNull IScenePermissionListener listener) {
        int rc = ActivityCompat.checkSelfPermission(this, permission);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            listener.onPermissionGranted(permission);
        } else {
            mRequestCode = requestCode;
            mPermissionListener = listener;
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           final @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(permissions.length == 1 && grantResults.length == 1) {
            if (requestCode == mRequestCode) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permission granted: Result code: " + grantResults[0]);
                    mPermissionListener.onPermissionGranted(permissions[0]);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    Log.e(TAG, "Permission not granted: Show Rationale");
                    mPermissionListener.onPermissionRationale(permissions[0]);
                } else {
                    Log.e(TAG, "Permission not granted: Result code: " + grantResults[0]);
                    mPermissionListener.onPermissionDenied(permissions[0]);
                }
                mRequestCode = -1;
                mPermissionListener = null;
                return;
            } else if (mSceneDirector != null) {
                mSceneDirector.onRequestPermissionsResult(requestCode, permissions, grantResults);
                return;
            }
        }
        Log.d(TAG, "Got unexpected permission result: " + requestCode);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    ///
    ///
    ///

    @Override
    public void onResume() {
        super.onResume();
        mSceneDirector.onResume();
    }

    @Override
    public void onPause() {
        mSceneDirector.onPause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return (mSceneDirector != null && mSceneDirector.onCreateOptionsMenu(menu)) || super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (mSceneDirector != null && mSceneDirector.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return (mSceneDirector != null && mSceneDirector.onPrepareOptionsMenu(menu)) || super.onPrepareOptionsMenu(menu);
    }

}
