package com.validvoice.dynamic.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

public abstract class BaseProvider extends ContentProvider {

    /**
     *
     */
    private static class ProviderContractWrapper {

        enum Type {
            DatabaseProvider,
            DatabasePathProvider,
            SwitchProvider
        }

        Type ProviderType;
        IProviderContract ProviderContract;

        ProviderContractWrapper(Type providerType, IProviderContract providerContract) {
            ProviderType = providerType;
            ProviderContract = providerContract;
        }

    }

    /**
     *
     */
    public interface INotifier {
        void notifyChange(@NonNull Context context,
                          @NonNull Uri uri,
                          @Nullable ContentObserver observer,
                          boolean syncToNetwork);
    }

    /**
     *
     */
    private static int mNextId = Integer.MAX_VALUE;

    /**
     *
     */
    private static SparseArray<ProviderContractWrapper> mProviderContracts = new SparseArray<>();

    /**
     *
     */
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     *
     */
    private static INotifier mNotifier;

    /**
     *
     */
    private static IProviderContract.IPopulator mPopulator = new IProviderContract.IPopulator() {
        @Override
        public void addProviderContract(IProviderContract providerContract, String path) {
            final int contractId = --mNextId;
            final ProviderContractWrapper.Type providerType;
            providerType =
                providerContract.getType() == IProviderContract.ProviderType.DatabaseProvider
                    ? path.endsWith("/#")
                        ? ProviderContractWrapper.Type.DatabasePathProvider
                        : ProviderContractWrapper.Type.DatabaseProvider
                    :  ProviderContractWrapper.Type.SwitchProvider;
            mUriMatcher.addURI(BaseContract.getContentAuthority(), path, contractId);
            mProviderContracts.put(contractId, new ProviderContractWrapper(providerType, providerContract));
        }
    };

    protected static IProviderContract.IPopulator getPopulator() {
        return mPopulator;
    }

    public static void Initialize(INotifier notifier) {
        if(mNotifier == null) {
            if (notifier == null) throw new AssertionError("INotifier == null");
            mNotifier = notifier;
        }
    }

    private BaseDatabase mDatabase;

    public abstract BaseDatabase onCreateDatabase();

    @Override
    public boolean onCreate() {
        mDatabase = onCreateDatabase();
        return mDatabase != null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        ProviderContractWrapper providerWrapper = mProviderContracts.get(mUriMatcher.match(uri));
        if(providerWrapper == null) {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Cursor cursor;
        final SQLiteDatabase db = mDatabase.getReadableDatabase();
        assert db != null;
        switch(providerWrapper.ProviderType) {
            case DatabaseProvider:
                cursor = providerWrapper.ProviderContract.query(db, projection, selection, selectionArgs, sortOrder);
                break;
            case DatabasePathProvider:
                cursor = providerWrapper.ProviderContract.query(db, uri.getLastPathSegment(), projection);
                break;
            default:
                throw new UnsupportedOperationException("Provider Type is not query-able");
        }
        Context ctx = getContext();
        if (ctx == null) throw new AssertionError("query context is null");
        cursor.setNotificationUri(ctx.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = mUriMatcher.match(uri);
        ProviderContractWrapper providerWrapper = mProviderContracts.get(match);
        if(providerWrapper == null) {
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Uri result;
        switch(providerWrapper.ProviderType) {
            case DatabaseProvider:
                final SQLiteDatabase db = mDatabase.getWritableDatabase();
                assert db != null;
                result = providerWrapper.ProviderContract.insert(db, values);
                break;
            case SwitchProvider:
                result = providerWrapper.ProviderContract.switchOn(getContext());
                break;
            default:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
        }
        Context context = getContext();
        if (context == null) throw new AssertionError("insert context is null");
        if (mNotifier == null) throw new AssertionError("insert notifier is null");
        mNotifier.notifyChange(context, uri, null, false);
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = mUriMatcher.match(uri);
        ProviderContractWrapper providerWrapper = mProviderContracts.get(match);
        if(providerWrapper == null) {
            throw new UnsupportedOperationException("Unknown delete uri: " + uri);
        }
        int count;
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        assert db != null;
        switch(providerWrapper.ProviderType) {
            case DatabaseProvider:
                count = providerWrapper.ProviderContract.delete(db, selection, selectionArgs);
                break;
            case DatabasePathProvider:
                count = providerWrapper.ProviderContract.delete(db, uri.getLastPathSegment());
                break;
            case SwitchProvider:
                count = providerWrapper.ProviderContract.switchOff(getContext());
                break;
            default:
                throw new UnsupportedOperationException("Delete not supported on URI: " + uri);
        }
        Context context = getContext();
        if (context == null) throw new AssertionError("delete context is null");
        if (mNotifier == null) throw new AssertionError("delete notifier is null");
        mNotifier.notifyChange(context, uri, null, false);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        ProviderContractWrapper providerWrapper = mProviderContracts.get(mUriMatcher.match(uri));
        if(providerWrapper == null) {
            throw new UnsupportedOperationException("Unknown update uri: " + uri);
        }
        int count;
        final SQLiteDatabase db = mDatabase.getWritableDatabase();
        assert db != null;
        switch(providerWrapper.ProviderType) {
            case DatabaseProvider:
                count = providerWrapper.ProviderContract.update(db, values, selection, selectionArgs);
                break;
            case DatabasePathProvider:
                count = providerWrapper.ProviderContract.update(db, values, uri.getLastPathSegment());
                break;
            default:
                throw new UnsupportedOperationException("Update not supported on URI: " + uri);
        }
        Context context = getContext();
        if (context == null) throw new AssertionError("update context is null");
        if (mNotifier == null) throw new AssertionError("update notifier is null");
        mNotifier.notifyChange(context, uri, null, false);
        return count;
    }
}
