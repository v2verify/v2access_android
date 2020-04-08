package com.validvoice.voxidem.cloud;

import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.CloudArray;
import com.validvoice.dynamic.cloud.ICloudObject;

import java.security.InvalidParameterException;

public class QrIntent implements ICloudObject {

    private static final String V2W_DEVICE_ID = "v2w_device_id";
    private static final String V2W_DEVICE_IP = "v2w_device_ip";
    private static final String V2W_DEVICE_INFO = "v2w_device_info";
    private static final String V2W_ACCOUNT = "v2w_account";
    private static final String V2W_ACCOUNTS = "v2w_accounts";
    private static final String V2W_COMPANY = "v2w_company";
    private static final String V2W_MAX_ATTEMPTS = "v2w_max_attempts";

    private final String mDeviceId;
    private final String mDeviceIp;
    private final String mDeviceInfo;
    private final String mCompany;
    private final int mMaxAttempts;
    private final QrUserAccount mAccount;
    private final CloudArray mAccounts;

    private QrIntent(String deviceId, String deviceIp, String deviceInfo, String company, int maxAttempts, CloudArray accounts) {
        mDeviceId = deviceId;
        mDeviceIp = deviceIp;
        mDeviceInfo = deviceInfo;
        mCompany = company;
        mMaxAttempts = maxAttempts;
        mAccount = null;
        mAccounts = accounts;
    }

    private QrIntent(String deviceId, String deviceIp, String deviceInfo, String company, int maxAttempts, QrUserAccount account) {
        mDeviceId = deviceId;
        mDeviceIp = deviceIp;
        mDeviceInfo = deviceInfo;
        mCompany = company;
        mMaxAttempts = maxAttempts;
        mAccount = account;
        mAccounts = null;
    }

    public final boolean hasDeviceId() {
        return mDeviceId != null;
    }

    public final String getDeviceId() {
        return mDeviceId;
    }

    public final String getDeviceIp() {
        return mDeviceIp;
    }

    public final String getDeviceInfo() {
        return mDeviceInfo;
    }

    public final boolean hasCompany() {
        return mCompany != null;
    }

    public final String getCompany() {
        return mCompany;
    }

    public final int getMaxAttempts() {
        return mMaxAttempts;
    }

    public final boolean hasAccount() {
        return mAccount != null;
    }

    public final QrUserAccount getAccount() {
        return mAccount;
    }

    public final boolean hasAccounts() {
        return mAccounts != null && mAccounts.size() > 0;
    }

    public final CloudArray getAccounts() {
        return mAccounts;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "QrIntent";
        }

        @Override
        public Class<?> getClassType() {
            return QrIntent.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            String device_id = null;
            String device_ip = null;
            String device_info = null;
            String company = null;
            int max_attempts = 3;
            if(object.has(V2W_DEVICE_ID) && !object.get(V2W_DEVICE_ID).isJsonNull()) {
                device_id = object.get(V2W_DEVICE_ID).getAsJsonPrimitive().getAsString();
            }
            if(object.has(V2W_DEVICE_IP) && !object.get(V2W_DEVICE_IP).isJsonNull()) {
                device_ip = object.get(V2W_DEVICE_IP).getAsJsonPrimitive().getAsString();
            }
            if(object.has(V2W_DEVICE_INFO) && !object.get(V2W_DEVICE_INFO).isJsonNull()) {
                device_info = object.get(V2W_DEVICE_INFO).getAsJsonPrimitive().getAsString();
            }
            if(object.has(V2W_COMPANY) && !object.get(V2W_COMPANY).isJsonNull()) {
                company = object.get(V2W_COMPANY).getAsJsonPrimitive().getAsString();
            }
            if(object.has(V2W_MAX_ATTEMPTS) && !object.get(V2W_MAX_ATTEMPTS).isJsonNull()) {
                max_attempts = object.get(V2W_MAX_ATTEMPTS).getAsJsonPrimitive().getAsInt();
            }
            if(object.has(V2W_ACCOUNTS) && !object.get(V2W_ACCOUNTS).isJsonNull()) {
                CloudArray accounts = (CloudArray)new CloudArray.Factory().createObject(
                        object.get(V2W_ACCOUNTS).getAsJsonObject(),
                        parser
                );
                return new QrIntent(device_id, device_ip, device_info, company, max_attempts, accounts);
            } else if(object.has(V2W_ACCOUNT) && !object.get(V2W_ACCOUNT).isJsonNull()) {
                QrUserAccount account = (QrUserAccount)new QrUserAccount.Factory().createObject(
                        object.get(V2W_ACCOUNT).getAsJsonObject(),
                        parser
                );
                return new QrIntent(device_id, device_ip, device_info, company, max_attempts, account);
            }
            throw new InvalidParameterException("Missing either 'v2w_accounts' or 'v2w_account'");
        }
    }
}
