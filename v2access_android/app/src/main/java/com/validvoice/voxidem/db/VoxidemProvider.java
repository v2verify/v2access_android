package com.validvoice.voxidem.db;

import com.validvoice.dynamic.db.BaseContract;
import com.validvoice.dynamic.db.BaseDatabase;
import com.validvoice.dynamic.db.BaseProvider;
import com.validvoice.voxidem.BuildConfig;
import com.validvoice.voxidem.db.contracts.AccountsContract;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.contracts.HistoryContract;

public class VoxidemProvider extends BaseProvider {

    @Override
    public BaseDatabase onCreateDatabase() {
        return new VoxidemDatabase(getContext());
    }

    static {
        BaseContract.Initialize(BuildConfig.APPLICATION_ID);
        AccountsContract.PopulateProviderContracts(getPopulator());
        DevicesContract.PopulateProviderContracts(getPopulator());
        HistoryContract.PopulateProviderContracts(getPopulator());
    }

}
