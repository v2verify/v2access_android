package com.validvoice.voxidem.scenes.subs.sve;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.validvoice.dynamic.db.ContractController;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.contracts.HistoryContract;
import com.validvoice.voxidem.db.models.HistoryModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class VerifyIntent {

    public enum VerifyMode {
        VerifyUnknown,
        VerifyQrCode,
        VerifyQrRequest,
        VerifyDevice,
        VerifyGuest,
        verifyMobileLogin
    }

    class VerifyInstance {
        private boolean mDirty = true;
        private long mAccountId = -1;
        private int mAccountUserId = -1;
        private int mMaxAttempts = 3;
        private long mDeviceId = -1;
        private String mQrIntentId = "";
        private String mDevice = "";
        private String mDeviceQrId = "";
        private String mAccount = "";
        private String mCompany = "";
        private boolean mIsHardware = false;
        private boolean mIsAccountLink = false;

        public String getQrIntent() {
            return mQrIntentId;
        }

        public String getAccountName() {
            return mAccount;
        }

        public long getAccountId() {
            return mAccountId;
        }

        public int getAccountUserId() {
            return mAccountUserId;
        }

        public String getCompanyName() {
            return mCompany;
        }

        public String getDeviceName() {
            return mDevice;
        }

        public long getDeviceId() {
            return mDeviceId;
        }

        public String getDeviceQrId() {
            return mDeviceQrId;
        }

        public long getMaxAttempts() {
            return mMaxAttempts;
        }

        public boolean getIsHardware() {
            return mIsHardware;
        }

        public boolean getIsLink() {
            return mIsAccountLink;
        }
    }

    private final VerifyMode mMode;
    private final String mUser;
    private final String mId;
    private final String mLC;
    private final List<String> mIntentIds;
    private final List<VerifyInstance> mInstances;
    private final Object mLock;
    private final ResolveIds mResolver;
    private String mDeviceQrId;
    private String mDeviceQrIp;
    private String mDeviceQrInfo;
    private int mMaxAttempts;

    public VerifyIntent(VerifyMode mode, String user) {
        mMode = mode;
        mUser = user;
        mId = "";
        mLC = "";
        mDeviceQrId = "";
        mDeviceQrInfo = "";
        mIntentIds = new ArrayList<>();
        mInstances = new ArrayList<>();
        mLock = new Object();
        mResolver = new ResolveIds(this);
        mMaxAttempts = 32;
    }

    public VerifyIntent(VerifyMode mode, String user, String id) {
        mMode = mode;
        mUser = user;
        mId = id;
        mLC = "";
        mDeviceQrId = "";
        mDeviceQrInfo = "";
        mIntentIds = new ArrayList<>();
        mInstances = new ArrayList<>();
        mLock = new Object();
        mResolver = new ResolveIds(this);
        mMaxAttempts = 32;
    }

    public VerifyIntent(VerifyMode mode, String user, String id, String languageCode) {
        mMode = mode;
        mUser = user;
        mId = id;
        mLC = languageCode;
        mDeviceQrId = "";
        mDeviceQrInfo = "";
        mIntentIds = new ArrayList<>();
        mInstances = new ArrayList<>();
        mLock = new Object();
        mResolver = new ResolveIds(this);
        mMaxAttempts = 32;
    }

    VerifyMode getVerifyMode() {
        return mMode;
    }

    String getVoxidemUser() {
        return mUser;
    }

    String getVoxidemId() {
        return mId;
    }

    String getLanguageCode() {
        return mLC;
    }

    List<VerifyInstance> getInstances() {
        return mInstances;
    }

    String getIntentIds() {
        String intentIds;
        synchronized (mLock) {
            intentIds = TextUtils.join(";", mIntentIds);
        }
        return intentIds;
    }

    List<String> getIntentIdList() {
        return mIntentIds;
    }

    public void setDeviceQrId(String deviceId, String deviceIp, String deviceInfo) {
        if(mMode == VerifyMode.VerifyQrRequest) {
            mDeviceQrId = deviceId;
            mDeviceQrIp = deviceIp;
            mDeviceQrInfo = deviceInfo;
        }
    }

    String getDeviceQrId() {
        return mDeviceQrId;
    }

    String getDeviceQrIp() {
        return mDeviceQrIp;
    }

    String getDeviceQrInfo() {
        return mDeviceQrInfo;
    }

    int getMaxAttempts() {
        return mMaxAttempts;
    }

    public void setCaptureInstance(String qrIntent, String accountName, String companyName, int maxAttempts) {
        synchronized (mLock) {
            mIntentIds.add(qrIntent);
            VerifyInstance instance = new VerifyInstance();
            instance.mQrIntentId = qrIntent;
            instance.mAccount = accountName;
            instance.mCompany = companyName;
            instance.mMaxAttempts = maxAttempts;
            if(mMode == VerifyMode.VerifyQrRequest && !mDeviceQrId.isEmpty()) {
                instance.mDeviceQrId = mDeviceQrId;
            }
            mInstances.clear();
            mInstances.add(instance);
            if(maxAttempts < mMaxAttempts) {
                mMaxAttempts = maxAttempts;
            }
        }
    }

    public void setCaptureInstance(String qrIntent, String accountName, int accountUserId, String companyName, int maxAttempts) {
        synchronized (mLock) {
            mIntentIds.add(qrIntent);
            VerifyInstance instance = new VerifyInstance();
            instance.mQrIntentId = qrIntent;
            instance.mAccount = accountName;
            instance.mAccountUserId = accountUserId;
            instance.mCompany = companyName;
            instance.mMaxAttempts = maxAttempts;
            if(mMode == VerifyMode.VerifyQrRequest && !mDeviceQrId.isEmpty()) {
                instance.mDeviceQrId = mDeviceQrId;
            }
            mInstances.clear();
            mInstances.add(instance);
            if(maxAttempts < mMaxAttempts) {
                mMaxAttempts = maxAttempts;
            }
        }
    }

    public void setHardware(boolean enable) {
        if(mInstances.size() == 0) return;
        mInstances.get(mInstances.size() - 1).mIsHardware = enable;
    }

    public void setLink(boolean enable) {
        if(mInstances.size() == 0) return;
        mInstances.get(mInstances.size() - 1).mIsAccountLink = enable;
    }

    public void addDeviceInstance(String qrIntent, String deviceName, String accountName, String companyName, int maxAttempts) {
        synchronized (mLock) {
            mIntentIds.add(qrIntent);
            VerifyInstance instance = new VerifyInstance();
            instance.mQrIntentId = qrIntent;
            instance.mDevice = deviceName;
            instance.mAccount = accountName;
            instance.mCompany = companyName;
            instance.mMaxAttempts = maxAttempts;
            mInstances.add(instance);
            if(maxAttempts < mMaxAttempts) {
                mMaxAttempts = maxAttempts;
            }
        }
    }

    public void resolveIds(ContractController cc) {
        if(mResolver.getStatus() == AsyncTask.Status.RUNNING)
            return;
        mResolver.doInBackground(cc);
    }

    void updateHistory(ContractController cc, String result, String sessionId,
                       String chosen, String captured) {
        new UpdateHistory(this).execute(new UpdateHistoryInfo(cc, result, sessionId, chosen, captured));
    }

    void updateHistory(ContractController cc,
                       String result, String chosen, String sessionId, String captured,
                       Boolean isBreakAttempt, Boolean isRecording, Boolean isBackgroundNoise,
                       String comments) {
        new UpdateHistory(this).execute(new UpdateHistoryInfo(
                cc, result, sessionId, chosen, captured, isBreakAttempt,
                isRecording, isBackgroundNoise, comments
        ));
    }

    private static class ResolveIds extends AsyncTask<ContractController, Void, Void> {

        private VerifyIntent mIntent;

        ResolveIds(VerifyIntent intent) {
            mIntent = intent;
        }

        private HashMap<String, Long> mResolvedIds = new HashMap<>();

        @Override
        protected Void doInBackground(ContractController... cc) {
            synchronized (mIntent.mLock) {
                for(VerifyInstance instance : mIntent.mInstances) {
                    if(instance.mDirty) {
                        instance.mDirty = false;
                        if(!instance.mDeviceQrId.isEmpty()) {
                            if(mResolvedIds.containsKey(instance.mDeviceQrId)) {
                                instance.mDeviceId = mResolvedIds.get(instance.mDeviceQrId);
                            } else {
                                instance.mDeviceId = cc[0].getId(DevicesContract.CONTENT_URI,
                                        DevicesContract.DEVICE_ID, instance.mDeviceQrId);
                                mResolvedIds.put(instance.mDeviceQrId, instance.mDeviceId);
                            }
                        } else if(!instance.mDevice.isEmpty()) {
                            if(mResolvedIds.containsKey(instance.mDevice)) {
                                instance.mDeviceId = mResolvedIds.get(instance.mDevice);
                            } else {
                                instance.mDeviceId = cc[0].getId(DevicesContract.CONTENT_URI,
                                        DevicesContract.DEVICE_NICKNAME, instance.mDevice);
                                mResolvedIds.put(instance.mDevice, instance.mDeviceId);
                            }
                        }
                        //instance.mAccountId = cc.getId(AccountsContract.CONTENT_URI,
                        // AccountsContract.ACCOUNT_USER_NAME, instance.mAccount);
                    }
                }
            }
            return null;
        }
    }

    private class UpdateHistoryInfo {
        final ContractController CC;
        final String SessionId;
        final String Result;
        final String Chosen;
        final String Captured;
        final Boolean HasFeedback;
        final Boolean FeedbackBreakAttempt;
        final Boolean FeedbackRecording;
        final Boolean FeedbackBackroundNoise;
        final String FeedbackComments;

        UpdateHistoryInfo(ContractController cc, String result, String sessionId,
                          String chosen, String captured) {
            CC = cc;
            Result = result;
            SessionId = sessionId;
            Chosen = chosen;
            Captured = captured;
            HasFeedback = false;
            FeedbackBreakAttempt = false;
            FeedbackRecording = false;
            FeedbackBackroundNoise = false;
            FeedbackComments = "";
        }

        UpdateHistoryInfo(ContractController cc,
                          String result, String sessionId, String chosen, String captured,
                          Boolean isBreakAttempt, Boolean isRecording, Boolean isBackgroundNoise,
                          String comments) {
            CC = cc;
            Result = result;
            SessionId = sessionId;
            Chosen = chosen;
            Captured = captured;
            HasFeedback = true;
            FeedbackBreakAttempt = isBreakAttempt;
            FeedbackRecording = isRecording;
            FeedbackBackroundNoise = isBackgroundNoise;
            FeedbackComments = comments;
        }
    }

    private static class UpdateHistory extends AsyncTask<UpdateHistoryInfo, Void, Void> {

        private VerifyIntent mIntent;

        UpdateHistory(VerifyIntent intent) {
            mIntent = intent;
        }

        private List<Long> mHandledIds = new ArrayList<>();

        @Override
        protected Void doInBackground(UpdateHistoryInfo... params) {
            final UpdateHistoryInfo info = params[0];
            switch(mIntent.getVerifyMode()) {
                case VerifyQrCode:
                case VerifyQrRequest: {
                    VerifyIntent.VerifyInstance instance = mIntent.getInstances().get(0);
                    HistoryModel model;
                    if(!instance.getIsLink()) {
                        model = HistoryModel.createVerificationQrRecord(
                                mIntent.getVoxidemUser(), instance.getAccountName(), instance.getAccountId(),
                                instance.getCompanyName()
                        );
                        model.setHardware(instance.getIsHardware());
                    } else {
                        model = HistoryModel.createVerificationQrLinkRecord(
                            mIntent.getVoxidemUser(), instance.getAccountName(),
                            instance.getCompanyName()
                        );
                    }
                    if(mIntent.getVerifyMode() == VerifyMode.VerifyQrRequest && !instance.mDeviceQrId.isEmpty()) {
                        model.setDevice(instance.mDeviceQrId, instance.mDeviceId);
                    }
                    model.setSessionId(info.SessionId);
                    model.setResult(info.Result);
                    model.setLivenessData(info.Chosen, info.Captured);
                    if(info.HasFeedback) {
                        model.setFeedback(
                                info.FeedbackBreakAttempt, info.FeedbackRecording,
                                info.FeedbackBackroundNoise, info.FeedbackComments
                        );
                    }
                    info.CC.insertModel(HistoryContract.CONTENT_URI, model);
                } break;
                case VerifyDevice: {
                    Calendar calendar = Calendar.getInstance();
                    for(VerifyIntent.VerifyInstance instance : mIntent.getInstances()) {
                        HistoryModel model = HistoryModel.createVerificationDeviceRecord(
                                mIntent.getVoxidemUser(), instance.getAccountName(), instance.getAccountId(),
                                instance.getCompanyName(), instance.getDeviceName(), instance.getDeviceId()
                        );
                        model.setSessionId(info.SessionId);
                        model.setResult(info.Result);
                        model.setLivenessData(info.Chosen, info.Captured);
                        if(info.HasFeedback) {
                            model.setFeedback(
                                    info.FeedbackBreakAttempt, info.FeedbackRecording,
                                    info.FeedbackBackroundNoise, info.FeedbackComments
                            );
                        }
                        info.CC.insertModel(HistoryContract.CONTENT_URI, model);
                        if(-1 == mHandledIds.indexOf(instance.getDeviceId())) {
                            info.CC.updateField(DevicesContract.CONTENT_URI, instance.getDeviceId(),
                                    DevicesContract.DEVICE_DATE_LAST_USED, Long.toString(calendar.getTimeInMillis()));
                            mHandledIds.add(instance.getDeviceId());
                        }
                    }
                } break;
                case VerifyGuest: {
                    VerifyIntent.VerifyInstance instance = mIntent.getInstances().get(0);
                    HistoryModel model = HistoryModel.createVerificationGuestRecord(
                            mIntent.getVoxidemUser(), instance.getAccountName(), instance.getCompanyName()
                    );
                    model.setSessionId(info.SessionId);
                    model.setResult(info.Result);
                    model.setLivenessData(info.Chosen, info.Captured);
                    if(info.HasFeedback) {
                        model.setFeedback(
                                info.FeedbackBreakAttempt, info.FeedbackRecording,
                                info.FeedbackBackroundNoise, info.FeedbackComments
                        );
                    }
                    info.CC.insertModel(HistoryContract.CONTENT_URI, model);
                } break;
            }
            return null;
        }
    }

}
