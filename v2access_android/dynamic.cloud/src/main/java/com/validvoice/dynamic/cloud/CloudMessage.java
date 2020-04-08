package com.validvoice.dynamic.cloud;

import android.content.Context;

public class CloudMessage implements ICloudObject {

    public enum MethodType {
        UNKNOWN,

        GET,
        LIST,

        POST,
        CREATE,
        VALIDATE,

        PUT,
        UPDATE,

        DELETE
    }

    ICloudMessage mCloudMessage;

    CloudMessage(ICloudMessage message) {
        mCloudMessage = message;
    }

    private CloudMessage(CloudController.Version version, String service, MethodType method) {
        mCloudMessage = CloudController.createCloudMessage();
        mCloudMessage.setVersion(version);
        mCloudMessage.setService(service);
        mCloudMessage.setMethod(method);
    }

    public CloudMessage() {
        mCloudMessage = CloudController.createCloudMessage();
    }

    public void setVersion(CloudController.Version version) {
        mCloudMessage.setVersion(version);
    }

    public CloudController.Version getVersion() {
        return mCloudMessage.getVersion();
    }

    public void setService(String service) {
        mCloudMessage.setService(service);
    }

    public String getService() {
        return mCloudMessage.getService();
    }

    public void setMethod(MethodType method) {
        mCloudMessage.setMethod(method);
    }

    public MethodType getMethod() {
        return mCloudMessage.getMethod();
    }

    public void setStatus(int status) {
        mCloudMessage.setStatus(status);
    }

    public int getStatus() {
        return mCloudMessage.getStatus();
    }

    public void setStatusDescription(String description) {
        mCloudMessage.setStatusDescription(description);
    }

    public String getStatusDescription() {
        return mCloudMessage.getStatusDescription();
    }

    public void putString(String key, String value) {
        mCloudMessage.putString(key, value);
    }

    public void putInt(String key, int value) {
        mCloudMessage.putInt(key, value);
    }

    public void putLong(String key, long value) {
        mCloudMessage.putLong(key, value);
    }

    public void putFloat(String key, float value) {
        mCloudMessage.putFloat(key, value);
    }

    public void putDouble(String key, double value) {
        mCloudMessage.putDouble(key, value);
    }

    public void putBoolean(String key, boolean value) {
        mCloudMessage.putBoolean(key, value);
    }

    public void putObject(String key, Object value) {
        mCloudMessage.putObject(key, value);
    }

    public String getString(String key) {
        return mCloudMessage.getString(key);
    }

    public int getInt(String key) {
        return mCloudMessage.getInt(key);
    }

    public long getLong(String key) {
        return mCloudMessage.getLong(key);
    }

    public float getFloat(String key) {
        return mCloudMessage.getFloat(key);
    }

    public double getDouble(String key) {
        return mCloudMessage.getDouble(key);
    }

    public boolean getBoolean(String key) {
        return mCloudMessage.getBoolean(key);
    }

    public Object getObject(String key) {
        return mCloudMessage.getObject(key);
    }

    public boolean containsKey(String key) {
        return mCloudMessage.containsKey(key);
    }

    public void removeKey(String key) {
        mCloudMessage.removeKey(key);
    }

    public boolean send(CloudController.ResponseCallback callback) {
        return CloudController.sendMessage(this, callback);
    }

    public boolean send(Context context, CloudController.ResponseOnUiCallback callback) {
        return CloudController.sendMessage(this, context, callback);
    }

    public static CloudMessage Get(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.GET);
    }

    public static CloudMessage Get(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.GET);
    }

    public static CloudMessage List(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.LIST);
    }

    public static CloudMessage List(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.LIST);
    }

    public static CloudMessage Post(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.POST);
    }

    public static CloudMessage Post(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.POST);
    }

    public static CloudMessage Create(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.CREATE);
    }

    public static CloudMessage Create(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.CREATE);
    }

    public static CloudMessage Validate(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.VALIDATE);
    }

    public static CloudMessage Validate(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.VALIDATE);
    }

    public static CloudMessage Put(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.PUT);
    }

    public static CloudMessage Put(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.PUT);
    }

    public static CloudMessage Update(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.UPDATE);
    }

    public static CloudMessage Update(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.UPDATE);
    }

    public static CloudMessage Delete(String service) {
        return new CloudMessage(CloudController.defaultVersion(), service, MethodType.DELETE);
    }

    public static CloudMessage Delete(String service, CloudController.Version version) {
        return new CloudMessage(version, service, MethodType.DELETE);
    }

}
