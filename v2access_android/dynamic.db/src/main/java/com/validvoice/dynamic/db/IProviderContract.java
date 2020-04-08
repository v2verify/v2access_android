package com.validvoice.dynamic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public interface IProviderContract {

    interface IPopulator {

        void addProviderContract(IProviderContract providerContract, String path);

    }

    enum ProviderType {
        DatabaseProvider,
        SwitchProvider
    }

    ProviderType getType();

    // Database Methods

    Cursor query(SQLiteDatabase db, String id, String[] projection);

    Cursor query(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder);

    Uri insert(SQLiteDatabase db, ContentValues values);

    int delete(SQLiteDatabase db, String id);

    int delete(SQLiteDatabase db, String selection, String[] selectionArgs);

    int update(SQLiteDatabase db, ContentValues values, String id);

    int update(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs);

    // Switch Methods

    Uri switchOn(Context context);

    int switchOff(Context context);

}
