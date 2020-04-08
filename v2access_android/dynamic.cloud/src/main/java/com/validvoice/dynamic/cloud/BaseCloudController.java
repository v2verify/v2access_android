package com.validvoice.dynamic.cloud;

import com.google.gson.JsonObject;

import java.util.HashMap;

public abstract class BaseCloudController implements ICloudController, ICloudObject.IFactoryParser {

    private HashMap<String, ICloudObject.IFactory> mCloudFactories = new HashMap<>();

    @Override
    public final void registerCloudObjectFactory(ICloudObject.IFactory factory) {
        mCloudFactories.put(factory.getObjectId(), factory);
    }

    @Override
    public final ICloudObject.IFactory findFactory(String name) {
        return mCloudFactories.get(name);
    }

    @Override
    public final ICloudObject parseObject(JsonObject o) {
        return parseCloudObject(o);
    }

    final protected ICloudObject parseCloudObject(JsonObject o) {
        if(o.has("__type")) {
            String cloudObjectId = o.get("__type").getAsJsonPrimitive().getAsString();
            if(mCloudFactories.containsKey(cloudObjectId)) {
                ICloudObject.IFactory factory = mCloudFactories.get(cloudObjectId);
                if(factory != null) {
                    try {
                        return factory.createObject(o, this);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return new CloudJsonObject(o);
    }
}
