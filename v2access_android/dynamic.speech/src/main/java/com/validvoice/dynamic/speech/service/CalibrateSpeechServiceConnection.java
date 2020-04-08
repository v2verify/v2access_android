package com.validvoice.dynamic.speech.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.validvoice.dynamic.utils.RollingAudioFrameAccumulator;
import com.validvoice.dynamic.voice.AudioFrame;
import com.validvoice.dynamic.voice.VoiceRecorder;

public class CalibrateSpeechServiceConnection implements ServiceConnection,
        VoiceRecorder.OnAudioFrameListener,
        VoiceRecorder.OnVoiceListener {

    private static final String TAG = CalibrateSpeechServiceConnection.class.getSimpleName();

    ///
    /// Private Final Variables
    ///

    private final OnCalibrateListener mOnCalibrateListener;
    private final double mSampleInMillis;
    private final double mReportEveryMillis;

    ///
    /// Private Variables
    ///

    private SpeechService mService;
    private boolean mIsConnected;
    private boolean mIsBound;
    private RollingAudioFrameAccumulator mRollingAudioFrameAccummulator;
    private RollingAudioFrameAccumulator mRollingPeakAccummulator;
    private AudioFrame mActivePeakFrame;
    private AudioFrame mRealizedPeakFrame;
    private int mSizeInBytes;
    private int mCurrentBytes;
    private int mReportBytes;
    private boolean mReportedTooNoisy;

    ///
    /// Public Constructor
    ///

    public CalibrateSpeechServiceConnection(int samplesInMillis, int reportEveryMillis,
            @NonNull OnCalibrateListener listener) {
        mIsConnected = false;
        mIsBound = false;
        mSampleInMillis = samplesInMillis;
        mReportEveryMillis = reportEveryMillis;
        mOnCalibrateListener = listener;
        mSizeInBytes = 0;
        mCurrentBytes = 0;
        mReportBytes = 0;
        mReportedTooNoisy = false;
    }

    ///
    /// Public Methods
    ///

    public void bind(Context context) {
        Log.d(TAG, "bind");
        if(!mIsBound) {
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

    public void setSilenceAmplitudeThreshold(int threshold) {
        if(mService != null) {
            mService.setSilenceAmplitudeThreshold(threshold);
        }
    }

    public boolean isConnected() {
        return mService != null && mIsConnected;
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

            mSizeInBytes = mService.getSizeInBytes();
            mCurrentBytes = 0;

            // calculate calibration range
            double sampleRate = mService.getSampleRate() * 2; // 2 - need to add the bit depth
            double sizeInBytes = mSizeInBytes;
            int maxSamples = (int) ((sampleRate / sizeInBytes) * (mSampleInMillis / 1000));

            mReportBytes = (int) (sampleRate * (mReportEveryMillis / 1000));

            mRollingAudioFrameAccummulator = new RollingAudioFrameAccumulator(maxSamples);
            mRollingPeakAccummulator = new RollingAudioFrameAccumulator((int) (mSampleInMillis / mReportEveryMillis));
            mActivePeakFrame = new AudioFrame();
            mRealizedPeakFrame = new AudioFrame();
            mReportedTooNoisy = false;

            // enable the listeners
            mService.addAudioFrameListener(this);
            mService.addVoiceListener(this);

            mService.resumeAudioFrameReporting();
            mService.resumeVoiceDetection();
        }
    }

    private void disconnect() {
        if(mIsConnected) {
            mIsConnected = false;

            mService.pauseVoiceDetection();
            mService.pauseAudioFrameReporting();

            // remove and disconnect the listeners
            mService.removeVoiceListener(this);
            mService.removeAudioFrameListener(this);

            mRollingAudioFrameAccummulator.clear();
            mRollingAudioFrameAccummulator = null;
        }
    }

    ///
    /// Private VoiceRecorder.OnAudioFrameListener
    ///

    @Override
    public void onFrameAvailable(AudioFrame lastFrame) {
        mRollingAudioFrameAccummulator.addSample(lastFrame);

        if(lastFrame.getMaxAmplitude() > mActivePeakFrame.getMaxAmplitude()) {
            mActivePeakFrame.copyFrom(lastFrame);
        }

        if(mRollingPeakAccummulator.isFull()) {
            AudioFrame mean = mRollingPeakAccummulator.computeMean();
            if(!mReportedTooNoisy) {
                if (mean.getMaxAmplitude() > 22000) {
                    mReportedTooNoisy = true;
                    mRollingPeakAccummulator.clear();
                    mOnCalibrateListener.onBackgroundTooNoisy();
                }
            } else {
                if (mean.getMaxAmplitude() < 20000) {
                    mReportedTooNoisy = false;
                    mRollingPeakAccummulator.clear();
                    mOnCalibrateListener.onBackgroundOk();
                }
            }
        }

        if(mCurrentBytes == 0) {
            mOnCalibrateListener.onReport(lastFrame, lastFrame);
        } else if(mCurrentBytes >= mReportBytes) {
            mCurrentBytes = 0;
            mRealizedPeakFrame.copyFrom(mActivePeakFrame);
            mRollingPeakAccummulator.addSample(mRealizedPeakFrame);
            mOnCalibrateListener.onReport(mRollingAudioFrameAccummulator.computeMean(), mRealizedPeakFrame);
            mActivePeakFrame.clear();
        }

        mCurrentBytes += mSizeInBytes;
    }

    ///
    /// Private VoiceRecorder.OnVoiceListener
    ///

    @Override
    public void onVoiceStart() {
        mOnCalibrateListener.onVoiceStart();
    }

    @Override
    public void onVoice(byte[] data, int size) {
    }

    @Override
    public void onVoiceEnd() {
        mOnCalibrateListener.onVoiceEnd();
    }

    ///
    ///
    ///

    public interface OnCalibrateListener {

        void onVoiceStart();

        void onVoiceEnd();

        void onBackgroundOk();

        void onBackgroundTooNoisy();

        void onReport(AudioFrame lastFrame, AudioFrame rollingFrame);

    }

}
