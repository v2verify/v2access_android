package com.validvoice.dynamic.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class BaseContract implements BaseColumns {

    protected static final Boolean DB_DEBUG = true;

    private static String mContentAuthority = "";

    private static Uri mBaseContentUri = null;

    public static void Initialize(String applicationId) {
        if(mContentAuthority.isEmpty()) {
            mContentAuthority = applicationId + ".db.provider";
            mBaseContentUri = Uri.parse("content://" + mContentAuthority);
            BaseProvider.Initialize(new BaseProvider.INotifier() {
                @Override
                public void notifyChange(@NonNull Context context, @NonNull Uri uri,
                                         @Nullable ContentObserver observer,
                                         boolean syncToNetwork) {
                    context.getContentResolver().notifyChange(uri, observer, syncToNetwork);
                }
            });
        }
    }

    public static void Initialize(String applicationId, BaseProvider.INotifier notifier) {
        if(mContentAuthority.isEmpty()) {
            mContentAuthority = applicationId + ".db.provider";
            mBaseContentUri = Uri.parse("content://" + mContentAuthority);
            BaseProvider.Initialize(notifier);
        }
    }

    public static String getContentAuthority() {
        return mContentAuthority;
    }

    public static Uri getBaseContentUri() {
        return mBaseContentUri;
    }

    public static class SimpleProviderContract implements IProviderContract {

        private static final String TAG = SimpleProviderContract.class.getSimpleName();

        private final String mTableName;
        private final Uri mContentUri;

        public SimpleProviderContract(@NonNull String tableName, @NonNull Uri contentUri) {
            mTableName = tableName;
            mContentUri = contentUri;
        }

        @Override
        public ProviderType getType() {
            return ProviderType.DatabaseProvider;
        }

        @Override
        public Cursor query(SQLiteDatabase db, String id, String[] projection) {
            return new SelectionBuilder()
                    .table(mTableName)
                    .where(_ID + " = ?", id)
                    .query(db, projection, "");
        }

        @Override
        public Cursor query(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            return new SelectionBuilder()
                    .table(mTableName)
                    .where(selection, selectionArgs)
                    .query(db, projection, sortOrder);
        }

        @Override
        public Uri insert(SQLiteDatabase db, ContentValues values) {
            Log.v(TAG, mTableName + "(insert): " + values.toString());
            return Uri.parse(mContentUri + "/" + db.insertOrThrow(mTableName, null, values));
        }

        @Override
        public int delete(SQLiteDatabase db, String id) {
            return new SelectionBuilder()
                    .table(mTableName)
                    .where(_ID + " = ?", id)
                    .delete(db);
        }

        @Override
        public int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
            return new SelectionBuilder()
                    .table(mTableName)
                    .where(selection, selectionArgs)
                    .delete(db);
        }

        @Override
        public int update(SQLiteDatabase db, ContentValues values, String id) {
            return new SelectionBuilder()
                    .table(mTableName)
                    .where(_ID + "=?", id)
                    .update(db, values);
        }

        @Override
        public int update(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {
            return new SelectionBuilder()
                    .table(mTableName)
                    .where(selection, selectionArgs)
                    .update(db, values);
        }

        @Override
        public Uri switchOn(Context context) {
            throw new UnsupportedOperationException("SimpleProviderContract does not support switch actions");
        }

        @Override
        public int switchOff(Context context) {
            throw new UnsupportedOperationException("SimpleProviderContract does not support switch actions");
        }
    }

    public static class SwitchProviderContract implements IProviderContract {

        @Override
        public ProviderType getType() {
            return ProviderType.SwitchProvider;
        }

        @Override
        public Cursor query(SQLiteDatabase db, String id, String[] projection) {
            throw new UnsupportedOperationException("SwitchProviderContract does not support query");
        }

        @Override
        public Cursor query(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            throw new UnsupportedOperationException("SwitchProviderContract does not support query");
        }

        @Override
        public Uri insert(SQLiteDatabase db, ContentValues values) {
            throw new UnsupportedOperationException("SwitchProviderContract does not support insert");
        }

        @Override
        public int delete(SQLiteDatabase db, String id) {
            throw new UnsupportedOperationException("SwitchProviderContract does not support delete");
        }

        @Override
        public int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
            throw new UnsupportedOperationException("SwitchProviderContract does not support delete");
        }

        @Override
        public int update(SQLiteDatabase db, ContentValues values, String id) {
            throw new UnsupportedOperationException("SwitchProviderContract does not support update");
        }

        @Override
        public int update(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {
            throw new UnsupportedOperationException("SwitchProviderContract does not support update");
        }

        @Override
        public Uri switchOn(Context context) {
            return null;
        }

        @Override
        public int switchOff(Context context) {
            return 0;
        }
    }

    public static class SimpleDatabaseContract implements IDatabaseContract {

        private final String mCreateTable;
        private final String mDropTable;
        private final IProviderContract mTableProvider;

        private ArrayList<ContentValues> mUpgradeList;

        public SimpleDatabaseContract(@NonNull String createTable,
                                      @NonNull String dropTable,
                                      @NonNull IProviderContract provider) {
            mCreateTable = createTable;
            mDropTable = dropTable;
            if(provider.getType() != IProviderContract.ProviderType.DatabaseProvider) {
                throw new IllegalArgumentException("SimpleDatabaseContract only supports IProviderContract.DatabaseProvider's");
            }
            mTableProvider = provider;
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(mCreateTable);
            if(mUpgradeList != null) {
                for(ContentValues values : mUpgradeList) {
                    mTableProvider.insert(db, values);
                }
                mUpgradeList = null;
            }
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                Cursor cursor = mTableProvider.query(db, null, null, null, null);
                if (cursor.moveToFirst()) {
                    mUpgradeList = new ArrayList<>();
                    do {
                        try {
                            ContentValues values = onUpgradeValues(cursor, oldVersion, newVersion);
                            mUpgradeList.add(values);
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch(Exception ex) {
                ex.printStackTrace();
                mUpgradeList = null;
            }
            db.execSQL(mDropTable);
        }

        public ContentValues onUpgradeValues(Cursor cursor, int oldVersion, int newVersion) {
            ContentValues values = new ContentValues();
            final int columns = cursor.getColumnCount();
            for(int i = 0; i < columns; ++i) {
                final String columnName = cursor.getColumnName(i);
                switch(cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_NULL:
                        values.put(columnName, (String)null);
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        values.put(columnName, cursor.getInt(i));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        values.put(columnName, cursor.getDouble(i));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        values.put(columnName, cursor.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_BLOB:
                        values.put(columnName, cursor.getBlob(i));
                        break;
                }
            }
            return values;
        }

    }

}
