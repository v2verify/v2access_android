package com.validvoice.dynamic.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class AudioRecorder extends Thread {

    private boolean isRecording = true;
    private AudioRecorderCallback callback = null;

    public AudioRecorder(AudioRecorderCallback callback) {
        assert(callback != null);   // Ensure we have a callback, otherwise what is the point
        this.callback = callback;
    }

    @Override
    public void run() {

        int bufferSize = 0;
        int sampleRate = getValidMinimumSampleRate();
        AudioRecord record = null;

        Log.i(AudioRecorder.class.getName(), "Minimum Sample Rate: " + sampleRate);

        assertTrue(-1 != sampleRate);

        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        try {
            bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            record = new AudioRecord(
                        AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize
                );
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }

        //if(sampleRate != 8000) {
            // we need to at this point figure out how to resample to 8000
        //}

        byte[] audioBuffer = new byte[bufferSize];
        record.startRecording();

        int read = 0;
        while (!isInterrupted() && isRecording) {
            read = record.read(audioBuffer, 0, bufferSize);

            if ((read == AudioRecord.ERROR_INVALID_OPERATION) ||
                    (read == AudioRecord.ERROR_BAD_VALUE) ||
                    (read <= 0)) {
                continue;
            }

            callback.onCaptureAudio(audioBuffer, read);
        }

        record.stop();
        record.release();
    }

    public synchronized void stopRecording() {
        isRecording = false;
    }

    private static int getValidMinimumSampleRate() {
        for (int rate : new int[] {8000, 11025, 16000, 22050, 44100, 48000}) {
            int bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (bufferSize > 0) {
                return rate;
            }
        }
        return -1;
    }

    private void assertTrue(boolean condition) {
        if(!condition) {
            throw new AssertionError();
        }
    }

}
