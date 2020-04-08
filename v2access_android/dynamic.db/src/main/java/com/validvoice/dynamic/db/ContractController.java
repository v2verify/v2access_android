package com.validvoice.dynamic.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;

public class ContractController {

    private ContentResolver mContentResolver;

    public static ContractController getContractController(Context context) {
        return new ContractController(context);
    }

    private ContractController(Context context) {
        mContentResolver = context.getContentResolver();
    }

    public final ContentResolver getContentResolver() {
        return this.mContentResolver;
    }

    public <Model extends IContractModel> Model getModelById(@NonNull Uri uri, String[] projection, long id, Class<Model> clazz) {
        Cursor c = getContentResolver().query(
                uri.buildUpon().appendPath(Long.toString(id)).build(),
                projection,
                null,
                null,
                null
        );
        Model model = null;
        if( c != null ) {
            if (c.moveToFirst()) {
                try {
                    Constructor<Model> constructor = clazz.getConstructor(Cursor.class);
                    model = constructor.newInstance(c);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c.close();
        }
        return model;
    }

    public <Model extends IContractModel> long insertModel(@NonNull Uri uri, @NonNull Model model) {
        uri = getContentResolver().insert(uri, model.getContentValues());
        String part = uri.getLastPathSegment();
        if (part == null) return -1;
        return Long.parseLong(part);
    }

    public <Model extends IContractModel> void updateModel(@NonNull Uri uri, @NonNull Model model) {
        getContentResolver().update(uri, model.getContentValues(), BaseColumns._ID + " = ?", new String[] { model.getIdAsString() });
    }

    public void updateField(@NonNull Uri uri, long id, @NonNull String field, @NonNull String value) {
        ContentValues values = new ContentValues();
        values.put(field, value);
        getContentResolver().update(uri, values, BaseColumns._ID + " = ?", new String[] { Long.toString(id) });
    }

    public <Model extends IContractModel> void deleteModel(@NonNull Uri uri, @NonNull Model model) {
        getContentResolver().delete(uri, BaseColumns._ID + " = ?", new String[] { model.getIdAsString() });
    }

    public long getId(@NonNull Uri uri, @NonNull String field, @NonNull String equals) {
        Cursor c = getContentResolver().query(
                uri,
                new String[] { BaseColumns._ID },
                field + " = ? ",
                new String[]{ equals },
                null
        );
        long id = -1;
        if( c != null ) {
            if (c.moveToFirst()) {
                try {
                    id = c.getLong(c.getColumnIndex(BaseColumns._ID));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            c.close();
        }
        return id;
    }

    public boolean contains(@NonNull Uri uri, @NonNull String field, @NonNull String equals) {
        Cursor c = getContentResolver().query(
                uri,
                new String[] { BaseColumns._ID },
                field + " = ? ",
                new String[]{ equals },
                null
        );
        if( c != null ) {
            if (c.moveToFirst()) {
                c.close();
                return true;
            }
            c.close();
        }
        return false;
    }

    public int count(@NonNull Uri uri, @NonNull String field, @NonNull String equals) {
        Cursor c = getContentResolver().query(
                uri,
                new String[] { BaseColumns._ID },
                field + " = ? ",
                new String[]{ equals },
                null
        );
        int cnt = 0;
        if( c != null ) {
            if (c.moveToFirst()) {
                ++cnt;
            }
            c.close();
        }
        return cnt;
    }

    public void clear(@NonNull Uri uri) {
        getContentResolver().delete(uri, null, null);
    }

}
