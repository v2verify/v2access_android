package com.validvoice.dynamic.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import okio.Buffer;

public class AudioPlayer extends Thread {

    public static final int SAMPLING_RATE = 8000;

    private static int bufferSize;

    private Buffer mBuffer;
    private boolean isPlaying = false;
    private Callback callback = null;

    static {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
    }

    public interface Callback {

        void onFinished();

    }

    public static int BufferSize() {
        return bufferSize;
    }

    public AudioPlayer(Buffer buffer, Callback callback) {
        assert(callback != null);   // Ensure we have a callback, otherwise what is the point
        this.callback = callback;
        this.mBuffer = new Buffer();
        buffer.copyTo(this.mBuffer, 0, buffer.size());
    }

    @Override
    public void run() {

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );

        byte[] s = new byte[bufferSize];
        int i = 0;
        audioTrack.play();
        isPlaying = true;

        while(isPlaying && (i = mBuffer.read(s, 0, bufferSize)) > -1) {
            audioTrack.write(s, 0, i);
        }

        audioTrack.stop();
        audioTrack.release();
        callback.onFinished();
    }

    public synchronized void stopPlaying() {
        isPlaying = false;
    }

}
