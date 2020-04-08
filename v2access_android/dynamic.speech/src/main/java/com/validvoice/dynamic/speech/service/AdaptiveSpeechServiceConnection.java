package com.validvoice.dynamic.speech.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.validvoice.dynamic.utils.RollingAudioFrameAccumulator;
import com.validvoice.dynamic.voice.AudioFrame;
import com.validvoice.dynamic.voice.VoiceRecorder;

import java.util.ArrayList;

public class AdaptiveSpeechServiceConnection implements ServiceConnection,
        SharedPreferences.OnSharedPreferenceChangeListener,
        VoiceRecorder.OnAudioFrameListener {

    private static final String TAG = AdaptiveSpeechServiceConnection.class.getSimpleName();

    // TODO: Need to determine if these numbers are accurate
    private static final int BACKGROUND_TOO_NOISY_THRESHOLD = 2000;
    private static final int BACKGROUND_OK_THRESHOLD = 4000;
    private static final int BACKGROUND_TOO_NOISY_NOISE_CANCELLATION_THRESHOLD = 1700;
    private static final int BACKGROUND_OK_NOISE_CANCELLATION_THRESHOLD = 1400;

    public static final String PREF_ADAPTIVE_MICROPHONE = "pref_adaptive_microphone";

    ///
    /// Private Final Variables
    ///

    private final double mSamplesInMillis;

    ///
    /// Private Variables
    ///

    private SpeechService mService;
    private ArrayList<OnAdaptiveSpeechListener> mOnAdaptiveSpeechListeners;
    private RollingAudioFrameAccumulator mRollingAudioFrameAccummulator;
    private SharedPreferences mSharedPreferences;
    private boolean mIsConnected;
    private boolean mIsActive;
    private boolean mIsBound;
    private boolean mBackgroundNoiseCalibrating;
    private boolean mBackgroundTooNoisy;
    private int mDefaultSilenceThreshold;
    private int mBackgroundTooNoisyThreshold;
    private int mBackgroundOkThreshold;
    private int mCalibratedSilenceThreshold;

    ///
    /// Public Constructor
    ///

    public AdaptiveSpeechServiceConnection(int samplesInMillis, @NonNull OnAdaptiveSpeechListener listener) {
        mSamplesInMillis = samplesInMillis;
        mOnAdaptiveSpeechListeners = new ArrayList<>();
        mOnAdaptiveSpeechListeners.add(listener);
        mIsConnected = false;
        mIsActive = false;
        mIsBound = false;
        mBackgroundNoiseCalibrating = false;
        mBackgroundTooNoisy = false;
        mDefaultSilenceThreshold = 0;
        mBackgroundTooNoisyThreshold = 0;
        mBackgroundOkThreshold = 0;
        mCalibratedSilenceThreshold = 0;
    }

    ///
    /// Public Methods
    ///

    public void bind(Context context) {
        Log.d(TAG, "bind");

        if(!mIsBound) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            mIsActive = mSharedPreferences.getBoolean(PREF_ADAPTIVE_MICROPHONE, false);

            boolean usesNoiseCancellation = mSharedPreferences.getBoolean(VoiceRecorder.PREF_NOISE_CANCELLATION, true);
            if (!usesNoiseCancellation) {
                mDefaultSilenceThreshold = mSharedPreferences.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_SILENCE_THRESHOLD);
                mBackgroundTooNoisyThreshold = BACKGROUND_TOO_NOISY_THRESHOLD;
                mBackgroundOkThreshold = BACKGROUND_OK_THRESHOLD;
            } else {
                mDefaultSilenceThreshold = mSharedPreferences.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
                mBackgroundTooNoisyThreshold = BACKGROUND_TOO_NOISY_NOISE_CANCELLATION_THRESHOLD;
                mBackgroundOkThreshold = BACKGROUND_OK_NOISE_CANCELLATION_THRESHOLD;
            }

            context.bindService(new Intent(context, SpeechService.class), this, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    public void unbind(Context context) {
        Log.d(TAG, "unbind");
        if(mIsBound) {
            mIsBound = false;
            disconnect();
            context.unbindService(this);
        }
    }

    public void startCalibrating(OnAdaptiveSpeechListener listener) {
        if(mIsActive && !mBackgroundNoiseCalibrating) {
            mBackgroundNoiseCalibrating = true;
            mCalibratedSilenceThreshold = 0;

            if(listener != null && !mOnAdaptiveSpeechListeners.contains(listener)) {
                mOnAdaptiveSpeechListeners.add(listener);
            }

            // calculate calibration range
            double sampleRate = mService.getSampleRate() * 2; // 2 - need to add bit depth
            double sizeInBytes = mService.getSizeInBytes();
            int maxSamples = (int) ((sampleRate / sizeInBytes) * (mSamplesInMillis / 1000));
            mRollingAudioFrameAccummulator = new RollingAudioFrameAccumulator(maxSamples);

            // enable the audio frame listener
            mService.addAudioFrameListener(this);
            mService.resumeAudioFrameReporting();
        }
    }

    public void stopCalibrating(OnAdaptiveSpeechListener listener) {
        if(mIsActive && mBackgroundNoiseCalibrating) {
            mBackgroundNoiseCalibrating = false;

            if(listener != null && mOnAdaptiveSpeechListeners.contains(listener)) {
                mOnAdaptiveSpeechListeners.remove(listener);
            }
        }
    }

    ///
    /// SharedPreferences.OnSharedPreferenceChangeListener
    ///

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(VoiceRecorder.PREF_NOISE_CANCELLATION) || key.equals(VoiceRecorder.PREF_SILENCE_THRESHOLD)) {

            boolean usesNoiseCancellation = sharedPreferences.getBoolean(VoiceRecorder.PREF_NOISE_CANCELLATION, true);

            Log.d(TAG, "Adaptive Microphone changing (Noise Cancellation or SilenceThreshold)");

            if (!usesNoiseCancellation) {
                mDefaultSilenceThreshold = sharedPreferences.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_SILENCE_THRESHOLD);
                mBackgroundTooNoisyThreshold = BACKGROUND_TOO_NOISY_THRESHOLD;
                mBackgroundOkThreshold = BACKGROUND_OK_THRESHOLD;
            } else {
                mDefaultSilenceThreshold = sharedPreferences.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
                mBackgroundTooNoisyThreshold = BACKGROUND_TOO_NOISY_NOISE_CANCELLATION_THRESHOLD;
                mBackgroundOkThreshold = BACKGROUND_OK_NOISE_CANCELLATION_THRESHOLD;
            }
        } else if(key.equals(PREF_ADAPTIVE_MICROPHONE)) {

            boolean isActive = sharedPreferences.getBoolean(PREF_ADAPTIVE_MICROPHONE, false);

            Log.d(TAG, "Adaptive Microphone changing (from " + mIsActive + " to " + isActive + ")");
            mIsActive = isActive;

            if (!mIsActive && mBackgroundNoiseCalibrating) {
                mBackgroundNoiseCalibrating = false;
            } else {
                startCalibrating(null);
            }
        }
    }

    ///
    /// Private ServiceConnection
    ///

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected");
        mService = SpeechService.from(service);
        connect();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        disconnect();
        mService = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        Log.d(TAG, "onBindingDied");
        disconnect();
        mService = null;
    }

    ///
    ///
    ///

    private void connect() {
        if(!mIsConnected) {
            mIsConnected = true;

            // enable the preference listener
            mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

            // calibrate
            startCalibrating(null);
        }
    }

    private void disconnect() {
        if(mIsConnected) {
            mIsConnected = false;

            if (mIsActive) {
                // remove and disconnect the audio frame listener
                mService.pauseAudioFrameReporting();
                mService.removeAudioFrameListener(this);
                if(mRollingAudioFrameAccummulator != null) {
                    mRollingAudioFrameAccummulator.clear();
                    mRollingAudioFrameAccummulator = null;
                }
            }

            // remove and disconnect the preference listener
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
            mSharedPreferences = null;
        }
    }

    ///
    /// Private VoiceRecorder.OnAudioFrameListener
    ///

    @Override
    public void onFrameAvailable(AudioFrame lastFrame) {

        // We only want to collect samples when:
        //     * Adaptive Microphone is turned on
        //     * We are not in an enrollment or verification
        //
        // Continuously process background noise when not in an enrollment or
        // verification, this way, if the user opens the app in one room, preemptively,
        // and then verifies in a different room, we continuously evaluate the background
        // to ascertain the best background threshold. To do this, we will start evaluating
        // background noise levels once we have gathered 1.5 seconds of noise. After we have
        // gathered the initial 1.5 seconds of noise, we will evaluate the peak levels,
        //
        // if they are above the too noisy level
        //     call OnAdaptiveSpeechListener.onBackgroundTooNoisy()
        // else if they were above the too noise level and have dropped back down below the ok level
        //     call OnAdaptiveSpeechListener.onBackgroundOk()
        // else if they are above the default
        //     set silenceThreshold to 10% above max peak
        // else if they are below the default
        //     if the default is not set, then set it
        //
        // Be proactive, if the background noise level is beyond a maximum tolerance
        // threshold, the post to the OnAdaptiveSpeechListener.onBackgroundTooNoisy(),
        // post to OnAdaptiveSpeechListener.onBackgroundOk() when the background noise
        // has reached a level of sufficiently less background noise.
        //
        // If the background level is less than the default level, then use the default level,
        // otherwise adapt to an average above the background level.

        if(!mBackgroundNoiseCalibrating) {
            mService.pauseAudioFrameReporting();
            mService.removeAudioFrameListener(this);
            mRollingAudioFrameAccummulator.clear();
            mRollingAudioFrameAccummulator = null;
            return;
        }

        mRollingAudioFrameAccummulator.addSample(lastFrame);

        if(!mRollingAudioFrameAccummulator.isFull())
            return;

        AudioFrame sample = mRollingAudioFrameAccummulator.computeMean();
        int maxAmplitude = sample.getMaxAmplitude();
        int calibratedThreshold;

        if(maxAmplitude > mBackgroundTooNoisyThreshold) {
            if(!mBackgroundTooNoisy) {
                Log.d(TAG, "Adaptive Microphone detected background is too noisy");

                mBackgroundTooNoisy = true;
                dispatchOnBackgroundTooNoisy();
            }
            return;
        } else if(mBackgroundTooNoisy && maxAmplitude < mBackgroundOkThreshold) {
            Log.d(TAG, "Adaptive Microphone detected background returned to normal");

            mBackgroundTooNoisy = false;
            dispatchOnBackgroundOk();
            return;
        } else if(maxAmplitude > mDefaultSilenceThreshold) {
            calibratedThreshold = mCalibratedSilenceThreshold;
            if(maxAmplitude > mCalibratedSilenceThreshold) {
                calibratedThreshold = (int) Math.round(maxAmplitude * 1.1);

                Log.d(TAG, "Adaptive Microphone detected background is noisier than default threshold.");
            }
        } else {
            calibratedThreshold = mDefaultSilenceThreshold;
        }

        if(mCalibratedSilenceThreshold != calibratedThreshold) {
            Log.d(TAG, "Adaptive Microphone adjusting silence threshold to: " + calibratedThreshold);
            mCalibratedSilenceThreshold = calibratedThreshold;
            mService.setSilenceAmplitudeThreshold(mCalibratedSilenceThreshold);
            dispatchOnBackgroundCalibrated(mCalibratedSilenceThreshold);
        }
    }

    ///
    /// Dispatch Methods
    ///

    private void dispatchOnBackgroundOk() {
        for(OnAdaptiveSpeechListener listener : mOnAdaptiveSpeechListeners) {
            listener.onBackgroundOk();
        }
    }

    private void dispatchOnBackgroundTooNoisy() {
        for(OnAdaptiveSpeechListener listener : mOnAdaptiveSpeechListeners) {
            listener.onBackgroundTooNoisy();
        }
    }

    private void dispatchOnBackgroundCalibrated(int level) {
        for(OnAdaptiveSpeechListener listener : mOnAdaptiveSpeechListeners) {
            listener.onBackgroundCalibrated(level);
        }
    }

    ///
    /// OnAdaptiveSpeechListener interface
    ///

    public interface OnAdaptiveSpeechListener {

        void onBackgroundOk();

        void onBackgroundTooNoisy();

        void onBackgroundCalibrated(int level);
    }
}
