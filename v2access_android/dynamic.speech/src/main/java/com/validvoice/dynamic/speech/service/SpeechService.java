package com.validvoice.dynamic.speech.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.validvoice.dynamic.voice.AudioFrame;
import com.validvoice.dynamic.voice.VoiceRecorder;

import java.util.ArrayList;

public class SpeechService extends Service implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        VoiceRecorder.OnAudioFrameListener,
        VoiceRecorder.OnVoiceListener {

    private static final String TAG = "SpeechService";

    ///
    /// Final Variables
    ///

    private final SpeechServiceBinder mBinder = new SpeechServiceBinder();

    ///
    /// Variables
    ///

    private SharedPreferences mSharedPreferences;
    private VoiceRecorder mVoiceRecorder;
    private ArrayList<VoiceRecorder.OnAudioFrameListener> mOnAudioFrameListeners = new ArrayList<>();
    private ArrayList<VoiceRecorder.OnVoiceListener> mOnVoiceListeners = new ArrayList<>();

    ///
    /// Public Methods
    ///

    public void setSilenceAmplitudeThreshold(int threshold) {
        mVoiceRecorder.setSilenceAmplitudeThreshold(threshold);
    }

    public int getSilenceAmplitudeThreshold() {
        return mVoiceRecorder.getSilenceAmplitudeThreshold();
    }

    public void setSpeechTimeoutMillis(int timeoutMillis) {
        mVoiceRecorder.setSpeechTimeoutMillis(timeoutMillis);
    }

    public int getSpeechTimeoutMillis() {
        return mVoiceRecorder.getSpeechTimeoutMillis();
    }

    public void setSpeechMaxLengthMillis(int lengthMillis) {
        mVoiceRecorder.setSpeechMaxLengthMillis(lengthMillis);
    }

    public int getSpeechMaxLengthMillis() {
        return mVoiceRecorder.getSpeechMaxLengthMillis();
    }

    public int getSampleRate() {
        return mVoiceRecorder.getSampleRate();
    }

    public int getSizeInBytes() {
        return mVoiceRecorder.getSizeInBytes();
    }

    public int getLeadInBytes() {
        return mVoiceRecorder.getLeadInBytes();
    }

    public boolean isRunning() {
        return mVoiceRecorder.isRunning();
    }

    public boolean isAudioFrameReportingPaused() {
        return mVoiceRecorder.isAudioFrameReportingPaused();
    }

    public boolean isVoiceDetectionPaused() {
        return mVoiceRecorder.isVoiceDetectionPaused();
    }

    public void pauseAudioFrameReporting() {
        mVoiceRecorder.pauseAudioFrameReporting();
    }

    public void resumeAudioFrameReporting() {
        mVoiceRecorder.resumeAudioFrameReporting();
    }

    public void pauseVoiceDetection() {
        mVoiceRecorder.pauseVoiceDetection();
    }

    public void resumeVoiceDetection() {
        mVoiceRecorder.resumeVoiceDetection();
    }

    public void addAudioFrameListener(@NonNull VoiceRecorder.OnAudioFrameListener listener) {
        mOnAudioFrameListeners.add(listener);
    }

    public void removeAudioFrameListener(@NonNull VoiceRecorder.OnAudioFrameListener listener) {
        mOnAudioFrameListeners.remove(listener);
    }

    public void addVoiceListener(@NonNull VoiceRecorder.OnVoiceListener listener) {
        mOnVoiceListeners.add(listener);
    }

    public void removeVoiceListener(@NonNull VoiceRecorder.OnVoiceListener listener) {
        mOnVoiceListeners.remove(listener);
    }

    ///
    /// Private Service Methods
    ///

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        // get the application shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // enable the listener
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        buildVoiceRecorder(mSharedPreferences);
    }

    @Override
    public void onDestroy() {

        // disable the listener
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        mSharedPreferences = null;

        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop(false);
            mVoiceRecorder = null;
        }

        super.onDestroy();
    }

    ///
    /// Private SharedPreferences.OnSharedPreferenceChangeListener
    ///

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        // ensure the voice recorder has been created
        if(mVoiceRecorder == null)
            return;

        boolean usesNoiseCancellation = sharedPreferences.getBoolean(VoiceRecorder.PREF_NOISE_CANCELLATION, false);
        boolean isAdaptiveActive = sharedPreferences.getBoolean(AdaptiveSpeechServiceConnection.PREF_ADAPTIVE_MICROPHONE, false);

        switch(key) {
            case VoiceRecorder.PREF_SILENCE_THRESHOLD: {
                if (isAdaptiveActive)
                    return;

                int silenceThreshold;
                if (!usesNoiseCancellation) {
                    silenceThreshold = sharedPreferences.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_SILENCE_THRESHOLD);
                } else {
                    silenceThreshold = sharedPreferences.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
                }

                mVoiceRecorder.setSilenceAmplitudeThreshold(silenceThreshold);
            } return;
            case VoiceRecorder.PREF_NOISE_CANCELLATION: {
                buildVoiceRecorder(sharedPreferences);
            } break;
            case VoiceRecorder.PREF_LEAD_IN_MILLIS: {
                buildVoiceRecorder(sharedPreferences);
            } break;
        }
    }

    private void buildVoiceRecorder(SharedPreferences sharedPreferences) {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stopAndWait(false);
            mVoiceRecorder = null;
        }

        try {
            mVoiceRecorder = new VoiceRecorder
                    .Builder(sharedPreferences)
                    .setOnAudioFrameListener(this)
                    .setOnVoiceListener(this)
                    .build();
        } catch(Exception ex) {
            // this should never occur, since we are setting
            // the OnVoiceListener
            ex.printStackTrace();
        }

        mVoiceRecorder.start(false, false);
    }

    ///
    /// Private VoiceRecorder.OnAudioFrameListener
    ///

    @Override
    public void onFrameAvailable(AudioFrame lastFrame) {
        if(mOnAudioFrameListeners.size() > 0) {
            for (VoiceRecorder.OnAudioFrameListener listener : mOnAudioFrameListeners) {
                listener.onFrameAvailable(lastFrame);
            }
        }
    }

    ///
    /// Private VoiceRecorder.OnVoiceListener
    ///

    @Override
    public void onVoiceStart() {
        if(mOnVoiceListeners.size() > 0) {
            for (VoiceRecorder.OnVoiceListener listener : mOnVoiceListeners) {
                listener.onVoiceStart();
            }
        }
    }

    @Override
    public void onVoice(byte[] data, int size) {
        if(mOnVoiceListeners.size() > 0) {
            for (VoiceRecorder.OnVoiceListener listener : mOnVoiceListeners) {
                listener.onVoice(data, size);
            }
        }
    }

    @Override
    public void onVoiceEnd() {
        if(mOnVoiceListeners.size() > 0) {
            for (VoiceRecorder.OnVoiceListener listener : mOnVoiceListeners) {
                listener.onVoiceEnd();
            }
        }
    }

    ///
    /// Binder Helper
    ///

    public static SpeechService from(IBinder binder) {
        return ((SpeechServiceBinder) binder).getService();
    }

    ///
    /// Binder Class
    ///

    private class SpeechServiceBinder extends Binder {
        SpeechService getService() {
            return SpeechService.this;
        }
    }

}
