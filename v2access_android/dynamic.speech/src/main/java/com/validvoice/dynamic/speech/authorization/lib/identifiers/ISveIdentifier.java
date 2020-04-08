package com.validvoice.dynamic.speech.authorization.lib.identifiers;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.audio.AudioCodec;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.dynamic.speech.authorization.SveIdentifier;
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
import java.util.UUID;

public class ISveIdentifier {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "ISveIdentifier";

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
    private String mResolvedClientId = null;
    private String mSessionId = null;
    private Boolean mIsSessionOpen = false;
    private Boolean mIsSessionClosing = false;
    private HashMap<Integer, SveIdentifier.Profile> mProfiles = new HashMap<>();
    private HashMap<String, String> mMetaData = new HashMap<>();
    private AudioCodec mCodec = AudioCodec.Unknown;
    private double mScore = 0.0;
    private double mSpeechRequired = 0.0;
    private double mSpeechExtracted = 0.0;
    private SveIdentifier.Result mResult = SveIdentifier.Result.Invalid;
    private HashMap<String, SveIdentifier.InstanceResult> mResults = new HashMap<>();
    private int mTotalProcessCalls = 0;
    private long mTotalBytesSent = 0;
    private ArrayList<ContentPart> mContentParts = new ArrayList<>();
    private Boolean mLivenessRequired = false;
    private Boolean mIsAlive = false;

    ///
    /// Constructor
    ///

    ISveIdentifier() {
    }

    ///
    /// Public Properties
    ///

    public void setInteractionId(String iid) {
        mInteractionId = iid;
        if(iid != null && !iid.isEmpty()) {
            mWebAgent.getExtraHeaders().put("Interaction-Id", iid);
        } else {
            mWebAgent.getExtraHeaders().remove("Interaction-Id");
        }
    }

    public String getInteractionId() {
        return mInteractionId;
    }

    public void setInteractionTag(String tag) {
        mInteractionTag = tag;
        if(tag != null && !tag.isEmpty()) {
            mWebAgent.getExtraHeaders().put("Interaction-Tag", tag);
        } else {
            mWebAgent.getExtraHeaders().remove("Interaction-Tag");
        }
    }

    public String getInteractionTag() {
        return mInteractionTag;
    }

    public String getResolvedClientId() {
        return mResolvedClientId;
    }

    public String getSessionId() {
        return mSessionId;
    }

    public Boolean isSessionOpen() {
        return mIsSessionOpen;
    }

