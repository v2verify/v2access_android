package com.validvoice.voxidem.scenes.subs.capture;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.scene.IScenePermissionListener;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.barcode.BarcodeTracker;
import com.validvoice.voxidem.barcode.BarcodeTrackerFactory;
import com.validvoice.voxidem.camera.CameraSource;
import com.validvoice.voxidem.camera.CameraSourcePreview;
import com.validvoice.voxidem.cloud.QrIntent;
import com.validvoice.voxidem.cloud.QrUserAccount;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.scenes.subs.sve.EnrollFragment;
import com.validvoice.voxidem.scenes.subs.sve.VerifyFragment;
import com.validvoice.voxidem.scenes.subs.sve.VerifyIntent;

import java.io.IOException;

public class CaptureFragment extends SceneFragment implements IScenePermissionListener {

    private static final String TAG = CaptureFragment.class.getSimpleName();

    public static final String CAPTURE_MODE = "CAPTURE_MODE";

    public enum CaptureMode {
        Enroll,
        Verify,
        Either
    }

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    private CameraSource mCameraSource;

    private CameraSourcePreview mPreview;

    // UI References
    private Handler mUiHandler;

    private boolean mPauseDetecting;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_capture, container, false);

        mPreview = view.findViewById(R.id.preview);

        mUiHandler = new Handler();
        mPauseDetecting = false;

        return view;
    }

    /**
     * Creates and starts the camera.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {

        Activity activity = getActivity();
        if(activity == null) {
            throw new NullPointerException("Activity");
        }

        Context context = activity.getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(new QrTrackerCallback());
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = activity.registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(activity, R.string.scene_capture_low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.scene_capture_low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        CameraSource.Builder builder = new CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(metrics.widthPixels, metrics.heightPixels)
                .setRequestedFps(24.0f);

        // make sure that auto focus is an available option
        builder = builder
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        requestPermission(1, Manifest.permission.CAMERA, this);
    }

    // Restarts the camera
    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    // Stops the camera
    @Override
    public void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    @Override
    public void onPermissionGranted(@NonNull String permission) {
        if(permission.equals(Manifest.permission.CAMERA)) {
            boolean autoFocus = true;
            boolean useFlash = false;
            createCameraSource(autoFocus, useFlash);
            startCameraSource();
        }
    }

    @Override
    public void onPermissionRationale(@NonNull String permission) {
        Log.d(TAG, "onPermissionRationale");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            makeSnackbar(R.string.audio_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermission(100, Manifest.permission.RECORD_AUDIO,
                                    CaptureFragment.this);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onPermissionDenied(@NonNull String permission) {
        Log.d(TAG, "onPermissionDenied");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            makeSnackbar(R.string.scene_capture_no_camera_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Activity activity = getActivity();
                            if(activity != null) {
                                activity.finish();
                            }
                        }
                    })
                    .show();
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {

        Activity activity = getActivity();
        if(activity == null) {
            throw new NullPointerException("Activity");
        }

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                activity.getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private class QrTrackerCallback implements BarcodeTracker.BarcodeGraphicTrackerCallback {

        @Override
        public void onDetectedQrCode(final Barcode barcode) {
            Log.d(TAG, "onDetectedQrCode:::::::::::::::::::::::::: ");
            if(mPauseDetecting) return;
            if(barcode.displayValue.length() == 40 && barcode.displayValue.startsWith("V2W:")) {
                CaptureMode mode = CaptureMode.Either;
                if(getSceneDirector().hasData(CAPTURE_MODE)) {
                    mode = (CaptureMode) getSceneDirector().getData(CAPTURE_MODE);
                }
                if(mode == CaptureMode.Enroll) {
                    if(barcode.displayValue.contains("+")) {
                        prepareEnrollment(barcode.displayValue.substring(4));
                    } else {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getSceneController().getContext(), R.string.scene_capture_invalid_enroll_barcode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else if(mode == CaptureMode.Verify) {
                    if(barcode.displayValue.contains("-")) {
                        prepareVerification(barcode.displayValue.substring(4));
                    } else {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getSceneController().getContext(), R.string.scene_capture_invalid_verify_barcode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else if(mode == CaptureMode.Either) {
                    if(barcode.displayValue.contains("+")) {
                        prepareEnrollment(barcode.displayValue.substring(4));
                    } else if(barcode.displayValue.contains("-")) {
                        prepareVerification(barcode.displayValue.substring(4));
                    } else {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getSceneController().getContext(), R.string.scene_capture_invalid_unknown_barcode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getSceneController().getContext(), R.string.scene_capture_not_our_barcode, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void prepareEnrollment(final String barcode) {
            if(getSceneController().hasActor(R.id.actor_enroll)) {
                mPauseDetecting = true;
                CloudMessage message = CloudMessage.Validate("v2access.Intent.{@v2w_interaction_id}");
                message.putString("v2w_interaction_id", barcode);
                message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                message.putString("v2w_intent_type", "e");
                message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                    @Override
                    public void onResult(CloudResult result) {
                        getSceneDirector().setData(EnrollFragment.QR_INTENT_ID, barcode);
                        getSceneController().dispatchSwap(getSceneController().getActiveActorId(), R.id.actor_enroll);
                        mPauseDetecting = false;
                    }

                    @Override
                    public void onError(CloudError error) {
                        Toast.makeText(getSceneController().getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        mPauseDetecting = false;
                    }

                    @Override
                    public void onFailure(final Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(getSceneController().getContext(), ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        mPauseDetecting = false;
                    }
                });
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getSceneController().getContext(), "SceneActor is not configured for enrollments", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void prepareVerification(final String barcode) {
            if(getSceneController().hasActor(R.id.actor_verify)) {
                CloudMessage message = CloudMessage.Validate("v2access.Intent.{@v2w_interaction_id}");
                message.putString("v2w_interaction_id", barcode);
                message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                message.putString("v2w_intent_type", "v");
                mPauseDetecting = true;
                message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                    @Override
                    public void onResult(CloudResult result) {
                        if(result.hasData() && result.getData() instanceof QrIntent) {
                            final QrIntent qrIntent = (QrIntent)result.getData();
                            final SceneDirector sd = getSceneDirector();
                            final VerifyIntent verifyIntent;
                            if (qrIntent.hasDeviceId() &&
                                    !getContractController().contains(DevicesContract.CONTENT_URI,
                                            DevicesContract.DEVICE_ID, qrIntent.getDeviceId())) {
                                verifyIntent = new VerifyIntent(
                                        VerifyIntent.VerifyMode.VerifyQrRequest,
                                        VoxidemPreferences.getUserAccountName(),
                                        VoxidemPreferences.getUserVoicePrintId(),
                                        VoxidemPreferences.getUserLanguage()
                                );
                                verifyIntent.setDeviceQrId(qrIntent.getDeviceId(),
                                        qrIntent.getDeviceIp(),
                                        qrIntent.getDeviceInfo());
                            } else {
                                verifyIntent = new VerifyIntent(
                                        VerifyIntent.VerifyMode.VerifyQrCode,
                                        VoxidemPreferences.getUserAccountName(),
                                        VoxidemPreferences.getUserVoicePrintId(),
                                        VoxidemPreferences.getUserLanguage()
                                );
                            }
                            sd.setData(VerifyFragment.VERIFY_INTENT, verifyIntent);
                            if(qrIntent.hasAccounts()) {
                                AccountSelectionDialogFragment fragment = AccountSelectionDialogFragment.newInstance(
                                        getSceneController(),
                                        qrIntent,
                                        new AccountSelectionDialogFragment.AccountSelectionListener() {
                                            @Override
                                            public void onAccountSelected(final int id, final String name) {
                                                CloudMessage message = CloudMessage.Update("v2access.Intent.{@v2w_interaction_id}");
                                                message.putString("v2w_interaction_id", barcode);
                                                message.putString("v2w_account_id", VoxidemPreferences.getUserAccountName());
                                                message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                                                message.putInt("v2w_account_id", id);
                                                message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                                                    @Override
                                                    public void onResult(CloudResult result) {
                                                        verifyIntent.setCaptureInstance(barcode, name, id, qrIntent.getCompany(), qrIntent.getMaxAttempts());
                                                        verifyIntent.resolveIds(getContractController());
                                                        getSceneController().dispatchSwap(getSceneController().getActiveActorId(), R.id.actor_verify);
                                                        mPauseDetecting = false;
                                                    }

                                                    @Override
                                                    public void onError(CloudError error) {
                                                        Toast.makeText(getActivity(), "Please refresh the browser and Try again", Toast.LENGTH_LONG).show();
                                                        mPauseDetecting = false;
                                                    }

                                                    @Override
                                                    public void onFailure(Exception ex) {
                                                        ex.printStackTrace();
                                                        Toast.makeText(getActivity(), "Please refresh the browser and Try again", Toast.LENGTH_LONG).show();
                                                        mPauseDetecting = false;
                                                    }
                                                });
                                            }
                                            
                                            @Override
                                            public void onAccountCancelled() {
                                                mPauseDetecting = false;
                                            }
                                        }
                                );
                                getSceneController().showDialogFragment(fragment, "AccountSelectionDialogFragment");
                            } else if(qrIntent.hasAccount()) {
                                verifyIntent.setCaptureInstance(barcode, qrIntent.getAccount().getName(), qrIntent.getCompany(), qrIntent.getMaxAttempts());
                                verifyIntent.setHardware(qrIntent.getAccount().getType() == QrUserAccount.Type.Hardware);
                                verifyIntent.setLink(qrIntent.getAccount().getType() == QrUserAccount.Type.Link);
                                verifyIntent.resolveIds(getContractController());
                                getSceneController().dispatchSwap(getSceneController().getActiveActorId(), R.id.actor_verify);
                                mPauseDetecting = false;
                            }
                        }
                    }

                    @Override
                    public void onError(CloudError error) {
                        Toast.makeText(getActivity(), "Please refresh the browser and Try again", Toast.LENGTH_LONG).show();
                        mPauseDetecting = false;
                    }

                    @Override
                    public void onFailure(final Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(getActivity(), "Please refresh the browser and Try again", Toast.LENGTH_LONG).show();
                        mPauseDetecting = false;
                    }
                });
            } else {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getSceneController().getContext(), "SceneActor is not configured for verifications", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

}
