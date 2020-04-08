package com.validvoice.dynamic.speech.authorization.lib.verifiers;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.audio.AudioCodec;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.dynamic.speech.authorization.SveVerifier;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContext;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveAudioWavHttpBody;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveJsonHttpBody;
import com.validvoice.dynamic.speech.authorization.lib.utils.SveUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ISveVerifier {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "ISveVerifier";

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

    enum IsAlive {
        Untested,
        NotAlive,
        Alive
    }

    ///
    /// Private Variables
    ///
    private SveWebAgent mWebAgent = new SveWebAgent();
    private String mInteractionId = null;
    private String mInteractionTag = null;
    private String mClientId = null;
    private String mSessionId = null;
    private String mAuthToken = null;
    private Boolean mIsSessionOpen = false;
    private Boolean mIsSessionClosing = false;
    private Boolean mIsOverridable = false;
    private Boolean mIsAuthorized = false;
    private HashMap<Integer, SveVerifier.Profile> mProfiles = new HashMap<>();
    private HashMap<String, String> mMetaData = new HashMap<>();
    private HashMap<String, Object> mExtra = new HashMap<>();
    private AudioCodec mCodec = AudioCodec.Unknown;
    private double mScore = 0.0;
    private double mSpeechRequired = 0.0;
    private double mSpeechExtracted = 0.0;
    private SveVerifier.Result mResult = SveVerifier.Result.Invalid;
    private HashMap<String, SveVerifier.InstanceResult> mResults = new HashMap<>();
    private int mTotalProcessCalls = 0;
    private long mTotalBytesSent = 0;
    private ArrayList<ContentPart> mContentParts = new ArrayList<>();
    private Boolean mLivenessRequired = false;
    private IsAlive mIsAlive = IsAlive.Untested;

    ///
    /// Constructor
    ///

    ISveVerifier() {
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
        if(mIsOverridable) {
            mAuthToken = authToken;
        }
    }

    public String getAuthToken() {
        if(mIsOverridable) {
            return mAuthToken;
        }
        return null;
    }

    public void setClientId(String cid) {
        mClientId = cid;
    }

    public String getClientId() {
        return mClientId;
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

    public HashMap<String, String> getMetaData() {
        return mMetaData;
    }

    public HashMap<String, Object> getExtra() {
        return mExtra;
    }

    public HashMap<Integer, SveVerifier.Profile> getProfiles() {
        return mProfiles;
    }

    public AudioCodec getCodec() {
        return mCodec;
    }

    public double getSpeechExtracted() {
        return mSpeechExtracted;
    }

    public double getSpeechRequired() {
        return mSpeechRequired;
    }

    public int getSpeechProgress() {
        int progress = (int) Math.round((getSpeechExtracted() / getSpeechRequired()) * 100);
        return (progress < 0) ? 0 : ((progress >= 100) ? (hasEnoughSpeech() ? 100 : 99) : progress);
    }

    public Boolean hasEnoughSpeech() {
        return mSpeechExtracted >= mSpeechRequired;
    }

    public double getVerifyScore() {
        return mScore;
    }

    public SveVerifier.Result getRawVerifyResult() {
        return mResult;
    }

    public SveVerifier.Result getVerifyResult() {
        if(!mLivenessRequired)
        {
            return mResult;
        }
        else
        {
            switch(mResult)
            {
                case Pass: return isAlive() ? SveVerifier.Result.PassIsAlive : SveVerifier.Result.PassNotAlive;
                case Ambiguous: return isAlive() ? SveVerifier.Result.AmbiguousIsAlive : SveVerifier.Result.AmbiguousNotAlive;
                case Fail: return isAlive() ? SveVerifier.Result.FailIsAlive : SveVerifier.Result.FailNotAlive;
                case NeedMore:
                    switch (mIsAlive) {
                        case Untested:
                            return SveVerifier.Result.NeedMore;
                        case Alive:
                            return SveVerifier.Result.NeedMoreAlive;
                        case NotAlive:
                            return SveVerifier.Result.NeedMoreNotAlive;
                    }
            }
            return mResult;
        }
    }

    public HashMap<String, SveVerifier.InstanceResult> getVerifyResults() {
        return mResults;
    }

    public int getTotalProcessCalls() {
        return mTotalProcessCalls;
    }

    public long getTotalBytesSent() {
        return mTotalBytesSent;
    }

    public boolean isLivenessRequired() {
        return mLivenessRequired;
    }

    public Boolean isVerified() {
        try {
            final int mimCallsToPass = SpeechApi.getMinimumVerifyCallsToPass();
            return (mimCallsToPass == 0 || getTotalProcessCalls() >= mimCallsToPass) &&
                    mResult == SveVerifier.Result.Pass && !mLivenessRequired || isAlive();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Boolean isAlive() {
        return mIsAlive == IsAlive.Alive;
    }

    public Boolean isOverridable() {
        return mIsOverridable;
    }

    public Boolean isAuthorized() {
        return mIsAuthorized;
    }

    public Boolean hasRawResult() {
        return mResult == SveVerifier.Result.Pass || mResult == SveVerifier.Result.Ambiguous || mResult == SveVerifier.Result.Fail;
    }

    public Boolean hasResult() {
        try {
            final int mimCallsToPass = SpeechApi.getMinimumVerifyCallsToPass();
            return (mimCallsToPass == 0 || getTotalProcessCalls() >= mimCallsToPass) &&
                    (mResult == SveVerifier.Result.Pass || mResult == SveVerifier.Result.Ambiguous
                            || mResult == SveVerifier.Result.Fail);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    ///
    /// Public Methods
    ///

    public void setFeedback(boolean isBreakAttempt, boolean isRecording, boolean isBackgroundNoise, String comments) {

        comments = (comments != null && !comments.isEmpty())
            ? comments.substring(0, Math.min(comments.length(), 255))
            : "N/A";

        // Populate the MetaData using custom interface (old way of storing feedback)
        mMetaData.put("Feedback-BreakAttempt", isBreakAttempt ? "1" : "0");
        mMetaData.put("Feedback-Recording", isRecording ? "1" : "0");
        mMetaData.put("Feedback-BackgroundNoise", isBackgroundNoise ? "1" : "0");
        mMetaData.put("Feedback-Comments", comments);

        // Populate the MetaData using the MetaData interface (new way of adding meta information)
        setMetaData("Feedback-BreakAttempt", isBreakAttempt);
        setMetaData("Feedback-Recording", isRecording);
        setMetaData("Feedback-BackgroundNoise", isBackgroundNoise);
        setMetaData("Feedback-Comments", comments);
    }

    public SveVerifier.Profile prefetchProfile() {
        return new SveVerifier.Profile();
    }

    public Boolean prefetchProfile(final SveVerifier.ProfileCallback callback) {
        return false;
    }

    public Boolean start() {
        return false;
    }

    public Boolean start(final SveVerifier.StartCallback callback) {
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

    public Boolean append(File file, SpeechContexts contexts) {
        if(isSessionOpen()) {
            if(append(file)) {
                if(contexts != null) {
                    mLivenessRequired = true;
                    for (SpeechContext ctx : contexts) {
                        mContentParts.add(new ContentPart("speech", SveJsonHttpBody.create(ctx.toMap())));
                    }
                    return true;
                }
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

    public Boolean append(byte[] audioBuffer, SpeechContexts contexts) {
        if(isSessionOpen()) {
            if(append(audioBuffer)) {
                if(contexts != null) {
                    mLivenessRequired = true;
                    for (SpeechContext ctx : contexts) {
                        mContentParts.add(new ContentPart("speech", SveJsonHttpBody.create(ctx.toMap())));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public SveVerifier.Result post() {
        return SveVerifier.Result.Invalid;
    }

    public Boolean post(SveVerifier.PostCallback callback) {
        callback.onPostComplete(SveVerifier.Result.Invalid);
        return false;
    }

    public SveVerifier.Result post(File file) {
        if(isSessionOpen()) {
            if(append(file)) {
                return post();
            }
        }
        return SveVerifier.Result.Invalid;
    }

    public SveVerifier.Result post(File file, SpeechContexts contexts) {
        if(isSessionOpen()) {
            if(append(file, contexts)) {
                return post();
            }
        }
        return SveVerifier.Result.Invalid;
    }

    public SveVerifier.Result post(byte[] audioBuffer) {
        if(isSessionOpen()) {
            if(append(audioBuffer)) {
                return post();
            }
        }
        return SveVerifier.Result.Invalid;
    }

    public SveVerifier.Result post(byte[] audioBuffer, SpeechContexts contexts) {
        if(isSessionOpen()) {
            if(append(audioBuffer, contexts)) {
                return post();
            }
        }
        return SveVerifier.Result.Invalid;
    }

    public Boolean post(File file, SveVerifier.PostCallback callback) {
        if(isSessionOpen()) {
            if(append(file)) {
                return post(callback);
            }
        }
        return false;
    }

    public Boolean post(File file, SpeechContexts contexts, SveVerifier.PostCallback callback) {
        if(isSessionOpen()) {
            if(append(file, contexts)) {
                return post(callback);
            }
        }
        return false;
    }

    public Boolean post(byte[] audioBuffer, final SveVerifier.PostCallback callback) {
        if(isSessionOpen()) {
            if(append(audioBuffer)) {
                return post(callback);
            }
        }
        return false;
    }

    public Boolean post(byte[] audioBuffer, SpeechContexts contexts, final SveVerifier.PostCallback callback) {
        if(isSessionOpen()) {
            if(append(audioBuffer, contexts)) {
                return post(callback);
            }
        }
        return false;
    }

    public SveVerifier.Result end() {
        return SveVerifier.Result.Invalid;
    }

    public Boolean end(final SveVerifier.EndCallback callback) {
        return false;
    }

    public Boolean cancel(String reason) {
        return false;
    }

    public Boolean cancel(String reason, final SveVerifier.CancelCallback callback) {
        return false;
    }

    ///
    /// Protected Methods
    ///

    SveWebAgent webAgent() {
        return mWebAgent;
    }

    void resetVerifier() {
        mCodec = AudioCodec.Unknown;
        mProfiles.clear();
        mResults.clear();
        mContentParts.clear();
        mExtra.clear();
        mResult = SveVerifier.Result.Invalid;
        mTotalProcessCalls = 0;
        mTotalBytesSent = 0;
        mIsSessionOpen = false;
        mIsSessionClosing = false;
        mIsOverridable = false;
        mIsAuthorized = false;
        mSpeechRequired = 0.0;
        mSpeechExtracted = 0.0;
        mLivenessRequired = false;
        mIsAlive = IsAlive.Untested;
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

    void updateVerifyResult(SveVerifier.Result result) {
        mResult = result;
    }

    void updateVerifyScore(double score) {
        mScore = score;
    }

    void updateTotalProcessCalls() {
        ++mTotalProcessCalls;
    }

    void updateTotalBytesSent(long bytes) {
        mTotalBytesSent += bytes;
    }

    void updateIsAlive(boolean isAlive) {
        mIsAlive = isAlive ? IsAlive.Alive : IsAlive.NotAlive;
    }

    ArrayList<ContentPart> getContentParts() {
        return mContentParts;
    }

    int addProfile(JsonObject o) {
        SveVerifier.Profile profile = new SveVerifier.Profile();

        profile.Type = SveVerifier.getProfileType(o.get("type").getAsInt());
        profile.Codec = SveUtility.GetCodec(o.get("codec").getAsString());
        profile.PassThreshold = o.get("pass").getAsDouble();
        profile.FailThreshold = o.get("fail").getAsDouble();

        if(o.has("min_seconds_of_speech")) {
            profile.MinimumSecondsOfSpeech = o.get("min_seconds_of_speech").getAsDouble();
        } else {
            profile.MinimumSecondsOfSpeech = 0.0;
        }
        mSpeechRequired = profile.MinimumSecondsOfSpeech;

        int index = o.get("index").getAsInt();
        getProfiles().put(index, profile);

        if(getCodec() == AudioCodec.Unknown) {
            mCodec = profile.Codec;
        }

        Log.d(TAG, "addProfile(): Type: " + profile.Type);
        Log.d(TAG, "addProfile(): Codec: " + profile.Codec);
        Log.d(TAG, "addProfile(): MinimumSecondsOfSpeech: " + profile.MinimumSecondsOfSpeech);
        Log.d(TAG, "addProfile(): PassThreshold: " + profile.PassThreshold);
        Log.d(TAG, "addProfile(): FailThreshold: " + profile.FailThreshold);
        Log.d(TAG, "addProfile(): Index: " + index);

        return index;
    }

    void addResult(String name, JsonObject o) {
        SveVerifier.InstanceResult result = new SveVerifier.InstanceResult();

        result.ErrorCode = o.get("error").getAsInt();
        result.Score = o.get("score").getAsDouble();
        result.SpeechExtracted = o.get("seconds_extracted").getAsDouble();

        o.remove("error");
        o.remove("score");
        o.remove("seconds_extracted");

        int index = o.get("index").getAsInt();
        o.remove("index");

        SveVerifier.Profile profile = getProfiles().get(index);

        if(o.has("status")) {
            char ch = o.get("status").getAsCharacter();
            switch(ch) {
                case 'P': result.Result = SveVerifier.Result.Pass; break;
                case 'A': result.Result = SveVerifier.Result.Ambiguous; break;
                case 'F': result.Result = SveVerifier.Result.Fail; break;
                case 'N': result.Result = SveVerifier.Result.NeedMore; break;
                default: result.Result = SveVerifier.Result.Unknown; break;
            }
        } else if (result.Score == 0.0)  {
            result.Result = SveVerifier.Result.NeedMore;
        } else if (result.Score >= profile.PassThreshold) {
            result.Result = SveVerifier.Result.Pass;
        } else if (result.Score <= profile.FailThreshold) {
            result.Result = SveVerifier.Result.Fail;
        } else {
            result.Result = SveVerifier.Result.Ambiguous;
        }

        Log.d(TAG, "addResult()[" + name + "]: Index: " + index);
        Log.d(TAG, "addResult()[" + name + "]: Error: " + result.ErrorCode);
        Log.d(TAG, "addResult()[" + name + "]: Score: " + result.Score);
        Log.d(TAG, "addResult()[" + name + "]: SpeechExtracted: " + result.SpeechExtracted);
        Log.d(TAG, "addResult()[" + name + "]: Result: " + result.Result);

        if(o.has("overridable")) {
            mIsOverridable = o.get("overridable").getAsBoolean();
            Log.d(TAG, "addResult()[" + name + "]: IsOverridable: " + mIsOverridable);
            o.remove("overridable");
        }

        if(o.has("authorized")) {
            mIsAuthorized = o.get("authorized").getAsBoolean();
            Log.d(TAG, "addResult()[" + name + "]: IsAuthorized: " + mIsAuthorized);
            o.remove("authorized");
        }

        for(Map.Entry<String, JsonElement> elem : o.entrySet()) {
            result.Extra.put(elem.getKey(), elem.getValue());
            Log.d(TAG, "addResult()[" + name + "]: " + elem.getKey() + ": " + elem.getValue().getAsString());
        }

        mResults.put(name, result);
    }

    ///
    /// Protected Static Functions
    ///

    static SveVerifier.Result getErrorResult(int errorCode) {
        errorCode = errorCode >= 0 ? errorCode : -errorCode;
        switch (errorCode) {
            case 104: return SveVerifier.Result.LimitReached;
            case 110: return SveVerifier.Result.Unauthorized;
            case 403: return SveVerifier.Result.NeedMore;
            case 410: return SveVerifier.Result.NotFound;
            case 412: return SveVerifier.Result.LimitReached;
            case 420: return SveVerifier.Result.BadEnrollment;
            case 430: return SveVerifier.Result.LockedOut;
        }
        return SveVerifier.Result.Error;
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
