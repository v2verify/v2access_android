package com.validvoice.dynamic.speech.recognition;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.validvoice.dynamic.audio.AudioBuffer;
import com.validvoice.dynamic.audio.wave.WaveReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class SpeechAudio {

    public static class Builder {

        private byte[] mBytes;

        public Builder() {
            mBytes = null;
        }

        public Builder setAudio(File file) {
            byte[] bytes = GetRawAudioFromFile(file);
            if(bytes != null) {
                mBytes = bytes;
            }
            return this;
        }

        public Builder setAudio(AudioBuffer buffer) {
            byte[] bytes = buffer.getFullBuffer();
            if(bytes != null) {
                mBytes = bytes;
            }
            return this;
        }

        public Builder setAudio(@NonNull byte[] bytes) {
            mBytes = bytes;
            return this;
        }

        public byte[] getAudio() {
            return mBytes;
        }

        public SpeechAudio build() throws Exception {
            if(mBytes == null) {
                throw new Exception("SpeechAudio MUST have audio data to build");
            }

            SpeechAudio audio = new SpeechAudio();
            audio.mAudioData = Base64.encodeToString(mBytes, Base64.DEFAULT);
            return audio;
        }
    }

    private String mAudioData;

    public String getAudio() {
        return mAudioData;
    }

    public HashMap<String, Object> getSpeechElements() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("content", mAudioData);
        return data;
    }

    private static byte[] GetRawAudioFromFile(@NonNull File file) {
        WaveReader reader = new WaveReader(file);
        try {
            reader.openWave();
            byte[] bytes = new byte[reader.getDataSize()];
            if(-1 == reader.read(bytes, bytes.length)) {
                return null;
            }
            reader.closeWaveFile();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
