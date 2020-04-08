package com.validvoice.voxidem.cloud;

import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.ICloudObject;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.models.DeviceModel;

import java.util.Calendar;

public class UserDeviceDetails implements ICloudObject {

    private final DeviceModel mDeviceModel;

    private UserDeviceDetails(String a, String b, String c, String d, long e, long f) {
        Calendar addDate = Calendar.getInstance();
        addDate.setTimeInMillis(e * 1000);
        Calendar lastUsed = Calendar.getInstance();
        lastUsed.setTimeInMillis(f * 1000);
        mDeviceModel = DeviceModel.createRecord(a, b, d, c, addDate, lastUsed);
    }

    public DeviceModel getDeviceModel() {
        return mDeviceModel;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "UserDeviceDetails";
        }

        @Override
        public Class<?> getClassType() {
            return UserDeviceDetails.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            String a = object.get(DevicesContract.DEVICE_ID).getAsJsonPrimitive().getAsString();
            String b = object.get(DevicesContract.DEVICE_TYPE).getAsJsonPrimitive().getAsString();
            String c = object.get(DevicesContract.DEVICE_LAST_KNOWN_IP_ADDRESS).getAsJsonPrimitive().getAsString();
            String d = object.get(DevicesContract.DEVICE_NICKNAME).getAsJsonPrimitive().getAsString();
            long e = object.get(DevicesContract.DEVICE_DATE_ADDED).getAsJsonPrimitive().getAsLong();
            long f = object.get(DevicesContract.DEVICE_DATE_LAST_USED).getAsJsonPrimitive().getAsLong();
            return new UserDeviceDetails(a, b, c, d, e, f);
        }
    }


}
