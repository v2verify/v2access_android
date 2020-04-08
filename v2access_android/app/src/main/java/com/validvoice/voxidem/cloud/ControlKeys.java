package com.validvoice.voxidem.cloud;


import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.ICloudObject;


public class ControlKeys implements ICloudObject {
    private static final String TAG = "CompanyInfo";

    private static final String V2W_APPLICATION_KEY = "application_key";
    private static final String V2W_DEVELOPER_KEY = "developer_key";

    private final String applicationKey;
    private final String developmentKey;

    private ControlKeys(String appKey, String devKey) {
        applicationKey = appKey;
        developmentKey = devKey;
    }


    public String getApplicationKey() {
        return applicationKey;
    }

    public String getDevelopmentKey(){return  developmentKey;}




    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "ControlKeys";
        }

        @Override
        public Class<?> getClassType() {
            return ControlKeys.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {

            String applicationKey = "";
            String developmentKey = "";
            if (object.has(V2W_APPLICATION_KEY) && !object.get(V2W_APPLICATION_KEY).isJsonNull()) {
                applicationKey = object.get(V2W_APPLICATION_KEY).getAsJsonPrimitive().getAsString();
            }
            if (object.has(V2W_DEVELOPER_KEY) && !object.get(V2W_DEVELOPER_KEY).isJsonNull()) {
                developmentKey = object.get(V2W_DEVELOPER_KEY).getAsJsonPrimitive().getAsString();
            }

            return new ControlKeys(applicationKey, developmentKey);
        }
    }
}
