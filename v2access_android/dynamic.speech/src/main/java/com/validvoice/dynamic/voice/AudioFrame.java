package com.validvoice.dynamic.voice;

public class AudioFrame {

    int mLow;
    int mMean;
    int mHigh;
    boolean mVoiceDetected;

    public AudioFrame() {
        mLow = mMean = mHigh = 0;
        mVoiceDetected = false;
    }

    public AudioFrame(int low, int mean, int high) {
        mLow = low;
        mMean = mean;
        mHigh = high;
        mVoiceDetected = false;
    }

    private AudioFrame(int low, int mean, int high, boolean voiceDetected) {
        mLow = low;
        mMean = mean;
        mHigh = high;
        mVoiceDetected = voiceDetected;
    }

    public int getMinAmplitude() {
        return mLow;
    }

    public int getAverageAmplitude() {
        return mMean;
    }

    public int getMaxAmplitude() {
        return mHigh;
    }

    public boolean isVoiceDetected () {
        return mVoiceDetected;
    }

    public AudioFrame clone() {
        return new AudioFrame(mLow, mMean, mHigh, mVoiceDetected);
    }

    public void copyFrom(AudioFrame frame) {
        mLow = frame.mLow;
        mMean = frame.mMean;
        mHigh = frame.mHigh;
        mVoiceDetected = frame.mVoiceDetected;
    }

    public void clear() {
        mLow = mMean = mHigh = 0;
        mVoiceDetected = false;
    }
}
