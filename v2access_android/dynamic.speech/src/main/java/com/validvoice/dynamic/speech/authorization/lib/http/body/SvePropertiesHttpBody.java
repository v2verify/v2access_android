package com.validvoice.dynamic.speech.authorization.lib.http.body;

import android.util.Log;

import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SvePropertiesHttpBody extends SveHttpBody {

    private static final String TAG = "SvePropertiesHttpBody";

    private String mData;

    private SvePropertiesHttpBody(String data) {
        super("application/properties", data.length());
        mData = data;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        Log.d(TAG, mData);
        out.write(toAsciiBytes(mData));
    }

    public static SvePropertiesHttpBody create(HashMap<String, String> map) {
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<String, String> entry : map.entrySet()) {
            builder.append(entry.getKey()).append(": ")
                    .append(entry.getValue()).append("\r\n");
        }
        return new SvePropertiesHttpBody(builder.toString());
    }

    private static byte[] toAsciiBytes(String str) {
        byte[] b = new byte[str.length()];
        for(int i = 0; i < b.length; ++i) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

}
