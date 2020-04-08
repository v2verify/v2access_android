package com.validvoice.voxidem.db.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.validvoice.dynamic.db.IContractModel;
import com.validvoice.voxidem.db.contracts.DevicesContract;

import java.util.Calendar;

public class DeviceModel implements IContractModel {

    private long mId;

    private String mDeviceId;
    private String mDeviceName;
    private String mDeviceType;
    private String mLastKnownIpAddress;
    private Calendar mDateAdded;
    private Calendar mDateLastUsed;

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public String getIdAsString() {
        return Long.toString(mId);
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getDeviceType() {
        return mDeviceType;
    }

    public Calendar getDateAdded() {
        return mDateAdded;
    }

    public Calendar getDateLastUsed() {
        return mDateLastUsed;
    }

    public String getLastKnownIPAddress() {
        return mLastKnownIpAddress;
    }

    public void updateDeviceName(String name) {
        mDeviceName = name;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(DevicesContract.DEVICE_ID, mDeviceId);
        values.put(DevicesContract.DEVICE_NICKNAME, mDeviceName);
        values.put(DevicesContract.DEVICE_TYPE, mDeviceType);
        values.put(DevicesContract.DEVICE_DATE_ADDED, mDateAdded.getTimeInMillis());
        values.put(DevicesContract.DEVICE_DATE_LAST_USED, mDateLastUsed.getTimeInMillis());
        values.put(DevicesContract.DEVICE_LAST_KNOWN_IP_ADDRESS, mLastKnownIpAddress);
        return values;
    }

    public static String[] DEVICE_PROJECTION = {
            DevicesContract._ID,
            DevicesContract.DEVICE_ID,
            DevicesContract.DEVICE_NICKNAME,
            DevicesContract.DEVICE_TYPE,
            DevicesContract.DEVICE_DATE_ADDED,
            DevicesContract.DEVICE_DATE_LAST_USED,
            DevicesContract.DEVICE_LAST_KNOWN_IP_ADDRESS
    };

    private DeviceModel() {
        mId = -1;
        mDeviceId = "";
        mDeviceName = "";
        mDeviceType = "";
        mDateAdded = Calendar.getInstance();
        mDateLastUsed = Calendar.getInstance();
        mLastKnownIpAddress = "";
    }

    private DeviceModel(long id, String deviceId, String deviceName, String deviceType,
                        Calendar addDate, Calendar lastUsedDate,
                        String lastKnownIpAddress) {
        mId = id;
        mDeviceId = deviceId;
        mDeviceName = deviceName;
        mDeviceType = deviceType;
        mDateAdded = addDate;
        mDateLastUsed = lastUsedDate;
        mLastKnownIpAddress = lastKnownIpAddress;
    }

    public static DeviceModel createRecord(String deviceId, String deviceType,
                                           String deviceName, String deviceIp) {
        return new DeviceModel(-1, deviceId, deviceName, deviceType,
                Calendar.getInstance(), Calendar.getInstance(), deviceIp);
    }

    public static DeviceModel createRecord(String deviceId, String deviceType,
                                           String deviceName, String deviceIp,
                                           Calendar addDate, Calendar lastUsedDate) {
        return new DeviceModel(-1, deviceId, deviceName, deviceType,
                addDate, lastUsedDate, deviceIp);
    }

    public static DeviceModel createRecordFromCursor(Cursor cursor) {
        DeviceModel model = new DeviceModel();
        model.mId = cursor.getLong(cursor.getColumnIndex(DevicesContract._ID));
        model.mDeviceId = cursor.getString(cursor.getColumnIndex(DevicesContract.DEVICE_ID));
        model.mDeviceName = cursor.getString(cursor.getColumnIndex(DevicesContract.DEVICE_NICKNAME));
        model.mDeviceType = cursor.getString(cursor.getColumnIndex(DevicesContract.DEVICE_TYPE));
        model.mDateAdded.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DevicesContract.DEVICE_DATE_ADDED)));
        model.mDateLastUsed.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(DevicesContract.DEVICE_DATE_LAST_USED)));
        model.mLastKnownIpAddress = cursor.getString(cursor.getColumnIndex(DevicesContract.DEVICE_LAST_KNOWN_IP_ADDRESS));
        return model;
    }
}
