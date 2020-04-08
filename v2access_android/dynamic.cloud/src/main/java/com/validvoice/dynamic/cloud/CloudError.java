package com.validvoice.dynamic.cloud;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CloudError implements ICloudObject {

    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String DATA = "data";

    private final int mCode;
    private final String mMessage;
    private final ICloudObject mData;

    private CloudError(int code, String message, ICloudObject data) {
        mCode = code;
        mMessage = message;
        mData = data;
    }

    public int getCode() {
        return mCode;
    }

    public String getMessage() {
        return mMessage;
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
            return "CloudError";
        }

        @Override
        public Class<?> getClassType() {
            return CloudError.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            int c = object.get(CODE).getAsJsonPrimitive().getAsInt();
            String m = "";
            if(object.has(MESSAGE) && !object.get(MESSAGE).isJsonNull()) {
                m = object.get(MESSAGE).getAsJsonPrimitive().getAsString();
            }
            JsonElement e = object.get(DATA);
            if(e != null && e.isJsonObject()) {
                return new CloudError(c, m, parser.parseObject(e.getAsJsonObject()));
            }
            return new CloudError(c, m, null);
        }
    }
}
