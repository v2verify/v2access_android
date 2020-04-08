package com.validvoice.voxidem.scenes.settings;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.steelkiwi.library.ui.SeparateShapesView;
import com.validvoice.dynamic.scene.IScenePermissionListener;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.dynamic.scene.widgets.KeyboardEditText;
import com.validvoice.dynamic.speech.service.CalibrateSpeechServiceConnection;
import com.validvoice.dynamic.voice.AudioFrame;
import com.validvoice.dynamic.voice.VoiceRecorder;
import com.validvoice.voxidem.R;

public class SettingsCalibrateFragment
        extends SceneFragment
        implements IScenePermissionListener,
            CalibrateSpeechServiceConnection.OnCalibrateListener {

    private static final String TAG = "SettingsCalibrateFrag";

    private CalibrateSpeechServiceConnection mCalibrateConnection;
    private Snackbar mTooNoisySnackbar;

    // UI References
    private Handler mUiHandler;
    private int mLastSelectedSilenceThreshold;

    // UI References
    private TextView scene_settings_calibration_listening;

    private TextView scene_settings_calibration_last_amplitude_high_value;
    private TextView scene_settings_calibration_last_amplitude_mean_value;
    private TextView scene_settings_calibration_last_amplitude_low_value;

    private TextView scene_settings_calibration_peak_amplitude_high_value;
    private TextView scene_settings_calibration_peak_amplitude_mean_value;
    private TextView scene_settings_calibration_peak_amplitude_low_value;

    private SeparateShapesView scene_settings_calibration_commit_button;

    // Resource caches
    private int mColorListening;
    private int mColorNotListening;

    public SettingsCalibrateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Resources resources = getResources();
        final Activity activity = getActivity();
        if(activity != null) {
            final Resources.Theme theme = activity.getTheme();
            mColorListening = ResourcesCompat.getColor(resources, R.color.colorStatusListening, theme);
            mColorNotListening = ResourcesCompat.getColor(resources, R.color.colorStatusNotListening, theme);
        }

        mUiHandler = new Handler();
        mCalibrateConnection = new CalibrateSpeechServiceConnection(2000, 500, this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean usesNoiseCancellation = sp.getBoolean(VoiceRecorder.PREF_NOISE_CANCELLATION, true);
        int defaultSilenceThreshold;
        if(!usesNoiseCancellation) {
            defaultSilenceThreshold = VoiceRecorder.DEFAULT_SILENCE_THRESHOLD;
            mLastSelectedSilenceThreshold = sp.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_SILENCE_THRESHOLD);
        } else {
            defaultSilenceThreshold = VoiceRecorder.DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD;
            mLastSelectedSilenceThreshold = sp.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
        }

        final View view = inflater.inflate(R.layout.fragment_settings_calibrate, container, false);

        scene_settings_calibration_listening = view.findViewById(R.id.scene_settings_calibration_listening);

        scene_settings_calibration_last_amplitude_high_value = view.findViewById(R.id.scene_settings_calibration_last_amplitude_high_value);
        scene_settings_calibration_last_amplitude_mean_value = view.findViewById(R.id.scene_settings_calibration_last_amplitude_mean_value);
        scene_settings_calibration_last_amplitude_low_value = view.findViewById(R.id.scene_settings_calibration_last_amplitude_low_value);

        scene_settings_calibration_peak_amplitude_high_value = view.findViewById(R.id.scene_settings_calibration_peak_amplitude_high_value);
        scene_settings_calibration_peak_amplitude_mean_value = view.findViewById(R.id.scene_settings_calibration_peak_amplitude_mean_value);
        scene_settings_calibration_peak_amplitude_low_value = view.findViewById(R.id.scene_settings_calibration_peak_amplitude_low_value);

        TextView scene_settings_calibration_silence_thresholds_default_value = view.findViewById(R.id.scene_settings_calibration_silence_thresholds_default_value);
        scene_settings_calibration_silence_thresholds_default_value.setText(String.format("%s", "" + defaultSilenceThreshold));

        KeyboardEditText scene_settings_calibration_silence_thresholds_current_value = view.findViewById(R.id.scene_settings_calibration_silence_thresholds_current_value);
        scene_settings_calibration_silence_thresholds_current_value.setText(String.format("%s", "" + mLastSelectedSilenceThreshold));
        scene_settings_calibration_silence_thresholds_current_value.setOnKeyboardListener(new KeyboardListener());
        scene_settings_calibration_silence_thresholds_current_value.addTextChangedListener(new CurrentSilenceThresholdTextWatcher());

        scene_settings_calibration_commit_button = view.findViewById(R.id.scene_settings_calibration_commit_button);
        scene_settings_calibration_commit_button.setOnButtonClickListener(new SeparateShapesViewClickListener());

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        requestPermission(1, Manifest.permission.RECORD_AUDIO, this);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        mCalibrateConnection.unbind(getContext());
        super.onStop();
    }

    @Override
    public void onPermissionGranted(@NonNull String permission) {
        Log.d(TAG, "onPermissionGranted");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            mCalibrateConnection.bind(getContext());
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
                                    SettingsCalibrateFragment.this);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onPermissionDenied(@NonNull String permission) {
        Log.d(TAG, "onPermissionDenied");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            makeSnackbar(R.string.no_record_audio_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getActivity().finish();
                        }
                    })
                    .show();
        }
    }

    ///
    /// OnVoiceListener Methods
    ///

    @Override
    public void onVoiceStart() {
        changeListeningStatus(true);
    }

    @Override
    public void onVoiceEnd() {
        changeListeningStatus(false);
    }

    @Override
    public void onBackgroundOk() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mTooNoisySnackbar != null) {
                    mTooNoisySnackbar.dismiss();
                    mTooNoisySnackbar = null;
                }
            }
        });
    }

    @Override
    public void onBackgroundTooNoisy() {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mTooNoisySnackbar == null) {
                    mTooNoisySnackbar = makeSnackbar(R.string.scene_settings_calibration_background_too_noisy, Snackbar.LENGTH_INDEFINITE);
                    mTooNoisySnackbar.show();
                }
            }
        });
    }

    @Override
    public void onReport(AudioFrame lastFrame, AudioFrame rollingFrame) {
        updateMicrophoneData(lastFrame, rollingFrame);
    }

    ///
    /// UI Methods
    ///

    private void changeListeningStatus(final boolean isListening) {
        Log.d(TAG, "changeListeningStatus");
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_settings_calibration_listening.setTextColor(isListening ? mColorListening : mColorNotListening);
            }
        });
    }

    private void updateMicrophoneData(final AudioFrame lastFrame, final AudioFrame rollingFrame) {
        Log.d(TAG, "updateMicrophoneData");
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_settings_calibration_last_amplitude_high_value.setText(String.format("%s", "" + lastFrame.getMaxAmplitude()));
                scene_settings_calibration_last_amplitude_mean_value.setText(String.format("%s", "" + lastFrame.getAverageAmplitude()));
                scene_settings_calibration_last_amplitude_low_value.setText(String.format("%s", "" + lastFrame.getMinAmplitude()));

                scene_settings_calibration_peak_amplitude_high_value.setText(String.format("%s", "" + rollingFrame.getMaxAmplitude()));
                scene_settings_calibration_peak_amplitude_mean_value.setText(String.format("%s", "" + rollingFrame.getAverageAmplitude()));
                scene_settings_calibration_peak_amplitude_low_value.setText(String.format("%s", "" + rollingFrame.getMinAmplitude()));
            }
        });
    }

    ///
    /// CurrentSilenceThresholdTextWatcher Class
    ///

    private class CurrentSilenceThresholdTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            try {
                mLastSelectedSilenceThreshold = Integer.parseInt(s.toString());
                mCalibrateConnection.setSilenceAmplitudeThreshold(mLastSelectedSilenceThreshold);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }

    ///
    /// KeyboardListener Class
    ///

    private class KeyboardListener implements KeyboardEditText.KeyboardListener {

        @Override
        public void onStateChanged(KeyboardEditText keyboardEditText, boolean showing) {
            if (showing) {
                scene_settings_calibration_commit_button.setVisibility(View.GONE);
            } else {
                scene_settings_calibration_commit_button.setVisibility(View.VISIBLE);
            }
        }

    }

    ///
    /// SeparateShapesViewClickListener Class
    ///

    private class SeparateShapesViewClickListener implements SeparateShapesView.OnButtonClickListener {

        @Override
        public boolean onLeftButtonClick() {
            // Commit

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final SharedPreferences.Editor editor = sp.edit();
            editor.putInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, mLastSelectedSilenceThreshold).apply();

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSceneController().dispatchCollapse(R.id.actor_calibrate);
                }
            }, 950);
            return true;
        }

        @Override
        public boolean onRightButtonClick() {
            // Cancel
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSceneController().dispatchCollapse(R.id.actor_calibrate);
                }
            }, 950);
            return true;
        }

        @Override
        public boolean onMiddleButtonClick() {
            return false;
        }
    }
}
