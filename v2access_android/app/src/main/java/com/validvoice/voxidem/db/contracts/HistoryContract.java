package com.validvoice.voxidem.db.contracts;

import android.net.Uri;

import com.validvoice.dynamic.db.BaseContract;
import com.validvoice.dynamic.db.IDatabaseContract;
import com.validvoice.dynamic.db.IProviderContract;

public class HistoryContract extends BaseContract {

    private static final String TABLE_NAME = "history_t";
    public static final Uri CONTENT_URI = BaseContract.getBaseContentUri().buildUpon().appendPath(TABLE_NAME).build();

    public static final String HISTORY_TYPE = "history_type";
    public static final String HISTORY_ACCOUNT_NAME = "history_account_name";
    public static final String HISTORY_ACCOUNT_ID = "history_account_id";
    public static final String HISTORY_COMPANY_NAME = "history_company_name";
    public static final String HISTORY_DEVICE_NAME = "history_device_name";
    public static final String HISTORY_DEVICE_ID = "history_device_id";
    public static final String HISTORY_USER_NAME = "history_user_name";
    public static final String HISTORY_RESULT = "history_result";
    public static final String HISTORY_EXPECTED_TEXT = "history_expected_text";
    public static final String HISTORY_CAPTURED_TEXT = "history_captured_text";
    public static final String HISTORY_SESSION_ID = "history_session_id";
    public static final String HISTORY_HAS_FEEDBACK = "history_has_feedback";
    public static final String HISTORY_FEEDBACK_BREAK_ATTEMPT = "history_feedback_break_attempt";
    public static final String HISTORY_FEEDBACK_RECORDING = "history_feedback_recording";
    public static final String HISTORY_FEEDBACK_BACKGROUND_NOISE = "history_feedback_background_noise";
    public static final String HISTORY_FEEDBACK_COMMENTS = "history_feedback_comments";
    public static final String HISTORY_DATETIME = "history_datetime";

    public enum HistoryTypes {
        Unknown,
        Enrollment,                 // username, result, datetime
        EnrollmentVerification,     // username, result, datetime
        VerificationQr,             // accountname, accountid, companyname, username, result, expected, captured, datetime
        VerificationDevice,         // accountname, accountid, companyname, devicename, deviceid, username, result, expected, captured, datetime
        VerificationGuest,          // accountname, accountid, companyname, username, result, expected, captured, datetime
        AccountLink,                // accountname, accountid, companyname, username, datetime
        DeviceLink,                 // devicename, deviceid, username, datetime
        DeviceUpdate,               // deviceid, accountname:olddevicename, devicename:newdevicename, username, datetime
        DeviceDelete                // deviceid, devicename, username, datetime
    }

    private static final String CREATE_TABLE = "create table " + TABLE_NAME
            + " ( "
                + _ID + " integer primary key autoincrement, "
                + HISTORY_TYPE + " integer not null, "
                + HISTORY_USER_NAME + " text null, "
                + HISTORY_ACCOUNT_NAME + " text null, "
                + HISTORY_ACCOUNT_ID + " integer null, "
                + HISTORY_COMPANY_NAME + " text null, "
                + HISTORY_DEVICE_NAME + " text null, "
                + HISTORY_DEVICE_ID + " integer null, "
                + HISTORY_RESULT + " text null, "
                + HISTORY_EXPECTED_TEXT + " text null, "
                + HISTORY_CAPTURED_TEXT + " text null, "
                + HISTORY_SESSION_ID + " text null, "
                + HISTORY_HAS_FEEDBACK + " integer null, "
                + HISTORY_FEEDBACK_BREAK_ATTEMPT + " integer null, "
                + HISTORY_FEEDBACK_RECORDING + " integer null, "
                + HISTORY_FEEDBACK_BACKGROUND_NOISE + " integer null, "
                + HISTORY_FEEDBACK_COMMENTS + " text null, "
                + HISTORY_DATETIME + " integer not null "
            + " );";

    private static final String DROP_TABLE = "drop table if exists " + TABLE_NAME;

    public static void PopulateDatabaseContracts(IDatabaseContract.IPopulator populator) {
        populator.addTableContract(mTableContract);
    }

    public static void PopulateProviderContracts(IProviderContract.IPopulator populator) {
        populator.addProviderContract(mTableProvider, TABLE_NAME);
        populator.addProviderContract(mTableProvider, TABLE_NAME + "/#");
    }

    private static IProviderContract mTableProvider = new SimpleProviderContract(
            TABLE_NAME,
            CONTENT_URI
    );

    private static IDatabaseContract mTableContract = new SimpleDatabaseContract(
            CREATE_TABLE,
            DROP_TABLE,
            mTableProvider
    );

}
