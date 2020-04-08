package com.validvoice.dynamic.cloud;

public interface ICloudController {

    boolean sendMessage(ICloudMessage message, CloudController.ResponseCallback callback);

    ICloudMessage createCloudMessage();

    ICloudImageLoader createCloudImageLoader();

    void registerCloudObjectFactory(ICloudObject.IFactory factory);

}
