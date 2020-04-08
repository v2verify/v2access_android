package com.validvoice.dynamic.db.contracts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.validvoice.dynamic.db.BaseContract;
import com.validvoice.dynamic.db.BaseProvider;
import com.validvoice.dynamic.db.IProviderContract;

import java.util.HashSet;
import java.util.LinkedList;

public class NotifyContract extends BaseContract {

    public static final String IGNORE = "ignore";
    public static final Uri IGNORE_URI = BaseContract.getBaseContentUri().buildUpon().appendPath(IGNORE).build();

    public static final String SUSPEND = "suspend";
    public static final Uri SUSPEND_URI = BaseContract.getBaseContentUri().buildUpon().appendPath(SUSPEND).build();

    public enum NotifyType {
        NOTIFY_IGNORE,
        NOTIFY_SUSPEND,
        NOTIFY_RESUME
    }

    private static NotifyType mPreviousNotificationType = NotifyType.NOTIFY_RESUME;
    private static NotifyType mNotificationType = NotifyType.NOTIFY_RESUME;
    private static LinkedList<Uri> mSuspendedNotifications = new LinkedList<>();
    private final static HashSet<Uri> mSuspendedNotificationsSet = new HashSet<>();

    public static void Initialize(String applicationId) {
        BaseContract.Initialize(applicationId, new BaseProvider.INotifier() {
            @Override
            public void notifyChange(@NonNull Context context, @NonNull Uri uri,
                                     @Nullable ContentObserver observer, boolean syncToNetwork) {
                NotifyChange(context, uri);
            }
        });
    }

    public static boolean IsNotifyAllowed() {
        return mNotificationType == NotifyType.NOTIFY_RESUME;
    }

    private static void NotifyChange(@NonNull Context context, @NonNull Uri uri) {
        switch (mNotificationType) {
            case NOTIFY_IGNORE: break;
            case NOTIFY_SUSPEND: {
                synchronized (mSuspendedNotificationsSet) { // Must be thread-safe
                    if (mSuspendedNotificationsSet.contains(uri)) {
                        // In case the URI is in the queue already, move it to the end.
                        // This could lead to side effects because the order is changed
                        // but we also reduce the number of outstanding notifications.
                        mSuspendedNotifications.remove(uri);
                    }
                    mSuspendedNotifications.add(uri);
                    mSuspendedNotificationsSet.add(uri);
                }
            } break;
            case NOTIFY_RESUME: {
                context.getContentResolver().notifyChange(uri, null, false);
            } break;
        }
    }

    private static int SetNotificationsIgnored(boolean ignored) {
        if( ignored ) {
            mPreviousNotificationType = mNotificationType;
            mNotificationType = NotifyType.NOTIFY_IGNORE;
        } else {
            mNotificationType = mPreviousNotificationType;
            mPreviousNotificationType = NotifyType.NOTIFY_RESUME;
        }
        return 1;
    }

    private static void NotifyOutstandingChanges(Context context) {
        Uri uri;
        assert context != null;
        final ContentResolver resolver = context.getContentResolver();
        while ((uri = mSuspendedNotifications.poll()) != null) {
            resolver.notifyChange(uri, null, false);
            mSuspendedNotificationsSet.remove(uri);
        }
    }

    private static int SetNotificationsSuspended(Context context, boolean suspended) {
        if( suspended ) {
            mPreviousNotificationType = mNotificationType;
            mNotificationType = NotifyType.NOTIFY_SUSPEND;
        } else {
            mNotificationType = mPreviousNotificationType;
            mPreviousNotificationType = NotifyType.NOTIFY_RESUME;
        }
        if (mNotificationType == NotifyType.NOTIFY_RESUME) {
            NotifyOutstandingChanges(context);
        }
        return 1;
    }

    public static void PopulateProviderContracts(IProviderContract.IPopulator populator) {
        populator.addProviderContract(mIgnoreProvider, IGNORE);
        populator.addProviderContract(mSuspendProvider, SUSPEND);
    }

    private static IProviderContract mIgnoreProvider = new SwitchProviderContract() {

        @Override
        public Uri switchOn(Context context) {
            SetNotificationsIgnored(true);
            return Uri.parse(IGNORE_URI + "/true");
        }

        @Override
        public int switchOff(Context context) {
            return SetNotificationsIgnored(false);
        }

    };

    private static IProviderContract mSuspendProvider = new SwitchProviderContract() {

        @Override
        public Uri switchOn(Context context) {
            SetNotificationsSuspended(context, true);
            return Uri.parse(SUSPEND_URI + "/true");
        }

        @Override
        public int switchOff(Context context) {
            return SetNotificationsSuspended(context, false);
        }

    };

}
