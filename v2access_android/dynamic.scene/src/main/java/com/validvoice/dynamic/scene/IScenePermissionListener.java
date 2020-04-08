package com.validvoice.dynamic.scene;

import android.support.annotation.NonNull;

public interface IScenePermissionListener {

    void onPermissionGranted(@NonNull String permission);

    void onPermissionRationale(@NonNull String permission);

    void onPermissionDenied(@NonNull String permission);

}
