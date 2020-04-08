package com.validvoice.dynamic.audio;

public interface AudioRecorderCallback {

    void onCaptureAudio(byte[] buffer, int length);

}
