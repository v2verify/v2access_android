package com.validvoice.dynamic.speech.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.validvoice.dynamic.voice.VoiceRecorder;

public class SpeechServiceConnection implements ServiceConnection {

    private static final String TAG = SpeechServiceConnection.class.getSimpleName();

    ///
    /// Public Interface
    ///

    public interface SpeechServiceListener extends VoiceRecorder.OnVoiceListener {
        void onSpeechServiceConnected();
        void onSpeechServiceDisconnected();
    }

    ///
    /// Private Final Variables
    ///

    private final SpeechServiceListener mSpeechServiceListener;

    ///
    /// Private Variables
    ///

    private SpeechService mService;
    private boolean mIsConnected;
    private boolean mIsBound;

    ///
    /// Public Constructor
    ///

    public SpeechServiceConnection(@NonNull SpeechServiceListener speechServiceListener) {
        mSpeechServiceListener = speechServiceListener;
        mIsConnected = false;
        mIsBound = false;
    }

    ///
    /// Public Methods
    ///

    public void bind(@NonNull Context context) {
        Log.d(TAG, "bind");
        if(!mIsBound) {
            context.bindService(new Intent(context, SpeechService.class), this, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    public void unbind(@NonNull Context context) {
        Log.d(TAG, "unbind");
        if(mIsBound) {
            mIsBound = false;
            disconnect();
            context.unbindService(this);
        }
    }

    public boolean isConnected() {
        return mService != null && mIsConnected;
    }

    public void pause() {
        if(isConnected()) {
            mService.pauseVoiceDetection();
        }
    }

    public void resume() {
        if(isConnected()) {
            mService.resumeVoiceDetection();
        }
    }

    public int getSilenceAmplitudeThreshold() {
        return isConnected() ? mService.getSilenceAmplitudeThreshold() : 0;
    }

    public void setSpeechTimeoutMillis(int timeoutMillis) {
        if(isConnected()) {
            mService.setSpeechTimeoutMillis(timeoutMillis);
        }
    }

    public int getSpeechTimeoutMillis() {
        return isConnected() ? mService.getSpeechTimeoutMillis() : 0;
    }

    public void setSpeechMaxLengthMillis(int lengthMillis) {
        if(isConnected()) {
            mService.setSpeechMaxLengthMillis(lengthMillis);
        }
    }

    public int getSpeechMaxLengthMillis() {
        return isConnected() ? mService.getSpeechMaxLengthMillis() : 0;
    }

    ///
    ///
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
        Log.d(TAG, "connect");
        if(!mIsConnected) {
            mIsConnected = true;
            mService.pauseVoiceDetection();
            mService.addVoiceListener(mSpeechServiceListener);
            mSpeechServiceListener.onSpeechServiceConnected();
        }
    }

    private void disconnect() {
        Log.d(TAG, "disconnect");
        if(mIsConnected) {
            mIsConnected = false;
            mSpeechServiceListener.onSpeechServiceDisconnected();
            mService.pauseVoiceDetection();
            mService.removeVoiceListener(mSpeechServiceListener);
        }
    }

}
