package com.validvoice.voxidem.db.contracts;

import android.net.Uri;

import com.validvoice.dynamic.db.BaseContract;
import com.validvoice.dynamic.db.IDatabaseContract;
import com.validvoice.dynamic.db.IProviderContract;

public class AccountsContract extends BaseContract {

    private static final String TABLE_NAME = "accounts_t";
    public static final Uri CONTENT_URI = BaseContract.getBaseContentUri().buildUpon().appendPath(TABLE_NAME).build();

    public static final String _ID = "_id";
    public static final String COMPANY_ID = "company_id";
    public static final String COMPANY_NAME = "company_name";
    public static final String COMPANY_LOGO = "company_logo";
    public static final String ACCOUNT_ID = "account_id";
    public static final String ACCOUNT_NAME = "account_name";
    public static final String ACCOUNT_DATE_LINKED = "account_date_linked";
    public static final String ACCOUNT_DATE_LAST_SIGNED_IN = "account_date_last_signed_in";
    public static final String ACCOUNT_LAST_SIGNED_IN_FROM = "account_last_signed_in_from";

    private static final String CREATE_TABLE = "create table " + TABLE_NAME
            + " ( "
                + _ID + " integer primary key autoincrement, "
                + COMPANY_ID + " integer not null, "
                + COMPANY_NAME + " text not null, "
                + COMPANY_LOGO + " text not null, "
                + ACCOUNT_ID + " integer not null, "
                + ACCOUNT_NAME + " text not null, "
                + ACCOUNT_DATE_LINKED + " integer not null, "
                + ACCOUNT_DATE_LAST_SIGNED_IN + " integer null, "
                + ACCOUNT_LAST_SIGNED_IN_FROM + " text null "
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
