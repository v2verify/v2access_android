package com.validvoice.dynamic.cloud;

import com.google.gson.JsonObject;

public interface ICloudObject {

    interface IFactoryParser {
        IFactory findFactory(String name);
        ICloudObject parseObject(JsonObject object);
    }

    interface IFactory {
        String getObjectId();
        Class<?> getClassType();
        ICloudObject createObject(JsonObject object, IFactoryParser parser);
    }

}
