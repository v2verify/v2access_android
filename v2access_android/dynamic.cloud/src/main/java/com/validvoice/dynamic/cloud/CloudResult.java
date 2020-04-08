package com.validvoice.dynamic.cloud;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static android.content.ContentValues.TAG;

public class CloudResult implements ICloudObject {

    private static final String CODE = "code";
    private static final String RESULT = "result";

    private final int mCode;
    private final ICloudObject mData;

    private CloudResult(int code, ICloudObject data) {
        mCode = code;
        mData = data;
    }

    public int getCode() {
        return mCode;
    }

    public boolean hasData() {
        return mData != null;
    }

    public ICloudObject getData() {
        return mData;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "CloudResult";
        }

        @Override
        public Class<?> getClassType() {
            return CloudResult.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {

            Log.d(TAG, "createObject::::::::::::::::::::object"+object);
            Log.d(TAG, "createObject::::::::::::::parser:::::::::::::::::"+parser.getClass());
            int c = object.get(CODE).getAsJsonPrimitive().getAsInt();
            JsonElement e = object.get(RESULT);

            Log.d(TAG, "createObject:;;;;;;;;;;;;;;;;;;;;elemental;;;;;;;;;;;;;;;;;;;;"+e);
            if(e != null && e.isJsonObject()) {
                return new CloudResult(c, parser.parseObject(e.getAsJsonObject()));
            }
            return new CloudResult(c, null);
        }
    }
}
