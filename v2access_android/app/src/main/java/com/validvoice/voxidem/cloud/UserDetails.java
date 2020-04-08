package com.validvoice.voxidem.cloud;

import android.util.Log;

import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.CloudArray;
import com.validvoice.dynamic.cloud.ICloudObject;
import com.validvoice.dynamic.speech.authorization.SveEnroller;

import java.util.Date;

public class UserDetails implements ICloudObject {

    private static final String TAG = "UserDetails";

    private final static String FIRST_NAME = "first_name";
    private final static String LAST_NAME = "last_name";
    private final static String USERNAME = "username";
    private final static String GENDER = "gender";
    private final static String LANGUAGE_CODE = "language_code";
    private final static String PRIMARY_PHONE = "primary_phone";
    private final static String EMAIL_ADDRESS = "email_addr";
    private final static String VOICE_PRINT_ID = "voice_print_id";
    private final static String ACCOUNTS = "accounts";
    private final static String DEVICES = "devices";
    private final static String ENROLLMENT_DATE = "last_enrollment";

    private final String mFirstName;
    private final String mLastName;
    private final String mUsername;
    private final SveEnroller.Gender mGender;
    private final String mLanguageCode;
    private final String mPrimaryPhone;
    private final String mEmailAddress;
    private final String mVoicePrintId;
    private final Long mEnrollmentDate;
    private final CloudArray mAccounts;
    private final CloudArray mDevices;

    private UserDetails(String firstName, String lastName, String userName, SveEnroller.Gender gender,
                        String langCode, String primaryPhone, String emailAddress, String voicePrintId,
                        CloudArray accounts, CloudArray devices,Long enrollmentDate) {
        mFirstName = firstName;
        mLastName = lastName;
        mUsername = userName;
        mGender = gender;
        mLanguageCode = langCode;
        mPrimaryPhone = primaryPhone;
        mEmailAddress = emailAddress;
        mVoicePrintId = voicePrintId;
        mAccounts = accounts;
        mDevices = devices;
        mEnrollmentDate = enrollmentDate;
    }

    public String GetFirstName() {
        return mFirstName;
    }

    public Long GetmEnrollmentDate(){return  mEnrollmentDate;}

    public String GetLastName() {
        return mLastName;
    }

    public String GetUsername() {
        return mUsername;
    }

    public SveEnroller.Gender GetGender() {
        return mGender;
    }

    public String GetLanguageCode() {
        return mLanguageCode;
    }

    public String GetPrimaryPhone() {
        return mPrimaryPhone;
    }

    public String GetEmailAddress() {
        return mEmailAddress;
    }

    public boolean HasVoicePrintId() {
        return mVoicePrintId != null;
    }

    public String GetVoicePrintId() {
        return mVoicePrintId;
    }

    public boolean HasAccounts() {
        return mAccounts != null;
    }

    public CloudArray GetAccounts() {
        return mAccounts;
    }

    public boolean HasDevices() {
        return mDevices != null;
    }

    public CloudArray GetDevices() {
        return mDevices;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "UserDetails";
        }

        @Override
        public Class<?> getClassType() {
            return UserDetails.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            Log.d(TAG, "createObject::::::::::::::::::::::::"+object);
            String first_name = object.get(FIRST_NAME).getAsJsonPrimitive().getAsString();
            String last_name = object.get(LAST_NAME).getAsJsonPrimitive().getAsString();
            String username = object.get(USERNAME).getAsJsonPrimitive().getAsString();
            SveEnroller.Gender gender = SveEnroller.getGender(object.get(GENDER).getAsJsonPrimitive().getAsString());
            String lang_code = object.get(LANGUAGE_CODE).getAsJsonPrimitive().getAsString();
            String phone = object.get(PRIMARY_PHONE).getAsJsonPrimitive().getAsString();
            String email_addr = object.get(EMAIL_ADDRESS).getAsJsonPrimitive().getAsString();
            Long enrollment_date = object.get(ENROLLMENT_DATE).getAsJsonPrimitive().getAsLong();
            String voice_print_id = null;
            CloudArray accounts = null;
            CloudArray devices = null;
            if (object.has(VOICE_PRINT_ID) && !object.get(VOICE_PRINT_ID).isJsonNull()) {
                voice_print_id = object.get(VOICE_PRINT_ID).getAsJsonPrimitive().getAsString();
            }
            if(object.has(ACCOUNTS) && !object.get(ACCOUNTS).isJsonNull()) {
                accounts = (CloudArray)new CloudArray.Factory().createObject(
                        object.get(ACCOUNTS).getAsJsonObject(),
                        parser
                );
            }
            if(object.has(DEVICES) && !object.get(DEVICES).isJsonNull()) {
                devices = (CloudArray)new CloudArray.Factory().createObject(
                        object.get(DEVICES).getAsJsonObject(),
                        parser
                );
            }
            return new UserDetails(first_name, last_name, username, gender, lang_code, phone,
                    email_addr, voice_print_id, accounts, devices,enrollment_date);
        }
    }

}
