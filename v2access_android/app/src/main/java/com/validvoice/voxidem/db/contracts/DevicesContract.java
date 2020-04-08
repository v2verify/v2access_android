package com.validvoice.voxidem.db.contracts;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.validvoice.dynamic.db.BaseContract;
import com.validvoice.dynamic.db.IDatabaseContract;
import com.validvoice.dynamic.db.IProviderContract;

public class DevicesContract extends BaseContract {

    private static final String TABLE_NAME = "devices_t";
    public static final Uri CONTENT_URI = BaseContract.getBaseContentUri().buildUpon().appendPath(TABLE_NAME).build();

    public static final String _ID = "_id";
    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_NICKNAME = "device_nickname";
    public static final String DEVICE_TYPE = "device_type";
    public static final String DEVICE_DATE_ADDED = "device_date_added";
    public static final String DEVICE_DATE_LAST_USED = "device_date_last_used";
    public static final String DEVICE_LAST_KNOWN_IP_ADDRESS = "device_last_known_ip_address";

    private static final String CREATE_TABLE = "create table " + TABLE_NAME
            + " ( "
                + _ID + " integer primary key autoincrement, "
                + DEVICE_ID + " text not null, "
                + DEVICE_NICKNAME + " text not null, "
                + DEVICE_TYPE + " text null, "
                + DEVICE_DATE_ADDED + " integer not null, "
                + DEVICE_DATE_LAST_USED + " integer not null, "
                + DEVICE_LAST_KNOWN_IP_ADDRESS + " text null "
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

    //private static IDatabaseContract mTableContract = new SimpleDatabaseContract(
    //        CREATE_TABLE,
    //        DROP_TABLE,
    //        mTableProvider
    //);

    private static class DeviceUpgradeContract extends SimpleDatabaseContract {

        DeviceUpgradeContract() {
            super(CREATE_TABLE, DROP_TABLE, mTableProvider);
        }

        @Override
        public ContentValues onUpgradeValues(Cursor cursor, int oldVersion, int newVersion) {
            ContentValues values = super.onUpgradeValues(cursor, oldVersion, newVersion);
            if(!values.containsKey(DEVICE_DATE_LAST_USED)) {
                values.put(DEVICE_DATE_LAST_USED, values.getAsLong(DEVICE_DATE_ADDED));
            }
            if(values.containsKey("device_expire_time")) {
                values.remove("device_expire_time");
            }
            return values;
        }
    }

    private static IDatabaseContract mTableContract = new DeviceUpgradeContract();

}
