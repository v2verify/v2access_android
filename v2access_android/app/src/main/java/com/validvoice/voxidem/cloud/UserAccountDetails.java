package com.validvoice.voxidem.cloud;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.ICloudObject;
import com.validvoice.voxidem.db.contracts.AccountsContract;
import com.validvoice.voxidem.db.models.AccountModel;

import java.util.Calendar;

public class UserAccountDetails implements ICloudObject {

    private final AccountModel mAccountModel;

    private UserAccountDetails(int a, String b, String c, int d, String e, long f, long g, String h) {
        Calendar linkDate = Calendar.getInstance();
        linkDate.setTimeInMillis(f * 1000);
        Calendar lastUsed = Calendar.getInstance();
        lastUsed.setTimeInMillis(g * 1000);
        mAccountModel = AccountModel.createRecord(a, b, c, d, e, linkDate, lastUsed, h);
    }

    public AccountModel getAccountModel() {
        return mAccountModel;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "UserAccountDetails";
        }

        @Override
        public Class<?> getClassType() {
            return UserAccountDetails.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            int a = object.get(AccountsContract.COMPANY_ID).getAsJsonPrimitive().getAsInt();
            String b = object.get(AccountsContract.COMPANY_NAME).getAsJsonPrimitive().getAsString();
            String c = object.get(AccountsContract.COMPANY_LOGO).getAsJsonPrimitive().getAsString();
            int d = object.get(AccountsContract.ACCOUNT_ID).getAsJsonPrimitive().getAsInt();
            String e = object.get(AccountsContract.ACCOUNT_NAME).getAsJsonPrimitive().getAsString();
            long f = object.get(AccountsContract.ACCOUNT_DATE_LINKED).getAsJsonPrimitive().getAsLong();
            long g = 0;
            if(object.has(AccountsContract.ACCOUNT_DATE_LAST_SIGNED_IN)) {
                JsonElement elem = object.get(AccountsContract.ACCOUNT_DATE_LAST_SIGNED_IN);
                if(!elem.isJsonNull()) {
                    g = elem.getAsJsonPrimitive().getAsLong();
                }
            }
            String h = "";
            if(object.has(AccountsContract.ACCOUNT_LAST_SIGNED_IN_FROM)) {
                JsonElement elem = object.get(AccountsContract.ACCOUNT_LAST_SIGNED_IN_FROM);
                if(!elem.isJsonNull()) {
                    h = elem.getAsJsonPrimitive().getAsString();
                }
            }
            return new UserAccountDetails(a, b, c, d, e, f, g, h);
        }
    }

}
