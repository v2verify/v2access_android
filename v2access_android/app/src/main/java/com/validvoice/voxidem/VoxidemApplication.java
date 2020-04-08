package com.validvoice.voxidem;

import android.util.Log;

import com.validvoice.dynamic.scene.SceneApplication;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.voxidem.cloud.ControlKeys;
import com.validvoice.voxidem.cloud.QrIntent;
import com.validvoice.voxidem.cloud.QrUserAccount;
import com.validvoice.voxidem.cloud.UserAccountDetails;
import com.validvoice.voxidem.cloud.UserDetails;
import com.validvoice.voxidem.cloud.UserDeviceDetails;
import com.validvoice.voxidem.cloud.UserIntent;
import com.validvoice.voxidem.cloud.UserError;
import com.validvoice.voxidem.cloud.UserLogin;

public class VoxidemApplication extends SceneApplication {
    private static final String TAG = "VoxidemApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        VoxidemPreferences.initialize(this);

        SpeechApi.Builder builder = new SpeechApi.Builder(this);

        builder.setDeveloperKey(getString(R.string.sve_dev_key))
                .setApplicationKey(getString(R.string.sve_app_key))
                .setApplicationVersion(BuildConfig.VERSION_NAME)
                .setServer(getString(R.string.sve_server))
                .setTransportProtocol(SpeechApi.TransportProtocol.REST);
               // .useSecureConnection();

        SpeechApi.initialize(builder.build());

        CloudController.initialize(CloudController.Protocol.REST, CloudController.Version.Version_1);
        CloudController.registerCloudObjectFactory(new QrIntent.Factory());
        CloudController.registerCloudObjectFactory(new QrUserAccount.Factory());
        CloudController.registerCloudObjectFactory(new UserError.Factory());
        CloudController.registerCloudObjectFactory(new UserIntent.Factory());
        CloudController.registerCloudObjectFactory(new UserLogin.Factory());
        CloudController.registerCloudObjectFactory(new UserDetails.Factory());
        CloudController.registerCloudObjectFactory(new UserAccountDetails.Factory());
        CloudController.registerCloudObjectFactory(new UserDeviceDetails.Factory());
        CloudController.registerCloudObjectFactory(new ControlKeys.Factory());

    }
}
