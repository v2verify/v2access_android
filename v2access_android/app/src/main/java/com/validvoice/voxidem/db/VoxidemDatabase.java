package com.validvoice.voxidem.db;

import android.content.Context;

import com.validvoice.dynamic.db.BaseContract;
import com.validvoice.dynamic.db.BaseDatabase;
import com.validvoice.voxidem.BuildConfig;
import com.validvoice.voxidem.db.contracts.AccountsContract;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.contracts.HistoryContract;

class VoxidemDatabase extends BaseDatabase {

    private static final String DATABASE_NAME = "validvoice-voxidem.db";
    private static final int DATABASE_VERSION = 1;

    VoxidemDatabase(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION);
    }

    static {
        BaseContract.Initialize(BuildConfig.APPLICATION_ID);
        AccountsContract.PopulateDatabaseContracts(getPopulator());
        DevicesContract.PopulateDatabaseContracts(getPopulator());
        HistoryContract.PopulateDatabaseContracts(getPopulator());
    }

}
