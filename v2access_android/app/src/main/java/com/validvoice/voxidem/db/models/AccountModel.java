package com.validvoice.voxidem.db.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.validvoice.dynamic.db.IContractModel;
import com.validvoice.voxidem.db.contracts.AccountsContract;

import java.util.Calendar;

public class AccountModel implements IContractModel {

    private long mId;

    private int mCompanyId;
    private String mCompanyName;
    private String mCompanyLogo;
    private int mAccountId;
    private String mAccountUsername;
    private Calendar mDateLinked;
    private Calendar mDateLastSignedIn;
    private String mLastSignedInFrom;

    @Override
    public long getId() {
        return mId;
    }

    @Override
    public String getIdAsString() {
        return Long.toString(mId);
    }

    public int getCompanyId() {
        return mCompanyId;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public String getCompanyLogo() {
        return mCompanyLogo;
    }

    public int getAccountId() {
        return mAccountId;
    }

    public String getAccountUsername() {
        return mAccountUsername;
    }

    public Calendar getDateLinked() {
        return mDateLinked;
    }

    public Calendar getDateLastSignedIn() {
        return mDateLastSignedIn;
    }

    public String getLastSignedInFrom() {
        return mLastSignedInFrom;
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();
        values.put(AccountsContract.COMPANY_ID, mCompanyId);
        values.put(AccountsContract.COMPANY_NAME, mCompanyName);
        values.put(AccountsContract.COMPANY_LOGO, mCompanyLogo);
        values.put(AccountsContract.ACCOUNT_ID, mAccountId);
        values.put(AccountsContract.ACCOUNT_NAME, mAccountUsername);
        values.put(AccountsContract.ACCOUNT_DATE_LINKED, mDateLinked.getTimeInMillis());
        values.put(AccountsContract.ACCOUNT_DATE_LAST_SIGNED_IN, mDateLastSignedIn.getTimeInMillis());
        values.put(AccountsContract.ACCOUNT_LAST_SIGNED_IN_FROM, mLastSignedInFrom);
        return values;
    }

    public static String[] ACCOUNT_PROJECTION = {
            AccountsContract._ID,
            AccountsContract.COMPANY_ID,
            AccountsContract.COMPANY_NAME,
            AccountsContract.COMPANY_LOGO,
            AccountsContract.ACCOUNT_ID,
            AccountsContract.ACCOUNT_NAME,
            AccountsContract.ACCOUNT_DATE_LINKED,
            AccountsContract.ACCOUNT_DATE_LAST_SIGNED_IN,
            AccountsContract.ACCOUNT_LAST_SIGNED_IN_FROM
    };

    private AccountModel() {
        mId = -1;
        mCompanyId = -1;
        mCompanyLogo = "";
        mCompanyName = "";
        mAccountId = -1;
        mAccountUsername = "";
        mDateLinked = Calendar.getInstance();
        mDateLastSignedIn = Calendar.getInstance();
        mLastSignedInFrom = "";
    }

    private AccountModel(long id, int companyId, String companyName, String companyLogo,
                         int accountId, String accountUsername, Calendar linkDate,
                         Calendar lastSignedInDate, String lastSignedInFrom) {
        mId = id;
        mCompanyId = companyId;
        mCompanyName = companyName;
        mCompanyLogo = companyLogo;
        mAccountId = accountId;
        mAccountUsername = accountUsername;
        mDateLinked = linkDate;
        mDateLastSignedIn = lastSignedInDate;
        mLastSignedInFrom = lastSignedInFrom;
    }

    public static AccountModel createRecord(int companyId, String companyName,
                                            String companyLogo, int accountId, String accountUsername,
                                            String linkedFrom) {
        return new AccountModel(-1, companyId, companyName, companyLogo, accountId, accountUsername,
                Calendar.getInstance(), Calendar.getInstance(), linkedFrom);
    }

    public static AccountModel createRecord(int companyId, String companyName,
                                            String companyLogo, int accountId, String accountUsername,
                                            Calendar linkDate, Calendar lastSignedInDate,
                                            String linkedFrom) {
        return new AccountModel(-1, companyId, companyName, companyLogo, accountId, accountUsername,
                linkDate, lastSignedInDate, linkedFrom);
    }

    public static AccountModel createRecordFromCursor(Cursor cursor) {
        AccountModel model = new AccountModel();
        model.mId = cursor.getLong(cursor.getColumnIndex(AccountsContract._ID));
        model.mCompanyId = cursor.getInt(cursor.getColumnIndex(AccountsContract.COMPANY_ID));
        model.mCompanyName = cursor.getString(cursor.getColumnIndex(AccountsContract.COMPANY_NAME));
        model.mCompanyLogo = cursor.getString(cursor.getColumnIndex(AccountsContract.COMPANY_LOGO));
        model.mAccountId = cursor.getInt(cursor.getColumnIndex(AccountsContract.ACCOUNT_ID));
        model.mAccountUsername = cursor.getString(cursor.getColumnIndex(AccountsContract.ACCOUNT_NAME));
        model.mDateLinked.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(AccountsContract.ACCOUNT_DATE_LINKED)));
        model.mDateLastSignedIn.setTimeInMillis(cursor.getLong(cursor.getColumnIndex(AccountsContract.ACCOUNT_DATE_LAST_SIGNED_IN)));
        model.mLastSignedInFrom = cursor.getString(cursor.getColumnIndex(AccountsContract.ACCOUNT_LAST_SIGNED_IN_FROM));
        return model;
    }

}
