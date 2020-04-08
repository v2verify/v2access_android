package com.validvoice.dynamic.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseDatabase extends SQLiteOpenHelper {

    private static final String TAG = BaseDatabase.class.getSimpleName();

    /**
     *
     */
    private static List<IDatabaseContract> mTableContracts = new ArrayList<>();

    /**
     *
     */
    private static List<IDatabaseContract> mViewContracts = new ArrayList<>();

    /**
     *
     */
    private static List<IDatabaseContract> mTriggerContracts = new ArrayList<>();

    private static IDatabaseContract.IPopulator mPopulator = new IDatabaseContract.IPopulator() {

        @Override
        public void addTableContract(IDatabaseContract tableContract) {
            mTableContracts.add(tableContract);
        }

        @Override
        public void addViewContract(IDatabaseContract viewContract) {
            mViewContracts.add(viewContract);
        }

        @Override
        public void addTriggerContract(IDatabaseContract triggerContract) {
            mTriggerContracts.add(triggerContract);
        }
    };

    protected static IDatabaseContract.IPopulator getPopulator() {
        return mPopulator;
    }

    protected BaseDatabase(Context context, String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Create Tables
        for(int i = 0; i < mTableContracts.size(); ++i) {
            mTableContracts.get(i).onCreate(db);
        }

        // Create Views
        for(int i = 0; i < mViewContracts.size(); ++i) {
            mViewContracts.get(i).onCreate(db);
        }

        // Create Triggers
        for(int i = 0; i < mTriggerContracts.size(); ++i) {
            mTriggerContracts.get(i).onCreate(db);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ".");

        // Drop Triggers
        for(int i = 0; i < mTriggerContracts.size(); ++i) {
            mTriggerContracts.get(i).onUpgrade(db, oldVersion, newVersion);
        }

        // Drop Views
        for(int i = mViewContracts.size() - 1; i >= 0; --i) {
            mViewContracts.get(i).onUpgrade(db, oldVersion, newVersion);
        }

        // Drop Tables
        for(int i = mTableContracts.size() - 1; i >= 0; --i) {
            mTableContracts.get(i).onUpgrade(db, oldVersion, newVersion);
        }

        onCreate(db);
    }
}
