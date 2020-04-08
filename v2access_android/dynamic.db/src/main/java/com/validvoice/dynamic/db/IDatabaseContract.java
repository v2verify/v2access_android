package com.validvoice.dynamic.db;

import android.database.sqlite.SQLiteDatabase;

public interface IDatabaseContract {

    interface IPopulator {

        void addTableContract(IDatabaseContract tableContract);

        void addViewContract(IDatabaseContract viewContract);

        void addTriggerContract(IDatabaseContract triggerContract);

    }

    void onCreate(SQLiteDatabase db);

    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

}
