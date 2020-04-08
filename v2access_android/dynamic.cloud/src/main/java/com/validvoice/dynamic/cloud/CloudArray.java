package com.validvoice.dynamic.cloud;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class CloudArray implements ICloudObject {

    private final static String CONTAINS = "contains";
    private final static String LIST = "list";

    private final String mObjectType;
    private final Class<?> mClazz;
    private final List<ICloudObject> mCloudList;

    private CloudArray(String objectType, Class<?> clazz, List<ICloudObject> list) {
        mObjectType = objectType;
        mClazz = clazz;
        mCloudList = list;
    }

    public final String getObjectType() {
        return mObjectType;
    }

    public final Class<?> getObjectClass() {
        return mClazz;
    }

    public final List<ICloudObject> getList() {
        return mCloudList;
    }

    public final int size() {
        return mCloudList.size();
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "CloudArray";
        }

        @Override
        public Class<?> getClassType() {
            return CloudArray.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            String contains = object.get(CONTAINS).getAsString();
            ICloudObject.IFactory factory = parser.findFactory(contains);
            if(factory == null) {
                factory = new CloudJsonObject.Factory();
            }
            List<ICloudObject> list = new ArrayList<>();
            if(object.get(LIST).isJsonArray()) {
                JsonArray a = object.getAsJsonArray(LIST);
                for (int i = 0; i < a.size(); ++i) {
                    list.add(factory.createObject(a.get(i).getAsJsonObject(), parser));
                }
            }
            return new CloudArray(contains, factory.getClassType(), list);
        }
    }

}
