package com.validvoice.voxidem.cloud;

import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.ICloudObject;

public class UserLogin implements ICloudObject {

    private static final String V2W_COMPLETE = "v2w_complete";
    private static final String V2W_SUCCESS = "v2w_success";
    private static final String V2W_MESSAGE = "v2w_message";

    private final boolean mIsComplete;
    private final boolean mIsSuccess;
    private final String mV2message;

    private UserLogin(boolean isComplete, boolean isSuccess, String v2message) {
        mIsComplete = isComplete;
        mIsSuccess = isSuccess;
        mV2message = v2message;
    }

    public final boolean isComplete() {
        return mIsComplete;
    }

    public final boolean isSuccess() {
        return mIsSuccess;
    }

    public final String getV2wMessage(){
        return  mV2message;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "UserLogin";
        }

        @Override
        public Class<?> getClassType() {
            return UserLogin.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            boolean isComplete = false;
            boolean isSuccess = false;
            String v2message = "";
            if (object.has(V2W_COMPLETE) && !object.get(V2W_COMPLETE).isJsonNull()) {
                isComplete = object.get(V2W_COMPLETE).getAsJsonPrimitive().getAsBoolean();
            }
            if (object.has(V2W_SUCCESS) && !object.get(V2W_SUCCESS).isJsonNull()) {
                isSuccess = object.get(V2W_SUCCESS).getAsJsonPrimitive().getAsBoolean();
            }
            if (object.has(V2W_MESSAGE) && !object.get(V2W_MESSAGE).isJsonNull()) {
                v2message = object.get(V2W_MESSAGE).getAsJsonPrimitive().getAsString();
            }
            return new UserLogin(isComplete, isSuccess, v2message);
        }
    }
}
