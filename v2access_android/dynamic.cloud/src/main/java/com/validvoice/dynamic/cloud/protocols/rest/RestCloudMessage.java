package com.validvoice.dynamic.cloud.protocols.rest;

import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveJsonHttpBody;
import com.validvoice.dynamic.cloud.ICloudMessage;

import java.util.HashMap;

class RestCloudMessage implements ICloudMessage {

    private CloudController.Version mVersion = CloudController.Version.Unknown;
    private String mService = "";
    private CloudMessage.MethodType mMethod = CloudMessage.MethodType.UNKNOWN;
    private int mStatus = 200;
    private String mStatusDescription = "";
    private HashMap<String, Object> mRestData = new HashMap<>();

    @Override
    public void setVersion(CloudController.Version version) {
        mVersion = version;
    }

    @Override
    public CloudController.Version getVersion() {
        return mVersion;
    }

    @Override
    public void setService(String service) {
        mService = service;
    }

    @Override
    public String getService() {
        return mService;
    }

    @Override
    public void setMethod(CloudMessage.MethodType method) {
        mMethod = method;
    }

    @Override
    public CloudMessage.MethodType getMethod() {
        return mMethod;
    }

    @Override
    public void setStatus(int status) {
        mStatus = status;
    }

    @Override
    public int getStatus() {
        return mStatus;
    }

    @Override
    public void setStatusDescription(String description) {
        mStatusDescription = description;
    }

    @Override
    public String getStatusDescription() {
        return mStatusDescription;
    }

    @Override
    public void putString(String key, String value) {
        mRestData.put(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        mRestData.put(key, value);
    }

    @Override
    public void putLong(String key, long value) {
        mRestData.put(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        mRestData.put(key, value);
    }

    @Override
    public void putDouble(String key, double value) {
        mRestData.put(key, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        mRestData.put(key, value);
    }

    @Override
    public void putObject(String key, Object value) {
        mRestData.put(key, value);
    }

    @Override
    public String getString(String key) {
        if(mRestData.containsKey(key)) {
            Object o = mRestData.get(key);
            if(o instanceof String) {
                return (String)o;
            } else if(o instanceof Integer || o instanceof Long ||
                    o instanceof Float || o instanceof Double) {
                return o.toString();
            } else if(o instanceof Boolean) {
                return ((Boolean)o) ? "true" : "false";
            }
        }
        return null;
    }

    @Override
    public int getInt(String key) {
        if(mRestData.containsKey(key)) {
            Object o = mRestData.get(key);
            if(o instanceof Integer) {
                return (int)o;
            }
        }
        return 0;
    }

    @Override
    public long getLong(String key) {
        if(mRestData.containsKey(key)) {
            Object o = mRestData.get(key);
            if(o instanceof Long) {
                return (int)o;
            }
        }
        return 0;
    }

    @Override
    public float getFloat(String key) {
        if(mRestData.containsKey(key)) {
            Object o = mRestData.get(key);
            if(o instanceof Float) {
                return (Float)o;
            }
        }
        return 0.0f;
    }

    @Override
    public double getDouble(String key) {
        if(mRestData.containsKey(key)) {
            Object o = mRestData.get(key);
            if(o instanceof Double) {
                return (Double)o;
            }
        }
        return 0.0;
    }

    @Override
    public boolean getBoolean(String key) {
        if(mRestData.containsKey(key)) {
            Object o = mRestData.get(key);
            if(o instanceof Boolean) {
                return (Boolean)o;
            }
        }
        return false;
    }

    @Override
    public Object getObject(String key) {
        if(mRestData.containsKey(key)) {
            return mRestData.get(key);
        }
        return null;
    }

    @Override
    public boolean containsKey(String key) {
        return mRestData.containsKey(key);
    }

    @Override
    public void removeKey(String key) {
        mRestData.remove(key);
    }

    String getRequestUrl() {
        String service = resolveService();
        if(mVersion == CloudController.Version.Unknown) {
            return "cloud/" + service;
        }
        return mVersion.ordinal() + "/cloud/" + service;
    }

    SveHttpBody getRequestBody() {
        return SveJsonHttpBody.create(mRestData);
    }

    private class KeyData {
        String key;
        String def;
        boolean keep;
    }

    private String resolveService() {
        String service = mService.replace('.', '/');
        if(service.length() > 0 && service.contains("{") && service.contains("}")) {
            StringBuilder builder = new StringBuilder();
            int lastOf = 0;
            while(lastOf != -1) {
                int firstOf = service.indexOf('{', lastOf);
                if(firstOf == -1) {
                    builder.append(service.substring(lastOf));
                    lastOf = -1;
                    continue;
                }
                int secondOf = service.indexOf('}', firstOf + 1);
                if(secondOf == -1) {
                    lastOf = -1;
                    continue;
                }
                builder.append(service.substring(lastOf, firstOf));

                KeyData keyData = new KeyData();
                keyData.key = service.substring(firstOf + 1, secondOf);
                keyData = resolveKeyData(keyData);

                Object value = getObject(keyData.key);
                if(value == null) {
                    value = keyData.def;
                } else if(!keyData.keep) {
                    removeKey(keyData.key);
                }
                builder.append(value);
                lastOf = (secondOf == service.length()) ? -1 : secondOf + 1;
            }
            service = builder.toString();
        }
        return service;
    }

    private KeyData resolveKeyData(KeyData keyData) {
        String key = keyData.key;
        if(key.length() > 0) {
            if(key.startsWith("@")) {
                keyData.keep = true;
                keyData.key = key = key.substring(1);
            }
            if(key.contains("|")) {
                String[] parts = key.split("|");
                if(parts.length == 1) {
                    keyData.key = parts[0];
                    keyData.def = "";
                } else {
                    keyData.key = parts[0];
                    keyData.def = parts[1];
                }
            } else {
                keyData.key = key;
                keyData.def = "";
            }
        }
        return keyData;
    }

}
