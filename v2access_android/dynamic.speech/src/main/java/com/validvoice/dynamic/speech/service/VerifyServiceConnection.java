package com.validvoice.dynamic.speech.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.validvoice.dynamic.audio.AudioBuffer;
import com.validvoice.dynamic.speech.authorization.SveVerifier;
import com.validvoice.dynamic.voice.VoiceRecorder;

public class VerifyServiceConnection
        implements ServiceConnection,
            VoiceRecorder.OnVoiceListener {

    private static final String TAG = VerifyServiceConnection.class.getSimpleName();

    ///
    /// Private Final Variables
    ///

    private final OnVerifyListener mOnVerifyListener;

    ///
    /// Private Variables
    ///
    private SpeechService mService;
    private boolean mIsConnected;
    private boolean mAutoStart;

    private SveVerifier mVerifier;
    private AudioBuffer mAudioBuffer;

    ///
    /// Public Constructor
    ///

    public VerifyServiceConnection(@NonNull OnVerifyListener onVerifyListener) {
        mOnVerifyListener = onVerifyListener;
        mIsConnected = false;
        mAutoStart = false;

        mVerifier = new SveVerifier();
        mAudioBuffer = new AudioBuffer();
    }

    ///
    /// Public Methods
    ///

    public VerifyServiceConnection setClientId(String clientId) {
        mVerifier.setClientId(clientId);
        return this;
    }

    public VerifyServiceConnection setInteractionId(String interactionId) {
        mVerifier.setInteractionId(interactionId);
        return this;
    }

    public VerifyServiceConnection setInteractionTag(String interactionTag) {
        mVerifier.setInteractionTag(interactionTag);
        return this;
    }

    public VerifyServiceConnection setMetaData(String name, boolean value) {
        mVerifier.setMetaData(name, value);
        return this;
    }

    public VerifyServiceConnection setMetaData(String name, int value) {
        mVerifier.setMetaData(name, value);
        return this;
    }

    VerifyServiceConnection setMetaData(String name, double value) {
        mVerifier.setMetaData(name, value);
        return this;
    }

    public VerifyServiceConnection setMetaData(String name, String value) {
        mVerifier.setMetaData(name, value);
        return this;
    }

    public void bind(Context context) {
        bind(context, false);
    }

    public void bind(Context context, boolean autoStart) {
        Log.d(TAG, "bind");
        mAutoStart = autoStart;
        context.bindService(new Intent(context, SpeechService.class), this, Context.BIND_AUTO_CREATE);
    }

    public void unbind(Context context) {
        Log.d(TAG, "unbind");
        disconnect();
        context.unbindService(this);
    }

    public boolean isSessionOpen() {
        return mService != null && mVerifier.isSessionOpen();
    }

    public void start() {
        if(mService != null) {
            mService.pauseVoiceDetection();
            mVerifier.setMetaData("Silence-Threshold", mService.getSilenceAmplitudeThreshold());
            mVerifier.start(new SveVerifier.StartCallback() {
                @Override
                public void onStartComplete() {
                    Log.d(TAG, "start().onStartComplete");
                    mOnVerifyListener.onStartComplete();
                    mService.resumeVoiceDetection();
                }

                @Override
                public void onFailure(final Exception ex) {
                    Log.d(TAG, "start().onFailure");
                    mOnVerifyListener.onFailure(ex);
                }
            });
        }
    }

    public void setSpeechContexts() {

    }

    public void post() {

    }

    public void end() {

    }

    public void cancel(String reason) {

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
        if(!mIsConnected) {
            mIsConnected = true;
            mService.pauseVoiceDetection();
            mService.addVoiceListener(this);

            if(mAutoStart) {
                start();
            }
        }
    }

    private void disconnect() {
        if(mIsConnected) {
            mIsConnected = false;
            mService.pauseVoiceDetection();
            mService.removeVoiceListener(this);
        }
    }

    ///
    ///
    ///

    @Override
    public void onVoiceStart() {
        mOnVerifyListener.onVoiceStart();
    }

    @Override
    public void onVoice(byte[] data, int size) {
        mAudioBuffer.write(data, size);
    }

    @Override
    public void onVoiceEnd() {
        mOnVerifyListener.onVoiceEnd();
    }

    ///
    ///
    ///

    public interface OnVerifyListener {

        void onFailure(Exception ex);

        void onStartBegin();

        void onStartComplete();

        void onVoiceStart();

        void onVoiceEnd();

        void onPostBegin();

        void onPostComplete(SveVerifier.Result result);

        void onEndBegin();

        void onEndComplete(SveVerifier.Result result);

        void onCancelBegin();

        void onCancelComplete();

    }
}
