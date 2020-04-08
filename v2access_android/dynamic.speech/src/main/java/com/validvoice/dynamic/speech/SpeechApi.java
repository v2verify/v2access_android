package com.validvoice.dynamic.speech;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

public class SpeechApi {

    public enum SpeechServer {
        TEST,
        PROD
    }

    public enum TransportProtocol {
        REST
    }

    public static class Configuration  {

        private Context mContext;

        private String mDeveloperKey;

        private String mApplicationKey;

        private String mServer;

        private int mMinimumVerifyCallsToPass = 0;

        // Value in Seconds
        private int mSocketReadTimeout = 0;

        private String mApplicationVersion;

        private TransportProtocol mTransportProtocol = TransportProtocol.REST;

        private boolean mUseSecureConnection = false;

        private ConnectivityManager mManager = null;

        private Configuration() {}

        private Configuration(Context context) {
            mContext = context;
            mManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }

    }

    public static class Builder {

        private Configuration mConfiguration;

        public Builder() {
            mConfiguration = new Configuration();
        }

        public Builder(Context context) {
            mConfiguration = new Configuration(context);
        }

        public String getDeveloperKey() {
            return mConfiguration.mDeveloperKey;
        }

        public Builder setDeveloperKey(String developerKey) {
            mConfiguration.mDeveloperKey = developerKey;
            return this;
        }

        public String getApplicationKey() {
            return mConfiguration.mApplicationKey;
        }

        public Builder setApplicationKey(String applicationKey) {
            mConfiguration.mApplicationKey = applicationKey;
            return this;
        }

        public String getServer() {
            return mConfiguration.mServer;
        }

        public Builder setServer(String server) {
            mConfiguration.mServer = server;
            return this;
        }

        public Builder setServer(SpeechServer server) {
            mConfiguration.mServer = server == SpeechServer.PROD ?
                    V2_ON_DEMAND_AWS_PROD_SERVER : V2_ON_DEMAND_AWS_TEST_SERVER;
            return this;
        }

        public int getMinimumVerifyCallsToPass() {
            return mConfiguration.mMinimumVerifyCallsToPass;
        }

        public Builder setMinimumVerifyCallsToPass(int minimumVerifyCallsToPass) {
            mConfiguration.mMinimumVerifyCallsToPass = minimumVerifyCallsToPass;
            return this;
        }

        public int getSocketReadTimeout() {
            return mConfiguration.mSocketReadTimeout;
        }

        public Builder setSocketReadTimeout(int socketReadTimeout) {
            mConfiguration.mSocketReadTimeout = socketReadTimeout;
            return this;
        }

        public String getApplicationVersion() {
            return mConfiguration.mApplicationVersion;
        }

        public Builder setApplicationVersion(String applicationVersion) {
            mConfiguration.mApplicationVersion = applicationVersion;
            return this;
        }

        public TransportProtocol getTransportProtocol() {
            return mConfiguration.mTransportProtocol;
        }

        public Builder setTransportProtocol(TransportProtocol protocol) {
            mConfiguration.mTransportProtocol = protocol;
            return this;
        }

        public Builder useSecureConnection() {
            mConfiguration.mUseSecureConnection = true;
            return this;
        }

        public Configuration build() {
            return mConfiguration;
        }

    }

    public static void initialize(@NonNull Configuration config) {
        if(config.mServer == null) {
            if(config.mTransportProtocol == TransportProtocol.REST) {
                config.mServer = V2_ON_DEMAND_AWS_TEST_SERVER;
            }
        }
        mConfiguration = config;
    }

    public static String getDeveloperKey() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mDeveloperKey;
    }

    public static String getApplicationKey() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mApplicationKey;
    }

    public static String getServer() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mServer;
    }

    public static int getMinimumVerifyCallsToPass() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mMinimumVerifyCallsToPass;
    }

    public static int getSocketReadTimeout() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mSocketReadTimeout;
    }

    public static String getApplicationVersion() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mApplicationVersion;
    }

    public static TransportProtocol getTransportProtocol() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mTransportProtocol;
    }

    public static boolean useSecureConnection() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mUseSecureConnection;
    }

    public static ConnectivityManager getConnectivityManager() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mManager;
    }

    public static Context getContext() throws Exception {
        if(mConfiguration == null) {
            throw new Exception("SpeechApi Not Initialized");
        }
        return mConfiguration.mContext;
    }

    private static final String V2_ON_DEMAND_AWS_PROD_SERVER = "http://www.v2ondemand.com:50102/";
    private static final String V2_ON_DEMAND_AWS_TEST_SERVER = "http://www.v2ondemand.com:50202/";
    private static Configuration mConfiguration;

    static {
        mConfiguration = null;
    }

}