    public Boolean isSessionClosing() {
        return mIsSessionClosing;
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

    public HashMap<Integer, SveIdentifier.Profile> getProfiles() {
        return mProfiles;
    }

    public HashMap<String, String> getMetaData() {
        return mMetaData;
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

    public SveIdentifier.Result getRawIdentifierResult() {
        return mResult;
    }

    public SveIdentifier.Result getIdentifierResult() {
        if(!mLivenessRequired)
        {
            return mResult;
        }
        else
        {
            switch(mResult)
            {
                case Pass: return mIsAlive ? SveIdentifier.Result.PassIsAlive : SveIdentifier.Result.PassNotAlive;
                case Ambiguous: return mIsAlive ? SveIdentifier.Result.AmbiguousIsAlive : SveIdentifier.Result.AmbiguousNotAlive;
                case Fail: return mIsAlive ? SveIdentifier.Result.FailIsAlive : SveIdentifier.Result.FailNotAlive;
                default: return mResult;
            }
        }
    }

    public HashMap<String, SveIdentifier.InstanceResult> getIdentifierResults() {
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
                    mResult == SveIdentifier.Result.Pass && !mLivenessRequired || mIsAlive;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Boolean isAlive() {
        return mIsAlive;
    }

    public Boolean hasRawResult() {
        return mResult == SveIdentifier.Result.Pass || mResult == SveIdentifier.Result.Ambiguous || mResult == SveIdentifier.Result.Fail;
    }

    public Boolean hasResult() {
        try {
            final int mimCallsToPass = SpeechApi.getMinimumVerifyCallsToPass();
            return (mimCallsToPass == 0 || getTotalProcessCalls() >= mimCallsToPass) &&
                    (mResult == SveIdentifier.Result.Pass || mResult == SveIdentifier.Result.Ambiguous
                            || mResult == SveIdentifier.Result.Fail);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    ///
    /// Public Methods
    ///

    public void reset() {
        resetIdentifier();
    }

    public Boolean append(File file) {
        SveAudioWavHttpBody body = SveAudioWavHttpBody.create(file);
        if(body != null) {
            String name = buildAudioName(getTotalProcessCalls());
            mContentParts.add(new ContentPart("data", name, body));
            return true;
        }
        return false;
    }

    public Boolean append(File file, SpeechContexts contexts) {
        if(append(file) && contexts != null) {
            mLivenessRequired = true;
            for(SpeechContext ctx : contexts) {
                mContentParts.add(new ContentPart("speech", SveJsonHttpBody.create(ctx.toMap())));
            }
            return true;
        }
        return false;
    }

    public Boolean append(byte[] audioBuffer) {
        String name = buildAudioName(getTotalProcessCalls());
        mContentParts.add(new ContentPart("data", name, SveAudioWavHttpBody.create(audioBuffer)));
        return true;
    }

    public Boolean append(byte[] audioBuffer, SpeechContexts contexts) {
        if(append(audioBuffer)) {
            if(contexts != null) {
                for (SpeechContext ctx : contexts) {
                    if(!ctx.getName().toLowerCase().equals("identity")) {
                        mLivenessRequired = true;
                    }
                    mContentParts.add(new ContentPart("speech", SveJsonHttpBody.create(ctx.toMap())));
                }
            }
            return true;
        }
        return false;
    }

    public SveIdentifier.Result post() {
        return SveIdentifier.Result.Invalid;
    }

    public Boolean post(SveIdentifier.PostCallback callback) {
        callback.onPostComplete(SveIdentifier.Result.Invalid);
        return false;
    }

    public SveIdentifier.Result end() {
        return SveIdentifier.Result.Invalid;
    }

    public Boolean end(final SveIdentifier.EndCallback callback) {
        return false;
    }

    public Boolean cancel(String reason) {
        return false;
    }

    public Boolean cancel(String reason, final SveIdentifier.CancelCallback callback) {
        return false;
    }

    ///
    /// Protected Methods
    ///

    SveWebAgent webAgent() {
        return mWebAgent;
    }

    private void resetIdentifier() {
        mCodec = AudioCodec.Unknown;
        mProfiles.clear();
        mResults.clear();
        mContentParts.clear();
        mMetaData.clear();
        mResult = SveIdentifier.Result.Invalid;
        mTotalProcessCalls = 0;
        mTotalBytesSent = 0;
        mSpeechRequired = 0.0;
        mSpeechExtracted = 0.0;
        mLivenessRequired = false;
        mIsAlive = false;
        mSessionId = "";
        mIsSessionClosing = false;
        mIsSessionOpen = false;
        mResolvedClientId = "";
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

    void updateClientId(String clientId) {
        mResolvedClientId = clientId;
    }

    void updateVerifyResult(SveIdentifier.Result result) {
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
        mIsAlive = isAlive;
    }

    ArrayList<ContentPart> getContentParts() {
        return mContentParts;
    }

    int addProfile(JsonObject o) {
        int index = o.get("index").getAsInt();
        if(!getProfiles().containsKey(index)) {
            SveIdentifier.Profile profile = new SveIdentifier.Profile();

            profile.Type = SveVerifier.getProfileType(o.get("type").getAsInt());
            profile.Codec = SveUtility.GetCodec(o.get("codec").getAsString());
            profile.PassThreshold = o.get("pass").getAsDouble();
            profile.FailThreshold = o.get("fail").getAsDouble();

            if (o.has("min_seconds_of_speech")) {
                profile.MinimumSecondsOfSpeech = o.get("min_seconds_of_speech").getAsDouble();
            } else {
                profile.MinimumSecondsOfSpeech = 0.0;
            }
            mSpeechRequired = profile.MinimumSecondsOfSpeech;

            getProfiles().put(index, profile);

            if (getCodec() == AudioCodec.Unknown) {
                mCodec = profile.Codec;
            }

            Log.d(TAG, "addProfile(): Type: " + profile.Type);
            Log.d(TAG, "addProfile(): Codec: " + profile.Codec);
            Log.d(TAG, "addProfile(): MinimumSecondsOfSpeech: " + profile.MinimumSecondsOfSpeech);
            Log.d(TAG, "addProfile(): PassThreshold: " + profile.PassThreshold);
            Log.d(TAG, "addProfile(): FailThreshold: " + profile.FailThreshold);
            Log.d(TAG, "addProfile(): Index: " + index);
        }
        return index;
    }

    void addResult(String name, JsonObject o) {
        SveIdentifier.InstanceResult result = new SveIdentifier.InstanceResult();

        result.ErrorCode = o.get("error").getAsInt();
        result.Score = o.get("score").getAsDouble();
        result.SpeechExtracted = o.get("seconds_extracted").getAsDouble();

        o.remove("error");
        o.remove("score");
        o.remove("seconds_extracted");

        int index = o.get("index").getAsInt();
        o.remove("index");

        SveIdentifier.Profile profile = getProfiles().get(index);

        if(o.has("status")) {
            char ch = o.get("status").getAsCharacter();
            switch(ch) {
                case 'P': result.Result = SveIdentifier.Result.Pass; break;
                case 'A': result.Result = SveIdentifier.Result.Ambiguous; break;
                case 'F': result.Result = SveIdentifier.Result.Fail; break;
                case 'N': result.Result = SveIdentifier.Result.NeedMore; break;
                default: result.Result = SveIdentifier.Result.Unknown; break;
            }
        } else if (result.Score == 0.0)  {
            result.Result = SveIdentifier.Result.NeedMore;
        } else if (result.Score >= profile.PassThreshold) {
            result.Result = SveIdentifier.Result.Pass;
        } else if (result.Score <= profile.FailThreshold) {
            result.Result = SveIdentifier.Result.Fail;
        } else {
            result.Result = SveIdentifier.Result.Ambiguous;
        }

        Log.d(TAG, "addResult()[" + name + "]: Index: " + index);
        Log.d(TAG, "addResult()[" + name + "]: Error: " + result.ErrorCode);
        Log.d(TAG, "addResult()[" + name + "]: Score: " + result.Score);
        Log.d(TAG, "addResult()[" + name + "]: SpeechExtracted: " + result.SpeechExtracted);
        Log.d(TAG, "addResult()[" + name + "]: Result: " + result.Result);

        for(Map.Entry<String, JsonElement> elem : o.entrySet()) {
            result.Extra.put(elem.getKey(), elem.getValue());
            Log.d(TAG, "addResult()[" + name + "]: " + elem.getKey() + ": " + elem.getValue().getAsString());
        }

        mResults.put(name, result);
    }

    ///
    /// Protected Static Functions
    ///

    static SveIdentifier.Result getErrorResult(int errorCode) {
        errorCode = errorCode >= 0 ? errorCode : -errorCode;
        switch (errorCode) {
            case 104: return SveIdentifier.Result.LimitReached;
            case 110: return SveIdentifier.Result.Unauthorized;
            case 403: return SveIdentifier.Result.NeedMore;
            case 410: return SveIdentifier.Result.NotFound;
            case 412: return SveIdentifier.Result.LimitReached;
            case 419: return SveIdentifier.Result.NotResolved;
            case 420: return SveIdentifier.Result.BadEnrollment;
        }
        return SveIdentifier.Result.Error;
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
        return builder.append(UUID.randomUUID().toString()).append("-")
                .append(getTotalProcessCalls()).append("-")
                .append(elementCount).append(".raw").toString();
    }

}
