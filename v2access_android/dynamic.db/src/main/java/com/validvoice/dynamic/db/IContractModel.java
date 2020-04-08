package com.validvoice.dynamic.db;

import android.content.ContentValues;

public interface IContractModel {

    long getId();

    String getIdAsString();

    ContentValues getContentValues();

}
