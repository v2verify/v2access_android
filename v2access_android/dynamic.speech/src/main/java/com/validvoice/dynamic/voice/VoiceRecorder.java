/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Moved from package(com.google.cloud.android.speech) to package(com.validvoice.dynamic.voice)
 *
 * Changed Speech Parameters from static final elements to changeable parameters
 */

package com.validvoice.dynamic.voice;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.validvoice.dynamic.utils.CircularByteBuffer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Continuously records audio and notifies the {@link OnVoiceListener} when voice (or any
 * sound) is heard.
 *
 * <p>The recorded audio format is always {@link AudioFormat#ENCODING_PCM_16BIT} and
 * {@link AudioFormat#CHANNEL_IN_MONO}. This class will automatically pick the right sample rate
 * for the device. Use {@link #getSampleRate()} to get the selected value.</p>
 */
public class VoiceRecorder {

    private static final String TAG = "VoiceRecorder";

    public static final String PREF_SILENCE_THRESHOLD = "pref_silence_threshold";
    public static final String PREF_LEAD_IN_MILLIS = "pref_lead_in_millis";
    public static final String PREF_NOISE_CANCELLATION = "pref_noise_cancellation";

    public static final int DEFAULT_SILENCE_THRESHOLD = 5000;
    public static final int DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD = 1500;
    public static final int DEFAULT_SPEECH_LEAD_IN_MILLIS = 100;
    public static final int DEFAULT_SPEECH_TIMEOUT_MILLIS = 2000;
    public static final int DEFAULT_SPEECH_MAX_LENGTH_MILLIS = 30 * 1000;

    private static final int[] SAMPLE_RATE_CANDIDATES = new int[]{8000, 16000, 11025, 22050, 44100};

    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private final OnAudioFrameListener mOnAudioFrameListener;
    private final OnVoiceListener mOnVoiceListener;

    private AudioRecord mAudioRecord;

    private Thread mThread;

    private final Object mLock = new Object();

    private AtomicBoolean mShouldRun = new AtomicBoolean(true);

    private AtomicBoolean mShouldReportFrame = new AtomicBoolean(false);

    private AtomicBoolean mShouldDetectVoice = new AtomicBoolean(false);

    /** The timestamp of the last time that voice is heard. */
    private long mLastVoiceHeardMillis = Long.MAX_VALUE;

    /** The timestamp when the current voice is started. */
    private long mVoiceStartedMillis;

    /** Stores the last AudioFrame amplitude data */
    private AudioFrame mLastFrame;

    /** The voice threshold. */
    private int mSilenceAmplitudeThreshold = DEFAULT_SILENCE_THRESHOLD;

    /** The sample source */
    private int mSampleSource;

    /** The sample rate */
    private int mSampleRate;

    /** The size in bytes to read from the audio recorder */
    private int mSizeInBytes;

    /** The size in bytes to read from the audio recorder */
    private int mLeadInBytes;

    /** The time out for when voice is no longer detected */
    private int mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;

    /** The max speech collected before starting a new sampling */
    private int mSpeechMaxLengthMillis = DEFAULT_SPEECH_MAX_LENGTH_MILLIS;

    private VoiceRecorder(@NonNull OnAudioFrameListener onAudioFrameListener,
                          @NonNull OnVoiceListener onVoiceListener,
                          int sampleSource, int sampleRate, int sizeInBytes, int leadInBytes) {
        mOnAudioFrameListener = onAudioFrameListener;
        mOnVoiceListener = onVoiceListener;
        mSampleSource = sampleSource;
        mSampleRate = sampleRate;
        mSizeInBytes = sizeInBytes;
        mLeadInBytes = leadInBytes;
    }

    public void setSilenceAmplitudeThreshold(int threshold) {
        if(threshold > 0) {
            mSilenceAmplitudeThreshold = threshold;
        }
    }

    public int getSilenceAmplitudeThreshold() {
        return mSilenceAmplitudeThreshold;
    }

    public void setSpeechTimeoutMillis(int timeoutMillis) {
        if(timeoutMillis > 0) {
            mSpeechTimeoutMillis = timeoutMillis;
        }
    }

    public int getSpeechTimeoutMillis() {
        return mSpeechTimeoutMillis;
    }

    public void setSpeechMaxLengthMillis(int lengthMillis) {
        if(lengthMillis > 0) {
            mSpeechMaxLengthMillis = lengthMillis;
        }
    }

    public int getSpeechMaxLengthMillis() {
        return mSpeechMaxLengthMillis;
    }

    public void restoreDefaults() {
        mSilenceAmplitudeThreshold = DEFAULT_SILENCE_THRESHOLD;
        mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;
        mSpeechMaxLengthMillis = DEFAULT_SPEECH_MAX_LENGTH_MILLIS;
        mLastFrame = new AudioFrame();
    }

    /**
     * Starts recording audio.
     *
     * <p>The caller is responsible for calling {@link #stop(boolean performDismiss)} later.</p>
     */
    public void start() {
        start(false, true);
    }

    /**
     * Starts recording audio.
     *
     * <p>The caller is responsible for calling {@link #stop(boolean performDismiss)} later.</p>
     */
    public void start(boolean reportAudioFrame, boolean detectVoice) {
        Log.d(TAG, "Starting");
        // Stop recording if it is currently ongoing.
        stop(false);
        // Try to create a new recording session.
        mAudioRecord = createAudioRecord();
        if (mAudioRecord == null) {
            throw new RuntimeException("Cannot instantiate VoiceRecorder");
        }
        mShouldReportFrame.set(reportAudioFrame);
        mShouldDetectVoice.set(detectVoice);
        mShouldRun.set(true);
        mLastFrame = new AudioFrame();
        // start recording.
        mAudioRecord.startRecording();
        // start processing the captured audio.
        mThread = new Thread(new ProcessVoice());
        mThread.start();
        Log.d(TAG, "Started");
    }

    /**
     * Stops recording audio.
     */
    public void stop(boolean performDismiss) {
        Log.d(TAG, "Stopping");
        mShouldRun.set(false);
        mLastFrame = new AudioFrame();
        synchronized (mLock) {
            if(performDismiss) {
                dismiss();
            }
            mThread = null;
            mAudioRecord = null;
        }
        Log.d(TAG, "Stopped");
    }

    /**
     * Stops recording audio.
     */
    public void stopAndWait(boolean performDismiss) {
        Log.d(TAG, "Stopping");
        mShouldRun.set(false);
        mLastFrame = new AudioFrame();
        synchronized (mLock) {
            if(performDismiss) {
                dismiss();
            }
            try {
                mThread.join();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            mThread = null;
            mAudioRecord = null;
        }
        Log.d(TAG, "Stopped");
    }

    /**
     * Dismisses the currently ongoing utterance.
     */
    public void dismiss() {
        Log.d(TAG, "Dismissing");
        if(mShouldDetectVoice.get()) {
            if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
                mLastVoiceHeardMillis = Long.MAX_VALUE;
                Log.d(TAG, "Calling onVoiceEnd");
                mOnVoiceListener.onVoiceEnd();
            }
        }
        mLastFrame = new AudioFrame();
        Log.d(TAG, "Dismissed");
    }

    ///
    ///
    ///

    public void pauseAudioFrameReporting() {
        // check to see if voice detection is already running,
        // if so, bail
        if(!mShouldReportFrame.get()) return;

        Log.d(TAG, "Pausing Audio Frame Reporting");
        mShouldReportFrame.set(false);
        Log.d(TAG, "Paused Audio Frame Reporting");
    }

    public void resumeAudioFrameReporting() {
        // check to see if audio frame or higher is already running,
        // if so, bail
        if(mShouldReportFrame.get()) return;

        Log.d(TAG, "Resuming Audio Frame Reporting");
        mShouldReportFrame.set(true);
        Log.d(TAG, "Resumed Audio Frame Reporting");
    }

    ///
    ///
    ///

    public void pauseVoiceDetection() {
        // check to see if voice detection is already running,
        // if so, bail
        if(!mShouldDetectVoice.get()) return;

        Log.d(TAG, "Pausing Voice Detection");

        mShouldDetectVoice.set(false);

        if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
            mLastVoiceHeardMillis = Long.MAX_VALUE;
            Log.d(TAG, "Calling onVoiceEnd");
            mOnVoiceListener.onVoiceEnd();
        }

        Log.d(TAG, "Paused Voice Detection");
    }

    public void resumeVoiceDetection() {
        // check to see if voice detection is already running,
        // if so, bail
        if(mShouldDetectVoice.get()) return;

        Log.d(TAG, "Resuming Voice Detection");

        mLastVoiceHeardMillis = Long.MAX_VALUE;
        mShouldDetectVoice.set(true);

        Log.d(TAG, "Resumed Voice Detection");
    }

    /**
     *
     */
    public boolean isRunning() {
        return mThread != null && mAudioRecord != null && mShouldRun.get();
    }

    /**
     *
     */
    public boolean isAudioFrameReportingPaused() {
        return mThread != null && mAudioRecord != null && !mShouldReportFrame.get();
    }

    /**
     *
     */
    public boolean isVoiceDetectionPaused() {
        return mThread != null && mAudioRecord != null && !mShouldDetectVoice.get();
    }

    /**
     * Retrieves the sample rate currently used to record audio.
     *
     * @return The sample rate of recorded audio.
     */
    public int getSampleRate() {
        return mSampleRate;
    }

    /**
     * Retrieves the calculated size in bytes for the audio buffer
     */
    public int getSizeInBytes() {
        return mSizeInBytes;
    }

    /**
     * Retrieves the calculated lead in bytes buffer size
     */
    public int getLeadInBytes() {
        return mLeadInBytes;
    }

    /**
     * Retrieves the last gathered audio frame amplitude data
     * @return The last gathered audio frame amplitude data
     */
    public AudioFrame getLastFrame() {
        return mLastFrame;
    }

    /**
     * Creates a new {@link AudioRecord}.
     *
     * @return A newly created {@link AudioRecord}, or null if it cannot be created (missing
     * permissions?).
     */
    private AudioRecord createAudioRecord() {
        final AudioRecord audioRecord = new AudioRecord(mSampleSource, mSampleRate,
                CHANNEL, ENCODING, mSizeInBytes);
        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            return audioRecord;
        } else {
            audioRecord.release();
        }
        return null;
    }

    /**
     * Continuously processes the captured audio and notifies {@link #mOnVoiceListener} of corresponding
     * events.
     */
    private class ProcessVoice implements Runnable {

        @Override
        public void run() {
            AudioRecord ar = mAudioRecord;

            int byteSize = 0;
            byte[] byteBuffer = new byte[mSizeInBytes];
            short[] shortBuffer = new short[mSizeInBytes / 2];

            int leadInSize = 0;
            byte[] leadInBuffer = new byte[mLeadInBytes];
            CircularByteBuffer leadInByteBuffer = new CircularByteBuffer(mLeadInBytes);

            Log.d(TAG, "Runner Enter");
            while (mShouldRun.get()) {

                // read run states
                boolean reportFrame = mShouldReportFrame.get();
                boolean detectVoice = mShouldDetectVoice.get();

                synchronized (mLock) {

                    // read from the microphone
                    final int size = ar.read(shortBuffer, 0, shortBuffer.length);

                    if(!reportFrame && !detectVoice)
                        continue;

                    // process the audio
                    byteSize = processAudio(shortBuffer, byteBuffer, size);

                    if(reportFrame) {
                        // report the frame
                        mOnAudioFrameListener.onFrameAvailable(mLastFrame);
                    }

                    if(!detectVoice)
                        continue;

                    final long now = System.currentTimeMillis();
                    if (mLastFrame.isVoiceDetected()) {
                        if (mLastVoiceHeardMillis == Long.MAX_VALUE) {
                            mVoiceStartedMillis = now;
                            Log.d(TAG, "Calling onVoiceStart");
                            mOnVoiceListener.onVoiceStart();
                            if(mLeadInBytes > 0) {
                                leadInSize = leadInByteBuffer.get(leadInBuffer);
                                Log.d(TAG, "Calling (lead-in) onVoice");
                                mOnVoiceListener.onVoice(leadInBuffer, leadInSize);
                                leadInByteBuffer.clear();
                            }
                        }
                        Log.d(TAG, "Calling onVoice");
                        mOnVoiceListener.onVoice(byteBuffer, byteSize);
                        mLastVoiceHeardMillis = now;
                        if (now - mVoiceStartedMillis > mSpeechMaxLengthMillis) {
                            end();
                        }
                    } else if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
                        Log.d(TAG, "Calling onVoice");
                        mOnVoiceListener.onVoice(byteBuffer, byteSize);
                        if (now - mLastVoiceHeardMillis > mSpeechTimeoutMillis) {
                            end();
                        }
                    } else if(mLeadInBytes > 0) {
                        // if we are not in a listening state,
                        // write audio to circular lead-in buffer
                        leadInByteBuffer.put(byteBuffer, 0, byteSize);
                    }
                }
            }
            Log.d(TAG, "Runner Exiting");
            ar.stop();
            ar.release();
            Log.d(TAG, "Runner Exit");
        }

        private void end() {
            mLastVoiceHeardMillis = Long.MAX_VALUE;
            Log.d(TAG, "Calling onVoiceEnd");
            mOnVoiceListener.onVoiceEnd();
        }

        private int processAudio(short[] shorts, byte[] bytes, int size) {

            if(size == 0) return 0;

            int sidx = 0, bidx = 0;
            int nsize = size * 2;
            int hAmp = 0, mAmp = 0, lAmp = Integer.MAX_VALUE;
            boolean isVoiceDetected = false;

            for(; sidx != size; ++sidx) {
                short amp = shorts[sidx];

                // determine voice activity
                mAmp += amp;
                if(amp <= lAmp) lAmp = amp;
                if(amp >= hAmp) hAmp = amp;
                if (!isVoiceDetected &&
                        (amp >= mSilenceAmplitudeThreshold || amp <= -mSilenceAmplitudeThreshold)) {
                    isVoiceDetected = true;
                }

                // convert to bytes
                bytes[bidx]       = (byte)(amp & 0x00ff);
                bytes[bidx + 1]   = (byte)((amp & 0xff00) >> 8);
                bidx += 2;
            }

            mLastFrame.mLow = lAmp;
            mLastFrame.mMean = Math.abs(mAmp / sidx);
            mLastFrame.mHigh = hAmp;
            mLastFrame.mVoiceDetected = isVoiceDetected;

            return nsize;
        }
    }

    ///
    /// OnAudioFrameListener interface
    ///

    public interface OnAudioFrameListener {

        void onFrameAvailable(AudioFrame lastFrame);

    }

    ///
    /// OnVoiceListener interface
    ///

    public interface OnVoiceListener {

        /**
         * Called when the recorder starts hearing voice.
         */
        void onVoiceStart();

        /**
         * Called when the recorder is hearing voice.
         *
         * @param data The audio data in {@link AudioFormat#ENCODING_PCM_16BIT}.
         * @param size The size of the actual data in {@code data}.
         */
        void onVoice(byte[] data, int size);

        /**
         * Called when the recorder stops hearing voice.
         */
        void onVoiceEnd();

    }

    ///
    /// Builder
    ///

    public static class Builder {
        private OnAudioFrameListener mOnAudioFrameListener;
        private OnVoiceListener mOnVoiceListener;
        private int mSilenceAmplitudeThreshold;
        private int mSpeechLeadInMillis;
        private int mSpeechTimeoutMillis;
        private int mSpeechMaxLengthMillis;
        private boolean mUsesNoiseCancellation;

        public Builder() {
            mOnAudioFrameListener = null;
            mOnVoiceListener = null;
            mSilenceAmplitudeThreshold = DEFAULT_SILENCE_THRESHOLD;
            mSpeechLeadInMillis = DEFAULT_SPEECH_LEAD_IN_MILLIS;
            mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;
            mSpeechMaxLengthMillis = DEFAULT_SPEECH_MAX_LENGTH_MILLIS;
            mUsesNoiseCancellation = false;
        }

        public Builder(Context context) {
            mOnAudioFrameListener = null;
            mOnVoiceListener = null;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            mSpeechLeadInMillis = sp.getInt(PREF_LEAD_IN_MILLIS, 0);
            mUsesNoiseCancellation = sp.getBoolean(PREF_NOISE_CANCELLATION, true);
            if(!mUsesNoiseCancellation) {
                mSilenceAmplitudeThreshold = sp.getInt(PREF_SILENCE_THRESHOLD, DEFAULT_SILENCE_THRESHOLD);
            } else {
                mSilenceAmplitudeThreshold = sp.getInt(PREF_SILENCE_THRESHOLD, DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
            }
        }

        public Builder(SharedPreferences preferences) {
            mOnVoiceListener = null;
            mSpeechLeadInMillis = preferences.getInt(PREF_LEAD_IN_MILLIS, 0);
            mUsesNoiseCancellation = preferences.getBoolean(PREF_NOISE_CANCELLATION, true);
            if(!mUsesNoiseCancellation) {
                mSilenceAmplitudeThreshold = preferences.getInt(PREF_SILENCE_THRESHOLD, DEFAULT_SILENCE_THRESHOLD);
            } else {
                mSilenceAmplitudeThreshold = preferences.getInt(PREF_SILENCE_THRESHOLD, DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
            }
        }

        /**
         * Sets the OnVoiceListener
         *
         * @param listener
         * @return Builder
         */
        public Builder setOnAudioFrameListener(OnAudioFrameListener listener) {
            mOnAudioFrameListener = listener;
            return this;
        }

        /**
         * Sets the OnVoiceListener
         *
         * @param listener
         * @return Builder
         */
        public Builder setOnVoiceListener(OnVoiceListener listener) {
            mOnVoiceListener = listener;
            return this;
        }

        /**
         * Set the voice amplitude threshold (silence detection threshold).
         *
         * @param threshold
         * @return Builder
         */
        public Builder setSilenceAmplitudeThreshold(int threshold) {
            mSilenceAmplitudeThreshold = threshold;
            return this;
        }

        /**
         * Set the voice amplitude threshold (silence detection threshold).
         */
        public Builder setSpeechLeadInMillis(int leadInMillis) {
            mSpeechLeadInMillis = leadInMillis;
            return this;
        }

        /**
         * Set the speech timeout in milliseconds (Amount of silence required
         * before recording is stopped).
         */
        public Builder setSpeechTimeoutMillis(int timeoutMillis) {
            mSpeechTimeoutMillis = timeoutMillis;
            return this;
        }

        /**
         * Set the max speech length in milliseconds (Max speech to record before
         * automatically recorder is automatically stopped).
         */
        public Builder setSpeechMaxLengthMillis(int lengthMillis) {
            mSpeechMaxLengthMillis = lengthMillis;
            return this;
        }

        /**
         * Sets whether or not noise cancellation is used
         */
        public Builder setNoiseCancellation(boolean enable) {
            mUsesNoiseCancellation = enable;
            return this;
        }

        /**
         * Builds the VoiceRecorder
         *
         * @return VoiceRecorder
         * @throws Exception if OnVoiceListener is missing
         */
        public VoiceRecorder build() throws Exception {
            if(mOnVoiceListener == null) {
                throw new Exception("Unable to build VoiceRecorder");
            }

            int sampleRate = 0;
            int sizeInBytes = 0;
            int leadInBytes = 0;

            for (int sampleRateCandidate : SAMPLE_RATE_CANDIDATES) {
                sizeInBytes = AudioRecord.getMinBufferSize(sampleRateCandidate, CHANNEL, ENCODING);
                if (sizeInBytes != AudioRecord.ERROR_BAD_VALUE) {
                    sampleRate = sampleRateCandidate;
                    break;
                }
            }

            if(mSpeechLeadInMillis > 0) {
                leadInBytes = (int)((sampleRate * 2) * (((double)mSpeechLeadInMillis) / 1000.0));
            }

            int sampleSource = !mUsesNoiseCancellation ?
                    MediaRecorder.AudioSource.MIC :
                    MediaRecorder.AudioSource.VOICE_RECOGNITION;

            if(mOnAudioFrameListener == null) {
                mOnAudioFrameListener = new NullAudioFrameListener();
            }

            VoiceRecorder voiceRecorder = new VoiceRecorder(
                    mOnAudioFrameListener,
                    mOnVoiceListener,
                    sampleSource,
                    sampleRate,
                    sizeInBytes,
                    leadInBytes
            );

            Log.i(TAG, "Created VoiceRecorder Object: "
                    + "Sample Source: " + sampleSource
                    + ", SampleRate: " + sampleRate
                    + ", SizeInBytes: " + sizeInBytes
                    + ", LeadInBytes: " + leadInBytes
                    + ", SilenceThreshold: " + mSilenceAmplitudeThreshold);

            voiceRecorder.setSilenceAmplitudeThreshold(mSilenceAmplitudeThreshold);
            voiceRecorder.setSpeechTimeoutMillis(mSpeechTimeoutMillis);
            voiceRecorder.setSpeechMaxLengthMillis(mSpeechMaxLengthMillis);

            return voiceRecorder;
        }
    }

    private static class NullAudioFrameListener implements OnAudioFrameListener {
        @Override
        public void onFrameAvailable(AudioFrame lastFrame) {
        }
    }

}
