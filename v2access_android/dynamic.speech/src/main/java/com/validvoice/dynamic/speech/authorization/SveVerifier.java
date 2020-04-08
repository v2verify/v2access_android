package com.validvoice.dynamic.speech.authorization;

import android.support.annotation.NonNull;
import android.util.Log;

import com.validvoice.dynamic.audio.AudioCodec;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;
import com.validvoice.dynamic.speech.authorization.lib.utils.SveFailureCallback;
import com.validvoice.dynamic.speech.authorization.lib.verifiers.ISveVerifier;
import com.validvoice.dynamic.speech.authorization.lib.verifiers.SveVerifier_1;

import java.io.File;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class SveVerifier {

    public enum Result {

        Pass,

        PassIsAlive,

        PassNotAlive,

        Ambiguous,

        AmbiguousIsAlive,

        AmbiguousNotAlive,

        Fail,

        FailIsAlive,

        FailNotAlive,

        NeedMore,

        NeedMoreAlive,

        NeedMoreNotAlive,

        LimitReached,

        Unauthorized,

        NotFound,

        BadEnrollment,

        Timeout,

        Invalid,

        Error,

        Unknown,

        LockedOut

    }

    public enum ProfileType {

        Single,

        // Presently Not Supported on Android
        Batch,

        // Presently Not Supported on Android
        Multi,

        // Presently Not Supported on Android
        Group,

        DropOne,

        // Presently Not Supported on Android
        Recognition,

        // Presently Not Supported on Android
        Monitor

    }

    public static class Profile {

        public AudioCodec Codec;

        public SveVerifier.ProfileType Type;

        public double MinimumSecondsOfSpeech;

        public double PassThreshold;

        public double FailThreshold;

    }

    public static class InstanceResult {

        public InstanceResult() {
            Extra = new HashMap<>();
        }

        public SveVerifier.Result Result;

        public double SpeechExtracted;

        public double Score;

        public int ErrorCode;

        public HashMap<String, Object> Extra;

    }

    public static class VerifyException extends Exception {

        private Result mResult = Result.Error;

        public VerifyException() {
        }

        public VerifyException(String msg) {
            super(msg);
        }

        public VerifyException(Result result) {
            super("Verify Result: " + result);
        }

        public VerifyException(String msg, Result result) {
            super(msg + ", Verify Result: " + result);
            mResult = result;
        }

        public Result getResult() {
            return mResult;
        }
    }

    public interface ProfileCallback extends SveFailureCallback {
        void onProfileComplete(Profile profile);
    }

    public interface StartCallback extends SveFailureCallback {
        void onStartComplete();
    }

    public interface PostCallback extends SveFailureCallback {
        void onPostComplete(Result result);
    }

    public interface EndCallback extends SveFailureCallback {
        void onEndComplete(boolean isAuthorized);
    }

    public interface CancelCallback extends SveFailureCallback {
        void onCancelComplete();
    }

    ///
    /// Constructors
    ///

    public SveVerifier() {
        mVerifier = new SveVerifier_1();
    }

    ///
    /// Public Properties
    ///

    public SveVerifier setInteractionId(String iid) {
        mVerifier.setInteractionId(iid);
        return this;
    }

    public String getInteractionId() {
        return mVerifier.getInteractionId();
    }

    public SveVerifier setInteractionTag(String tag) {
        mVerifier.setInteractionTag(tag);
        return this;
    }

    public String getInteractionTag() {
        return mVerifier.getInteractionTag();
    }

    public SveVerifier setClientId(String cid) {
        mVerifier.setClientId(cid);
        return this;
    }

    public String getClientId() {
        return mVerifier.getClientId();
    }

    public SveVerifier setAuthToken(String authToken) {
        mVerifier.setAuthToken(authToken);
        return this;
    }

    public String getAuthToken() {
        return mVerifier.getAuthToken();
    }

    public void setFeedback(boolean isBreakAttempt, boolean isRecording, boolean isBackgroundNoise, String comments) {
        mVerifier.setFeedback(isBreakAttempt, isRecording, isBackgroundNoise, comments);
    }

    public void setMetaData(String name, boolean value) {
        mVerifier.setMetaData(name, value);
    }

    public void setMetaData(String name, int value) {
        mVerifier.setMetaData(name, value);
    }

    public void setMetaData(String name, double value) {
        mVerifier.setMetaData(name, value);
    }

    public void setMetaData(String name, String value) {
        mVerifier.setMetaData(name, value);
    }

    ///
    /// Public Getters
    ///

    public String getSessionId() {
        return mVerifier.getSessionId();
    }

    public Boolean isSessionOpen() {
        return mVerifier.isSessionOpen();
    }

    public Boolean isSessionClosing() {
        return mVerifier.isSessionClosing();
    }

    public Boolean isOverridable() {
        return mVerifier.isOverridable();
    }

    public Boolean isAuthorized() {
        return mVerifier.isAuthorized();
    }

    public HashMap<Integer, Profile> getProfiles() {
        return mVerifier.getProfiles();
    }

    public HashMap<String, Object> getExtra() {
        return mVerifier.getExtra();
    }

    public AudioCodec getCodec() {
        return mVerifier.getCodec();
    }

    public double getVerifyScore() {
        return mVerifier.getVerifyScore();
    }

    public double getSpeechExtracted() {
        return mVerifier.getSpeechExtracted();
    }

    public double getSpeechRequired() {
        return mVerifier.getSpeechRequired();
    }

    public int getSpeechProgress() {
        return mVerifier.getSpeechProgress();
    }

    public boolean hasEnoughSpeech() {
        return mVerifier.hasEnoughSpeech();
    }

    public Result getRawVerifyResult() {
        return mVerifier.getRawVerifyResult();
    }

    public Result getVerifyResult() {
        return mVerifier.getVerifyResult();
    }

    public HashMap<String, InstanceResult> getVerifyResults() {
        return mVerifier.getVerifyResults();
    }

    public int getTotalProcessCalls() {
        return mVerifier.getTotalProcessCalls();
    }

    public long totalBytesSent() {
        return mVerifier.getTotalBytesSent();
    }

    public Boolean isLivenessRequired() {
        return mVerifier.isLivenessRequired();
    }

    public Boolean isVerified() {
        return mVerifier.isVerified();
    }

    public Boolean isAlive() {
        return mVerifier.isAlive();
    }

    public Boolean hasRawResult() {
        return mVerifier.hasRawResult();
    }

    public Boolean hasResult() {
        return mVerifier.hasResult();
    }

    ///
    /// Public Methods
    ///

    // Pre-fetch the verify speech profile. (Synchronously)
    public Profile prefetchProfile() {
        return mVerifier.prefetchProfile();
    }

    // Pre-fetch the verify speech profile. (Asynchronously)
    public Boolean prefetchProfile(ProfileCallback callback) {
        return mVerifier.prefetchProfile(callback);
    }

    // Start a Verify Session. (Synchronously)
    public Boolean start() {
        return mVerifier.start();
    }

    // Start a Verify Session. (Asynchronously)
    public Boolean start(StartCallback callback) {
        return mVerifier.start(callback);
    }

    // Append a Wav file with speech to the outbound queue.
    public Boolean append(File file) {
        return mVerifier.append(file);
    }

    // Append a Wav file with speech and liveness contexts to the 
    // outbound queue.
    public Boolean append(File file, SpeechContexts contexts) {
        return mVerifier.append(file, contexts);
    }

    // Append a content list
    public Boolean append(@NonNull SpeechContent content) {
        for(SpeechContent.ContentPart part : content.mParts) {
            if(part.mBuffer != null) {
                if(!append(part.mBuffer, part.mContexts)) return false;
            } else if(part.mFile != null) {
                if(!append(part.mFile, part.mContexts)) return false;
            }
        }
        return true;
    }

    // Append a raw audio buffer with speech to the outbound queue.
    public Boolean append(byte[] audioBuffer) {
        return mVerifier.append(audioBuffer);
    }

    // Append a raw audio buffer with speech and liveness contexts to 
    // the outbound queue.
    public Boolean append(byte[] audioBuffer, SpeechContexts contexts) {
        return mVerifier.append(audioBuffer, contexts);
    }

    // Post the queued speech data. (Synchronously)
    public Result post() {
        return mVerifier.post();
    }

    // Post the queued speech data. (Asynchronously)
    public Boolean post(PostCallback callback) {
        return mVerifier.post(callback);
    }

    // Append a Wav file with speech to the outbound queue and immediately 
    // post it. (Synchronously)
    public Result post(File file) {
        return mVerifier.post(file);
    }

    // Append a raw audio buffer with speech to the outbound queue and 
    // immediately post it. (Synchronously)
    public Result post(byte[] audioBuffer) {
        return mVerifier.post(audioBuffer);
    }

    // Append a Wav file with speech and liveness contexts to the outbound 
    // queue and immediately post it. (Synchronously)
    public Result post(File file, SpeechContexts contexts) {
        return mVerifier.post(file, contexts);
    }

    // Append a raw audio buffer with speech and liveness contexts to the outbound 
    // queue and immediately post it. (Synchronously)
    public Result post(byte[] audioBuffer, SpeechContexts contexts) {
        return mVerifier.post(audioBuffer, contexts);
    }

    // Append a Wav file with speech to the outbound queue and immediately 
    // post it. (Asynchronously)
    public Boolean post(File file, PostCallback callback) {
        return mVerifier.post(file, callback);
    }

    // Append a raw audio buffer with speech to the outbound queue and 
    // immediately post it. (Asynchronously)
    public Boolean post(byte[] audioBuffer, PostCallback callback) {
        return mVerifier.post(audioBuffer, callback);
    }

    // Append a Wav file with speech and liveness contexts to the outbound 
    // queue and immediately post it. (Asynchronously)
    public Boolean post(File file, SpeechContexts contexts, PostCallback callback) {
        return mVerifier.post(file, contexts, callback);
    }

    // Append a raw audio buffer with speech and liveness contexts to the outbound 
    // queue and immediately post it. (Asynchronously)
    public Boolean post(byte[] audioBuffer, SpeechContexts contexts, PostCallback callback) {
        return mVerifier.post(audioBuffer, contexts, callback);
    }

    // End a verify session. (Synchronously)
    //
    // Returns the last result that the final post call returned
    public Result end() {
        return mVerifier.end();
    }

    // End a verify session. (Synchronously)
    //
    // Returns the last result that the final post call returned
    public Boolean end(EndCallback callback) {
        return mVerifier.end(callback);
    }

    // Cancel a verify session. (Asynchronously)
    public Boolean cancel(String reason) {
        return mVerifier.cancel(reason);
    }

    // Cancel a verify session. (Asynchronously)
    public Boolean cancel(String reason, CancelCallback callback) {
        Log.d(TAG, "cancel:::::::::::::::::::::::::::::"+mVerifier.cancel(reason, callback));
        return mVerifier.cancel(reason, callback);
    }

    ///
    /// Public Static Functions
    ///

    public static String getResultDescription(Result result) {
        return result.name();
    }

    public static ProfileType getProfileType(int profileType) {
        switch(profileType) {
            case 2:
            case 3: return ProfileType.Single;
            case 4:
            case 5: return ProfileType.Batch;
            case 6:
            case 7: return ProfileType.Multi;
            case 8:
            case 9: return ProfileType.Group;
            case 10:
            case 11: return ProfileType.DropOne;
            case 12:
            case 13: return ProfileType.Recognition;
            case 14:
            case 15: return ProfileType.Monitor;
        }
        return ProfileType.Single;
    }

    ///
    /// Private Variables
    ///

    private ISveVerifier mVerifier;

}
