package com.validvoice.dynamic.cloud;

public interface ICloudMessage extends ICloudObject {

    void setVersion(CloudController.Version version);
    CloudController.Version getVersion();

    void setService(String service);
    String getService();

    void setMethod(CloudMessage.MethodType method);
    CloudMessage.MethodType getMethod();

    void setStatus(int status);
    int getStatus();

    void setStatusDescription(String description);
    String getStatusDescription();

    void putString(String key, String value);
    void putInt(String key, int value);
    void putLong(String key, long value);
    void putFloat(String key, float value);
    void putDouble(String key, double value);
    void putBoolean(String key, boolean value);
    void putObject(String key, Object value);

    String getString(String key);
    int getInt(String key);
    long getLong(String key);
    float getFloat(String key);
    double getDouble(String key);
    boolean getBoolean(String key);
    Object getObject(String key);

    boolean containsKey(String key);

    void removeKey(String key);

}
