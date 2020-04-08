package com.validvoice.dynamic.cloud;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.validvoice.dynamic.cloud.protocols.rest.RestCloudController;

public class CloudController {

    public enum Protocol {
        Unknown,
        REST,
        TCMP
    }

    public enum Version {
        Unknown,
        Version_1
    }

    public interface ResponseCallback {

        void onResult(CloudResult result);

        void onError(CloudError error);

        void onFailure(Exception ex);

    }

    public interface ResponseOnUiCallback {

        void onResult(CloudResult result);

        void onError(CloudError error);

        void onFailure(Exception ex);

    }

    private static ICloudController mCloudController;
    private static Version mVersion;

    public static void initialize(Protocol protocol, Version version) {
        if(mCloudController == null) {
            ICloudController controller;
            if(protocol == Protocol.REST) {
                controller = new RestCloudController();
            } else if(protocol == Protocol.TCMP) {
                throw new IllegalArgumentException("TCMP Not Yet Implemented");
            } else {
                throw new IllegalArgumentException("Unable to set Unknown Transport Protocol");
            }
            initialize(controller, version);
        }
    }

    public static void initialize(@NonNull ICloudController controller, Version version) {
        if(mCloudController == null) {
            mCloudController = controller;
            if(version == Version.Unknown) {
                throw new IllegalArgumentException("Unable to set Unknown Transport Version");
            } else {
                mVersion = version;
            }
            mCloudController.registerCloudObjectFactory(new CloudResult.Factory());
            mCloudController.registerCloudObjectFactory(new CloudError.Factory());
            mCloudController.registerCloudObjectFactory(new CloudArray.Factory());
            mCloudController.registerCloudObjectFactory(new CloudJsonObject.Factory());
        }
    }

    static boolean sendMessage(@NonNull CloudMessage message, @NonNull final ResponseCallback callback) {
        return mCloudController.sendMessage(message.mCloudMessage, callback);
    }

    static boolean sendMessage(@NonNull CloudMessage message, @NonNull Context context, @NonNull final ResponseOnUiCallback callback) {
        final Activity activity = (Activity)context;
        return mCloudController.sendMessage(message.mCloudMessage, new ResponseCallback() {

            @Override
            public void onResult(final CloudResult result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResult(result);
                    }
                });
            }

            @Override
            public void onError(final CloudError error) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(error);
                    }
                });
            }

            @Override
            public void onFailure(final Exception ex) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(ex);
                    }
                });
            }
        });
    }

    public static void registerCloudObjectFactory(@NonNull ICloudObject.IFactory factory) {
        mCloudController.registerCloudObjectFactory(factory);
    }

    static ICloudMessage createCloudMessage() {
        return mCloudController.createCloudMessage();
    }

    static ICloudImageLoader createCloudImageLoader() {
        return mCloudController.createCloudImageLoader();
    }

    static Version defaultVersion() {
        return mVersion;
    }

}
