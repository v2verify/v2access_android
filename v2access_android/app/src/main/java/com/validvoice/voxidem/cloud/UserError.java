package com.validvoice.voxidem.cloud;

import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.ICloudObject;

public class UserError implements ICloudObject {

    private static final String V2W_USER_NAME_ERROR = "v2w_user_name_error";
    private static final String V2W_EMAIL_ADDRESS_ERROR = "v2w_email_address_error";
    private static final String V2W_PHONE_NUMBER_ERROR = "v2w_phone_number_error";

    private final boolean mNameError;
    private final boolean mAddressError;
    private final boolean mPhoneError;

    private UserError(boolean nameError, boolean addressError, boolean phoneError) {
        mNameError = nameError;
        mAddressError = addressError;
        mPhoneError = phoneError;
    }

    public final boolean isNameError() {
        return mNameError;
    }

    public final boolean isEmailAddressError() {
        return mAddressError;
    }

    public final boolean isPhoneError() {
        return mPhoneError;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "UserError";
        }

        @Override
        public Class<?> getClassType() {
            return UserError.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            boolean nameError = false;
            boolean addrError = false;
            boolean phoneError = false;
            if (object.has(V2W_USER_NAME_ERROR) && !object.get(V2W_USER_NAME_ERROR).isJsonNull()) {
                nameError = object.get(V2W_USER_NAME_ERROR).getAsJsonPrimitive().getAsBoolean();
            }
            if (object.has(V2W_EMAIL_ADDRESS_ERROR) && !object.get(V2W_EMAIL_ADDRESS_ERROR).isJsonNull()) {
                addrError = object.get(V2W_EMAIL_ADDRESS_ERROR).getAsJsonPrimitive().getAsBoolean();
            }
            if (object.has(V2W_PHONE_NUMBER_ERROR) && !object.get(V2W_PHONE_NUMBER_ERROR).isJsonNull()) {
                phoneError = object.get(V2W_PHONE_NUMBER_ERROR).getAsJsonPrimitive().getAsBoolean();
            }
            return new UserError(nameError, addrError, phoneError);
        }
    }
}
