package com.validvoice.voxidem.cloud;

import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.ICloudObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UserIntent implements ICloudObject {

    private static final String V2W_INTENT_ID = "v2w_intent_id";
    private static final String V2W_EXPIRE_TIME = "v2w_expire_time";
    private static final String V2W_COMPANY = "v2w_company";
    private static final String V2W_COMPANY_LOGO = "v2w_company_logo";
    private static final String V2W_MAX_ATTEMPTS = "v2w_max_attempts";
    private static final String V2W_NICK_NAME = "v2w_nick_name";
    private static final String V2W_DEVICE_INFO = "v2w_device_info";
    private static final String V2W_DEVICE_IP = "v2w_device_ip";
    private static final String V2W_COMPANY_USER = "v2w_cmp_user";
    private static final String V2W_COMPANY_USER_RAW_ID = "v2w_cmp_user_raw_id";

    private final String mIntentId;
    private final String mCompany;
    private final String mCompanyLogo;
    private final String mCompanyUser;
    private final int mCompanyUserRawId;
    private final int mMaxAttempts;
    private final String mDeviceNickName;
    private final String mDeviceInfo;
    private final String mDeviceIp;
    private final Calendar mExpireTime;

    private boolean mIsSelected;
    private boolean mIsDisabled;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss", new Locale("en-US"));

    public String GetIntentId() {
        return mIntentId;
    }

    public String GetCompany() {
        return mCompany;
    }

    public String GetCompanyLogo() {
        return mCompanyLogo;
    }

    public String GetCompanyUser() {
        return mCompanyUser;
    }

    public int GetCompanyUserAccountId() {
        return mCompanyUserRawId;
    }

    public int GetMaxAttempts() {
        return mMaxAttempts;
    }

    public String GetDeviceNickName() {
        return mDeviceNickName;
    }

    public String GetDeviceDisplay() {
        return "Device Name:\n " + mDeviceNickName ;

    }

    public String GetDeviceIp() {
        return mDeviceIp;
    }

    public void SetIsSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public boolean GetIsSelected() {
        return mIsSelected;
    }

    public void SetIsDisabled(boolean isDisabled) {
        mIsDisabled = isDisabled;
    }

    public boolean GetIsDisabled() {
        return mIsDisabled;
    }

    public boolean IsActive() {
        return Calendar.getInstance().getTimeInMillis() < mExpireTime.getTimeInMillis();
    }

    public String GetTimeLeft() {
        if(IsActive()) {
            Calendar time = Calendar.getInstance();
            time.setTimeInMillis(mExpireTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
            return mDateFormat.format(time.getTime());
        }
        return "Expired";
    }

    private UserIntent(int id, String a, String b, String c, String d, String e, String f, String g, long h, int i) {
        mCompanyUserRawId = id;
        mIntentId = a;
        mCompany = b;
        mCompanyLogo = c;
        mCompanyUser = d;
        mMaxAttempts = i;
        mDeviceNickName = e;
        mDeviceInfo = f;
        mDeviceIp = g;
        mExpireTime = Calendar.getInstance();
        mExpireTime.setTimeInMillis(h * 1000);
        mIsSelected = false;
        mIsDisabled = false;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "UserIntent";
        }

        @Override
        public Class<?> getClassType() {
            return UserIntent.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            int id = object.get(V2W_COMPANY_USER_RAW_ID).getAsJsonPrimitive().getAsInt();
            String a = object.get(V2W_INTENT_ID).getAsJsonPrimitive().getAsString();
            String b = object.get(V2W_COMPANY).getAsJsonPrimitive().getAsString();
            String c = object.get(V2W_COMPANY_LOGO).getAsJsonPrimitive().getAsString();
            String d = object.get(V2W_COMPANY_USER).getAsJsonPrimitive().getAsString();
            String e = object.get(V2W_NICK_NAME).getAsJsonPrimitive().getAsString();
            String f = object.get(V2W_DEVICE_INFO).getAsJsonPrimitive().getAsString();
            String g = object.get(V2W_DEVICE_IP).getAsJsonPrimitive().getAsString();
            long h = object.get(V2W_EXPIRE_TIME).getAsJsonPrimitive().getAsLong();
            int i = object.get(V2W_MAX_ATTEMPTS).getAsJsonPrimitive().getAsInt();
            return new UserIntent(id, a, b, c, d, e, f, g, h, i);
        }
    }
}
