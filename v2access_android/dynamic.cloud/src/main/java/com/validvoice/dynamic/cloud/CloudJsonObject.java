package com.validvoice.dynamic.cloud;

import com.google.gson.JsonObject;

public class CloudJsonObject implements ICloudObject {

    private final JsonObject mJsonObject;

    CloudJsonObject(JsonObject jsonObject) {
        mJsonObject = jsonObject;
    }

    public final JsonObject getJsonObject() {
        return mJsonObject;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "CloudJsonObject";
        }

        @Override
        public Class<?> getClassType() {
            return CloudJsonObject.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            return new CloudJsonObject(object);
        }
    }

}
