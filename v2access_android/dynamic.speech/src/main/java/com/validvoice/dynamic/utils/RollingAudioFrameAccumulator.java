package com.validvoice.dynamic.utils;

import com.validvoice.dynamic.voice.AudioFrame;

import java.util.ArrayList;

public class RollingAudioFrameAccumulator {

    ///
    /// Private Variables
    ///

    private final int mMaxSamples;
    private final ArrayList<AudioFrame> mSamples;

    private int mCount;
    private int mNextIndex;

    private double mLowSum;
    private double mMeanSum;
    private double mHighSum;

    ///
    /// Public Constructor
    ///

    public RollingAudioFrameAccumulator(int maxSamples) {
        mMaxSamples = maxSamples;
        mSamples = new ArrayList<>(maxSamples + 1);

        mCount = 0;
        mNextIndex = 0;
        mLowSum = 0;
        mMeanSum = 0;
        mHighSum = 0;
    }

    ///
    /// Public Methods
    ///

    public int getMaxSamples() {
        return mMaxSamples;
    }

    public int getSamplesCount() {
        return mCount;
    }

    public boolean isFull() {
        return mCount == mMaxSamples;
    }

    public void clear() {
        mCount = 0;
        mNextIndex = 0;
        mLowSum = 0;
        mMeanSum = 0;
        mHighSum = 0;
        mSamples.clear();
    }

    public void addSample(AudioFrame frame) {
        if(mCount == mMaxSamples) {
            AudioFrame sample = mSamples.get(mNextIndex);
            mLowSum -= sample.getMinAmplitude();
            mMeanSum -= sample.getAverageAmplitude();
            mHighSum -= sample.getMaxAmplitude();
            sample.copyFrom(frame);
        } else {
            mSamples.add(frame.clone());
            ++mCount;
        }

        mLowSum += frame.getMinAmplitude();
        mMeanSum += frame.getAverageAmplitude();
        mHighSum += frame.getMaxAmplitude();

        mNextIndex = (mNextIndex + 1) % mMaxSamples;
    }

    public AudioFrame computeSum() {
        return new AudioFrame((int)mLowSum, (int)mMeanSum, (int)mHighSum);
    }

    public AudioFrame computeMean() {
        if(mCount == 0) {
            return new AudioFrame();
        }
        return new AudioFrame(
            (int)(mLowSum / mCount),
            (int)(mMeanSum / mCount),
            (int)(mHighSum / mCount)
        );
    }

    public AudioFrame computeSmoothed(int smoothFactor) {
        assertTrue("Illegal Smoothing Factor",smoothFactor >= 1 && smoothFactor <= 99);

        int inverseSmoothFactor = (100 - smoothFactor);

        double lowResult = 0;
        double meanResult = 0;
        double highResult = 0;
        for(int i = 0; i < mCount; ++i) {
            AudioFrame sample = mSamples.get((mNextIndex + i) % mMaxSamples);
            lowResult = ((smoothFactor * lowResult) + (inverseSmoothFactor * sample.getMinAmplitude()))/100;
            meanResult = ((smoothFactor * meanResult) + (inverseSmoothFactor * sample.getAverageAmplitude()))/100;
            highResult = ((smoothFactor * highResult) + (inverseSmoothFactor * sample.getMaxAmplitude()))/100;
        }

        return new AudioFrame((int)lowResult, (int)meanResult, (int)highResult);
    }

    private void assertTrue(String message, boolean condition) {
        if(!condition) {
            throw new AssertionError(message);
        }
    }

}
