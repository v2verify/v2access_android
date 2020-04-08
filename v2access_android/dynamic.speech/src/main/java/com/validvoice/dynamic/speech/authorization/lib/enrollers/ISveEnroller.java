package com.validvoice.dynamic.speech.authorization.lib.enrollers;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.speech.authorization.SveEnroller;
import com.validvoice.dynamic.audio.AudioCodec;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveAudioWavHttpBody;
import com.validvoice.dynamic.speech.authorization.lib.utils.SveUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ISveEnroller {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "ISveEnroller";

    ///
    /// Private Classes
    ///

    class ContentPart {

        String Name;
        String FileName;
        SveHttpBody Data;

        ContentPart(String name, SveHttpBody data) {
            Name = name;
            FileName = name;
            Data = data;
        }

        ContentPart(String name, String fileName, SveHttpBody data) {
            Name = name;
            FileName = fileName;
            Data = data;
        }

    }

    ///
    /// Private Variables
    ///
    private SveWebAgent mWebAgent = new SveWebAgent();
    private String mInteractionId = null;
    private String mInteractionTag = null;
    private String mClientId = null;
    private SveEnroller.Gender mGender = SveEnroller.Gender.Unknown;
    private String mSessionId = null;
    private String mAuthToken = null;
    private Boolean mIsSessionOpen = false;
    private Boolean mIsSessionClosing = false;
    private HashMap<Integer, SveEnroller.Profile> mProfiles = new HashMap<>();
    private HashMap<String, String> mMetaData = new HashMap<>();
    private HashMap<String, Object> mExtra = new HashMap<>();
    private AudioCodec mCodec = AudioCodec.Unknown;
    private double mSpeechRequired = 0.0;
    private double mSpeechExtracted = 0.0;
    private double mSpeechTrained = 0.0;
    private SveEnroller.Result mResult = SveEnroller.Result.Unknown;
    private HashMap<String, SveEnroller.InstanceResult> mResults = new HashMap<>();
    private int mTotalProcessCalls = 0;
    private long mTotalBytesSent = 0;
    private ArrayList<ContentPart> mContentParts = new ArrayList<>();

    ///
    /// Constructor
    ///

    ISveEnroller() {
    }

    ///
    /// Public Properties
    ///

    public void setInteractionId(String iid) {
        mInteractionId = iid;
    }

    public String getInteractionId() {
        return mInteractionId;
    }

    public void setInteractionTag(String tag) {
        mInteractionTag = tag;
    }

    public String getInteractionTag() {
        return mInteractionTag;
    }

    public void setAuthToken(String authToken) {
        mAuthToken = authToken;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public void setClientId(String cid) {
        mClientId = cid;
    }

    public String getClientId() {
        return mClientId;
    }

    public void setGender(SveEnroller.Gender gender) {
        mGender = gender;
    }

    public SveEnroller.Gender getGender() {
        return mGender;
    }

    public void setMetaData(String name, boolean value) {
        mMetaData.put("Meta-" + name, value ? "1" : "0");
    }

    public void setMetaData(String name, int value) {
        mMetaData.put("Meta-" + name, "" + value + "");
    }

    public void setMetaData(String name, double value) {
        mMetaData.put("Meta-" + name, "" + value + "");
    }

    public void setMetaData(String name, String value) {
        mMetaData.put("Meta-" + name, value);
    }

    ///
    /// Public Getters
    ///

    public String getSessionId() {
        return mSessionId;
    }

    public Boolean isSessionOpen() {
        return mIsSessionOpen;
    }

    public Boolean isSessionClosing() {
        return mIsSessionClosing;
    }

    public HashMap<Integer, SveEnroller.Profile> getProfiles() {
        return mProfiles;
    }

    public HashMap<String, String> getMetaData() {
        return mMetaData;
    }

    public HashMap<String, Object> getExtra() {
        return mExtra;
    }

    public AudioCodec getCodec() {
        return mCodec;
    }

    public double getSpeechRequired() {
        return mSpeechRequired;
    }

    public double getSpeechExtracted() {
        return mSpeechExtracted;
    }

    public double getSpeechTrained() {
        return mSpeechTrained;
    }

    public SveEnroller.Result getEnrollResult() {
        return mResult;
    }

    public HashMap<String, SveEnroller.InstanceResult> getEnrollResults() {
        return mResults;
    }

    public int getTotalProcessCalls() {
        return mTotalProcessCalls;
    }

    public long getTotalBytesSent() {
        return mTotalBytesSent;
    }

    public Boolean hasEnoughSpeech() {
        return mSpeechExtracted >= mSpeechRequired;
    }

    public Boolean isTrained() {
        return mSpeechTrained >= mSpeechRequired;
    }

    ///
    /// Public Methods
    ///

    public SveEnroller.Profile prefetchProfile() {
        return new SveEnroller.Profile();
    }

    public Boolean prefetchProfile(final SveEnroller.ProfileCallback callback) {
        return false;
    }

    public Boolean start() {
        return false;
    }

    public Boolean start(final SveEnroller.StartCallback callback) {
        return false;
    }

    public Boolean append(File file) {
        if(isSessionOpen()) {
            SveAudioWavHttpBody body = SveAudioWavHttpBody.create(file);
            if(body != null) {
                String name = buildAudioName(getTotalProcessCalls());
                mContentParts.add(new ContentPart("data", name, body));
                return true;
            }
        }
        return false;
    }

    public Boolean append(byte[] audioBuffer) {
        if(isSessionOpen()) {
            String name = buildAudioName(getTotalProcessCalls());
            mContentParts.add(new ContentPart("data", name, SveAudioWavHttpBody.create(audioBuffer)));
            return true;
        }
        return false;
    }

    public SveEnroller.Result post() {
        return SveEnroller.Result.Invalid;
    }

    public Boolean post(SveEnroller.PostCallback callback) {
        callback.onPostComplete(SveEnroller.Result.Invalid);
        return false;
    }

    public SveEnroller.Result post(File file) {
        if(isSessionOpen()) {
            if(append(file)) {
                return post();
            }
        }
        return SveEnroller.Result.Invalid;
    }

    public SveEnroller.Result post(byte[] audioBuffer) {
        if(isSessionOpen()) {
            if(append(audioBuffer)) {
                return post();
            }
        }
        return SveEnroller.Result.Invalid;
    }

    public Boolean post(File file, SveEnroller.PostCallback callback) {
        if(isSessionOpen()) {
            if(append(file)) {
                return post(callback);
            }
        }
        return false;
    }

    public Boolean post(byte[] audioBuffer, final SveEnroller.PostCallback callback) {
        if(isSessionOpen()) {
            if(append(audioBuffer)) {
                return post(callback);
            }
        }
        return false;
    }

    public SveEnroller.Result end() {
        return SveEnroller.Result.Invalid;
    }

    public Boolean end(final SveEnroller.EndCallback callback) {
        return false;
    }

    public Boolean cancel(String reason) {
        return false;
    }

    public Boolean cancel(String reason, final SveEnroller.CancelCallback callback) {
        return false;
    }

    ///
    /// Protected Methods
    ///

    SveWebAgent webAgent() {
        return mWebAgent;
    }

    void resetEnroller() {
        mCodec = AudioCodec.Unknown;
        mProfiles.clear();
        mResults.clear();
        mContentParts.clear();
        mExtra.clear();
        mMetaData.clear();
        mResult = SveEnroller.Result.Unknown;
        mTotalProcessCalls = 0;
        mTotalBytesSent = 0;
        mIsSessionOpen = false;
        mIsSessionClosing = false;
        mSessionId = "";
        webAgent().clear();
    }

    void updateSessionId(String sessionId) {
        mSessionId = sessionId;
    }

    void updateSessionStatus(boolean status) {
        mIsSessionOpen = status;
    }

    void updateSessionClosing(boolean status) {
        mIsSessionClosing = status;
    }

    void updateSpeechExtracted(double extracted) {
        mSpeechExtracted = extracted;
    }

    void updateSpeechTrained(double trained) {
        mSpeechTrained = trained;
    }

    void updateEnrollResult(SveEnroller.Result result) {
        mResult = result;
    }

    void updateTotalProcessCalls() {
        ++mTotalProcessCalls;
    }

    void updateTotalBytesSent(long bytes) {
        mTotalBytesSent += bytes;
    }

    ArrayList<ContentPart> getContentParts() {
        return mContentParts;
    }

    int addProfile(JsonObject o) {
        SveEnroller.Profile profile = new SveEnroller.Profile();

        profile.Codec = SveUtility.GetCodec(o.get("codec").getAsString());
        profile.MinimumSecondsOfSpeech = o.get("min_seconds_of_speech").getAsDouble();

        mSpeechRequired = profile.MinimumSecondsOfSpeech;

        int index = o.get("index").getAsInt();
        getProfiles().put(index, profile);

        if(getCodec() == AudioCodec.Unknown) {
            mCodec = profile.Codec;
        }

        Log.d(TAG, "addProfile(): Codec: " + profile.Codec);
        Log.d(TAG, "addProfile(): MinimumSecondsOfSpeech: " + profile.MinimumSecondsOfSpeech);
        Log.d(TAG, "addProfile(): Index: " + index);

        return index;
    }

    void addResult(String name, JsonObject o) {
        SveEnroller.InstanceResult result = new SveEnroller.InstanceResult();

        result.ErrorCode = o.get("error").getAsInt();
        result.Result = getErrorResult(result.ErrorCode);
        o.remove("error");

        int index = o.get("index").getAsInt();
        o.remove("index");

        if(o.has("seconds_extracted")) {
            result.SpeechExtracted = o.get("seconds_extracted").getAsDouble();
            result.SpeechTrained = 0.0;
            o.remove("seconds_extracted");
        } else if(o.has("seconds_trained")) {
            result.SpeechExtracted = 0.0;
            result.SpeechTrained = 0.0;
            if(result.Result == SveEnroller.Result.Success) {
                result.SpeechTrained = o.get("seconds_trained").getAsDouble();
            }
            o.remove("seconds_trained");
        } else {
            result.Result = SveEnroller.Result.Invalid;
        }

        Log.d(TAG, "addResult()[" + name + "]: Error: " + result.ErrorCode);
        Log.d(TAG, "addResult()[" + name + "]: Index: " + index);
        Log.d(TAG, "addResult()[" + name + "]: SpeechExtracted: " + result.SpeechExtracted);
        Log.d(TAG, "addResult()[" + name + "]: SpeechTrained: " + result.SpeechTrained);
        Log.d(TAG, "addResult()[" + name + "]: Result: " + result.Result);

        for(Map.Entry<String, JsonElement> elem : o.entrySet()) {
            result.Extra.put(elem.getKey(), elem.getValue());
            Log.d(TAG, "addResult()[" + name + "]: " + elem.getKey() + ": " + elem.getValue().getAsString());
        }

        getEnrollResults().put(name, result);
    }

    ///
    /// Protected Static Functions
    ///

    static SveEnroller.Result getErrorResult(int errorCode) {
        errorCode = errorCode >= 0 ? errorCode : -errorCode;
        switch (errorCode) {
            case 0: return SveEnroller.Result.Success;
            case 104: return SveEnroller.Result.LimitReached;
            case 110: return SveEnroller.Result.Unauthorized;
            case 303: return SveEnroller.Result.NeedMore;
            case 311: return SveEnroller.Result.LimitReached;
            case 318: return SveEnroller.Result.AlreadyEnrolled;
            case 320: return SveEnroller.Result.TokenExists;
            case 321: return SveEnroller.Result.TokenRequired;
        }
        return SveEnroller.Result.Error;
    }

    ///
    /// Private Methods
    ///

    private String buildAudioName(int elementCount) {
        StringBuilder builder = new StringBuilder();
        String iid = getInteractionId();
        if(iid != null) {
            builder.append(iid).append("-");
        }
        return builder.append(getClientId()).append("-")
                .append(getTotalProcessCalls()).append("-")
                .append(elementCount).append(".raw").toString();
    }

}
