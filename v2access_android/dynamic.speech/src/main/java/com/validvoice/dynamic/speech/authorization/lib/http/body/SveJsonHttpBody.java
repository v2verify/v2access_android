package com.validvoice.dynamic.speech.authorization.lib.http.body;

import android.util.Log;

import com.google.gson.Gson;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public class SveJsonHttpBody extends SveHttpBody {

    private static final String TAG = "SveJsonHttpBody";

    private String mData;

    private SveJsonHttpBody(String data) {
        super("application/json", data.length());
        mData = data;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        Log.d(TAG, mData);
        out.write(toAsciiBytes(mData));
    }

    public static SveJsonHttpBody create(HashMap<String, Object> map) {
        return new SveJsonHttpBody(new Gson().toJson(map));
    }

    public static SveJsonHttpBody create(List<Object> list) {
        return new SveJsonHttpBody(new Gson().toJson(list));
    }

    private static byte[] toAsciiBytes(String str) {
        byte[] b = new byte[str.length()];
        for(int i = 0; i < b.length; ++i) {
            b[i] = (byte) str.charAt(i);
        }
        return b;
    }

}
