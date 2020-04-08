package com.validvoice.voxidem.db.models;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;

import com.validvoice.dynamic.db.IContractModel;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.contracts.HistoryContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HistoryModel implements IContractModel {

    private long mId;

    private static final String TAG = "HistoryModel";
    private HistoryContract.HistoryTypes mType;
    private String mAccountName;
    private long mAccountId;
    private String mCompanyName;
    private String mDeviceName;
    private long mDeviceId;
    private String mUserName;
    private String mResult;
    private String mExpectedText;
    private String mCapturedText;
    private String mSessionId;
    private Boolean mHasFeedback;
    private Boolean mFeedbackBreakAttempt;
    private Boolean mFeedbackRecording;
    private Boolean mFeedbackBackgroundNoise;
    private String mFeedbackComments;
    private Calendar mDateTime;

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public String getIdAsString() {
        return Long.toString(mId);
    }

    public String getDateTimeAsString() {
        SimpleDateFormat datetimeFormat = new SimpleDateFormat("MM/dd/yyyy - hh:mm a", new Locale("en-US"));
        return datetimeFormat.format(mDateTime.getTime());
    }

    public String getDataAsString(Context context) {
        Resources res = context.getResources();
        switch(mType) {
            case Enrollment:
                String strEnrolled = mResult.toLowerCase().equals("success") ?
                        res.getString(R.string.scene_history_enrolled) :
                        res.getString(R.string.scene_history_not_enrolled);
                return String.format(
                        res.getString(R.string.scene_history_enrollment_type),
                        mUserName, strEnrolled
                    );
            case EnrollmentVerification:
                String strEnrollVerified = mResult.toLowerCase().equals("success - pass") ?
                        res.getString(R.string.scene_history_verified) :
                        res.getString(R.string.scene_history_not_verified);
                return String.format(
                        context.getResources().getString(R.string.scene_history_enrollment_verification_type),
                        mUserName, strEnrollVerified
                    );
            case VerificationQr:
            case VerificationGuest:
                String strVerified = mResult.toLowerCase().equals("passisalive") ?
                        res.getString(R.string.scene_history_granted) :
                        res.getString(R.string.scene_history_not_granted);
                String strVerifiedAccessType = mDeviceName.toLowerCase().equals("hardware") ?
                        res.getString(R.string.scene_history_door) :
                        res.getString(R.string.scene_history_account);
                return String.format(
                        context.getResources().getString(R.string.scene_history_verification_type),
                        mUserName, strVerified, strVerifiedAccessType, mAccountName, mCompanyName
                );
            case VerificationDevice:
                String strVerifiedDevice = mResult.toLowerCase().equals("passisalive") ?
                        res.getString(R.string.scene_history_granted) :
                        res.getString(R.string.scene_history_not_granted);
                return String.format(
                        context.getResources().getString(R.string.scene_history_verification_device_type),
                        mUserName, strVerifiedDevice, mAccountName, mCompanyName, mDeviceName
                );
            case AccountLink:
                String strVerifiedLink = mResult.toLowerCase().equals("passisalive") ?
                        res.getString(R.string.scene_history_linked) :
                        res.getString(R.string.scene_history_not_linked);
                return String.format(
                        context.getResources().getString(R.string.scene_history_account_link_type),
                        mUserName, strVerifiedLink, mAccountName, mCompanyName
                );
            case DeviceLink:
                return String.format(
                        context.getResources().getString(R.string.scene_history_device_link_type),
                        mUserName, mDeviceName
                );
            case DeviceUpdate:
                return String.format(
                        context.getResources().getString(R.string.scene_history_device_update_type),
                        mUserName, mAccountName, mDeviceName
                );
            case DeviceDelete:
                return String.format(
                        context.getResources().getString(R.string.scene_history_device_delete_type),
                        mUserName, mDeviceName
                );
        }
        return "<Unknown>";
    }

    public String getUser() {
        return mUserName;
    }

    public String getResult() {

        if (mResult.toLowerCase().equals("passisalive")
                || mResult.toLowerCase().equals("success - pass")
                || mResult.toLowerCase().equals("success")
                || mResult.toLowerCase().equals("success")) {
            return "Pass";
        }else{
            return  "Failure";
        }
    }

    public HistoryContract.HistoryTypes getmType() {
        return mType;
    }

    public String getmAccountName() {
        return mAccountName;
    }

    public String getmDeviceName() {
        return  (mDeviceName.isEmpty()?"QR Code Scanned":mDeviceName);
     //   return mDeviceName;
    }

    public String getmCompanyName() {
        return mCompanyName;
    }

    public String getHistoryType() {
        switch(mType) {
            case Enrollment: return "Enroll";
            case EnrollmentVerification: return "Verify Enrollment";
            case VerificationQr: return "Qr Verify";
            case VerificationDevice: return "Device Verify";
            case VerificationGuest: return "Guest Verify";
            case AccountLink: return "Link Account";
            case DeviceLink: return "Link Device";
            case DeviceUpdate: return "Update Device";
            case DeviceDelete: return "Delete Device";
        }
        return "Unknown";
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(HistoryContract.HISTORY_TYPE, mType.ordinal());
        values.put(HistoryContract.HISTORY_ACCOUNT_NAME, mAccountName);
        values.put(HistoryContract.HISTORY_ACCOUNT_ID, mAccountId);
        values.put(HistoryContract.HISTORY_COMPANY_NAME, mCompanyName);
        values.put(HistoryContract.HISTORY_DEVICE_NAME, mDeviceName);
        values.put(HistoryContract.HISTORY_DEVICE_ID, mDeviceId);
        values.put(HistoryContract.HISTORY_USER_NAME, mUserName);
        values.put(HistoryContract.HISTORY_RESULT, mResult);
        values.put(HistoryContract.HISTORY_EXPECTED_TEXT, mExpectedText);
        values.put(HistoryContract.HISTORY_CAPTURED_TEXT, mCapturedText);
        values.put(HistoryContract.HISTORY_SESSION_ID, mSessionId);
        if(mHasFeedback) {
            values.put(HistoryContract.HISTORY_FEEDBACK_BREAK_ATTEMPT, mFeedbackBreakAttempt ? 1 : 0);
            values.put(HistoryContract.HISTORY_FEEDBACK_RECORDING, mFeedbackRecording ? 1 : 0);
            values.put(HistoryContract.HISTORY_FEEDBACK_BACKGROUND_NOISE, mFeedbackBackgroundNoise ? 1 : 0);
            values.put(HistoryContract.HISTORY_FEEDBACK_COMMENTS, mFeedbackComments);
        }
        values.put(HistoryContract.HISTORY_DATETIME, mDateTime.getTimeInMillis());
        return values;
    }

    public HistoryModel setAccount(AccountModel model) {
        mAccountName = model.getAccountUsername();
        mAccountId = model.getId();
        mCompanyName = model.getCompanyName();
        return this;
    }

    public HistoryModel setAccount(String accountName, long accountId, String companyName) {
        mAccountName = accountName;
        mAccountId = accountId;
        mCompanyName = companyName;
        return this;
    }

    public HistoryModel setHardware(boolean enable) {
        if(mDeviceName.isEmpty() && enable) {
            mDeviceName = "hardware";
        }
        return this;
    }

    public HistoryModel setDevice(DeviceModel model) {
        mDeviceName = model.getDeviceName();
        mDeviceId = model.getId();
        return this;
    }

    public HistoryModel setDevice(String deviceName, long deviceId) {
        mDeviceName = deviceName;
        mDeviceId = deviceId;
        return this;
    }

    public HistoryModel updateDevice(String oldDeviceName, String newDeviceName, long deviceId) {
        mAccountName = oldDeviceName;
        mDeviceName = newDeviceName;
        mDeviceId = deviceId;
        return this;
    }

    public HistoryModel setResult(String result) {
        mResult = result;
        return this;
    }

    public HistoryModel setSessionId(String sessionId) {
        mSessionId = sessionId;
        return this;
    }

    public HistoryModel setLivenessData(String expectedText, String capturedText) {
        mExpectedText = expectedText;
        mCapturedText = capturedText;
        return this;
    }

    public HistoryModel setFeedback(Boolean isBreakAttempt, boolean isRecording, boolean isBackgroundNoise, String comments) {
        mHasFeedback = true;
        mFeedbackBreakAttempt = isBreakAttempt;
        mFeedbackRecording = isRecording;
        mFeedbackBackgroundNoise = isBackgroundNoise;
        mFeedbackComments = comments;
        return this;
    }

    private HistoryModel() {
        mId = -1;
        mType = HistoryContract.HistoryTypes.Unknown;
        mAccountName = "";
        mAccountId = -1;
        mCompanyName = "";
        mDeviceName = "";
        mDeviceId = -1;
        mUserName = "";
        mResult = "";
        mExpectedText = "";
        mCapturedText = "";
        mSessionId = "";
        mHasFeedback = false;
        mFeedbackBreakAttempt = false;
        mFeedbackRecording = false;
        mFeedbackBackgroundNoise = false;
        mFeedbackComments = "";
        mDateTime = Calendar.getInstance();
    }

    private HistoryModel(HistoryContract.HistoryTypes type, String username) {
        mId = -1;
        mType = type;
        mAccountName = "";
        mAccountId = -1;
        mCompanyName = "";
        mDeviceName = "";
        mDeviceId = -1;
        mUserName = username;
        mResult = "";
        mExpectedText = "";
        mCapturedText = "";
        mSessionId = "";
        mHasFeedback = false;
        mFeedbackBreakAttempt = false;
        mFeedbackRecording = false;
        mFeedbackBackgroundNoise = false;
        mFeedbackComments = "";
        mDateTime = Calendar.getInstance();
    }

    private HistoryModel(long id, HistoryContract.HistoryTypes type, String accountName,
                         long accountId, String companyName, String deviceName, long deviceId,
                         String userName, Calendar dateTime) {
        mId = id;
        mType = type;
        mAccountName = accountName;
        mAccountId = accountId;
        mCompanyName = companyName;
        mDeviceName = deviceName;
        mDeviceId = deviceId;
        mUserName = userName;
        mDateTime = dateTime;
    }

    private static HistoryModel createRecord(long id, HistoryContract.HistoryTypes type, String accountName,
                                            long accountId, String companyName, String deviceName,
                                            long deviceId, String userName, Calendar dateTime) {
        return new HistoryModel(id, type, accountName, accountId, companyName, deviceName, deviceId,
                                userName, dateTime);
    }

    public static HistoryModel createEnrollmentRecord(String username) {
        return new HistoryModel(HistoryContract.HistoryTypes.Enrollment, username);
    }

    public static HistoryModel createEnrollmentVerificationRecord(String username) {
        return new HistoryModel(HistoryContract.HistoryTypes.EnrollmentVerification, username);
    }

    public static HistoryModel createVerificationQrRecord(String username, String accountName,
                                                          long accountId, String companyName) {
        HistoryModel model = new HistoryModel(HistoryContract.HistoryTypes.VerificationQr, username);
        model.setAccount(accountName, accountId, companyName);
        return model;
    }

    public static HistoryModel createVerificationQrLinkRecord(String username, String accountName,
                                                          String companyName) {
        HistoryModel model = new HistoryModel(HistoryContract.HistoryTypes.AccountLink, username);
        model.setAccount(accountName, -1, companyName);
        return model;
    }

    public static HistoryModel createVerificationDeviceRecord(String username, String accountName,
                                                              long accountId, String companyName,
                                                          String deviceName, long deviceId) {
        HistoryModel model = new HistoryModel(HistoryContract.HistoryTypes.VerificationDevice, username);
        model.setAccount(accountName, accountId, companyName);
        model.setDevice(deviceName, deviceId);
        return model;
    }

    public static HistoryModel createVerificationGuestRecord(String username, String accountName,
                                                             String companyName) {
        HistoryModel model = new HistoryModel(HistoryContract.HistoryTypes.VerificationGuest, username);
        model.setAccount(accountName, -1, companyName);
        return model;
    }

    public static HistoryModel createDeviceRecord(long deviceId, String username, String deviceName) {
        HistoryModel model = new HistoryModel(HistoryContract.HistoryTypes.DeviceLink, username);
        model.setDevice(deviceName, deviceId);
        return model;
    }

    public static HistoryModel updateDeviceRecord(long deviceId, String username, String oldDeviceName, String newDeviceName) {
        HistoryModel model = new HistoryModel(HistoryContract.HistoryTypes.DeviceUpdate, username);
        model.updateDevice(oldDeviceName, newDeviceName, deviceId);
        return model;
    }

    public static HistoryModel deleteDeviceRecord(long deviceId, String username, String deviceName) {
        HistoryModel model = new HistoryModel(HistoryContract.HistoryTypes.DeviceDelete, username);
        model.setDevice(deviceName, deviceId);
        return model;
    }

    public static HistoryModel createRecordFromCursor(Cursor cursor) {
        HistoryModel model = new HistoryModel();
        model.mId = cursor.getLong(cursor.getColumnIndex(HistoryContract._ID));
        model.mType = HistoryContract.HistoryTypes.values()[cursor.getInt(cursor.getColumnIndex(HistoryContract.HISTORY_TYPE))];
        model.mUserName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_USER_NAME));
        model.mResult = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_RESULT));
        model.mDateTime.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(HistoryContract.HISTORY_DATETIME)));
        switch(model.mType) {
            case VerificationQr:
            case VerificationGuest:
                model.mAccountName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_ACCOUNT_NAME));
                model.mCompanyName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_COMPANY_NAME));
                if(!cursor.isNull(cursor.getColumnIndex(HistoryContract.HISTORY_DEVICE_NAME))) {
                    model.mDeviceName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_DEVICE_NAME));
                }
                model.mExpectedText = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_EXPECTED_TEXT));
                model.mCapturedText = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_CAPTURED_TEXT));
                break;
            case VerificationDevice:
                model.mAccountName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_ACCOUNT_NAME));
                model.mCompanyName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_COMPANY_NAME));
                model.mDeviceName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_DEVICE_NAME));
                model.mExpectedText = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_EXPECTED_TEXT));
                model.mCapturedText = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_CAPTURED_TEXT));
                break;
            case AccountLink:
                model.mAccountName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_ACCOUNT_NAME));
                model.mCompanyName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_COMPANY_NAME));
                break;
            case DeviceLink:
            case DeviceDelete:
                model.mDeviceName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_DEVICE_NAME));
                break;
            case DeviceUpdate:
                model.mAccountName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_ACCOUNT_NAME));
                model.mDeviceName = cursor.getString(cursor.getColumnIndex(HistoryContract.HISTORY_DEVICE_NAME));
                break;
        }

        int columnIndex;
        columnIndex = cursor.getColumnIndex(HistoryContract.HISTORY_SESSION_ID);
        if (-1 != columnIndex && !cursor.isNull(columnIndex)) {
            model.mSessionId = cursor.getString(columnIndex);
        } else {
            model.mSessionId = "";
        }

        columnIndex = cursor.getColumnIndex(HistoryContract.HISTORY_HAS_FEEDBACK);
        model.mHasFeedback = -1 != columnIndex && !cursor.isNull(columnIndex) && cursor.getInt(columnIndex) == 1;
        if(model.mHasFeedback) {
            columnIndex = cursor.getColumnIndex(HistoryContract.HISTORY_FEEDBACK_BREAK_ATTEMPT);
            model.mFeedbackBreakAttempt = -1 != columnIndex && !cursor.isNull(columnIndex) && cursor.getInt(columnIndex) == 1;

            columnIndex = cursor.getColumnIndex(HistoryContract.HISTORY_FEEDBACK_RECORDING);
            model.mFeedbackRecording = -1 != columnIndex && !cursor.isNull(columnIndex) && cursor.getInt(columnIndex) == 1;

            columnIndex = cursor.getColumnIndex(HistoryContract.HISTORY_FEEDBACK_BACKGROUND_NOISE);
            model.mFeedbackBackgroundNoise = -1 != columnIndex && !cursor.isNull(columnIndex) && cursor.getInt(columnIndex) == 1;

            columnIndex = cursor.getColumnIndex(HistoryContract.HISTORY_FEEDBACK_COMMENTS);
            if (-1 != columnIndex && !cursor.isNull(columnIndex)) {
                model.mFeedbackComments = cursor.getString(columnIndex);
            } else {
                model.mFeedbackComments = "";
            }
        } else {
            model.mFeedbackBreakAttempt = false;
            model.mFeedbackRecording = false;
            model.mFeedbackBackgroundNoise = false;
            model.mFeedbackComments = "";
        }

        return model;
    }

    public static String[] HISTORY_PROJECTION = {
            HistoryContract._ID,
            HistoryContract.HISTORY_TYPE,
            HistoryContract.HISTORY_ACCOUNT_NAME,
            HistoryContract.HISTORY_ACCOUNT_ID,
            HistoryContract.HISTORY_COMPANY_NAME,
            HistoryContract.HISTORY_DEVICE_NAME,
            HistoryContract.HISTORY_DEVICE_ID,
            HistoryContract.HISTORY_USER_NAME,
            HistoryContract.HISTORY_RESULT,
            HistoryContract.HISTORY_EXPECTED_TEXT,
            HistoryContract.HISTORY_CAPTURED_TEXT,
            HistoryContract.HISTORY_SESSION_ID,
            HistoryContract.HISTORY_HAS_FEEDBACK,
            HistoryContract.HISTORY_FEEDBACK_BREAK_ATTEMPT,
            HistoryContract.HISTORY_FEEDBACK_RECORDING,
            HistoryContract.HISTORY_FEEDBACK_BACKGROUND_NOISE,
            HistoryContract.HISTORY_FEEDBACK_COMMENTS,
            HistoryContract.HISTORY_DATETIME
    };

}
