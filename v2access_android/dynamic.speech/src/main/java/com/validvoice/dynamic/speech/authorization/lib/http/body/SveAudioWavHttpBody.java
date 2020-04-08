package com.validvoice.dynamic.speech.authorization.lib.http.body;

import android.support.annotation.NonNull;

import com.validvoice.dynamic.audio.wave.WaveReader;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public class SveAudioWavHttpBody extends SveHttpBody {

    private byte[] mData;

    private SveAudioWavHttpBody(byte[] data) {
        super("audio/wav", data.length);
        mData = data;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(mData);
    }

    public static SveAudioWavHttpBody create(@NonNull byte[] audioBytes) {
        return new SveAudioWavHttpBody(audioBytes);
    }

    public static SveAudioWavHttpBody create(@NonNull File file) {
        byte[] data = GetRawAudioFromFile(file);
        if(data != null) {
            return new SveAudioWavHttpBody(data);
        }
        return null;
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
