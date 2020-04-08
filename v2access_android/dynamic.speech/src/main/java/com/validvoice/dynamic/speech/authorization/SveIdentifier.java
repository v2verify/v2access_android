package com.validvoice.dynamic.speech.authorization;

import com.validvoice.dynamic.audio.AudioCodec;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;
import com.validvoice.dynamic.speech.authorization.lib.identifiers.ISveIdentifier;
import com.validvoice.dynamic.speech.authorization.lib.identifiers.SveIdentifier_1;
import com.validvoice.dynamic.speech.authorization.lib.utils.SveFailureCallback;

import java.io.File;
import java.util.HashMap;

public class SveIdentifier {

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

        LimitReached,

        Unauthorized,

        NotResolved,        // id not resolved from audio

        NotFound,           // voice print for id not found

        BadEnrollment,         // identified client id, but not allowed

        Timeout,

        Invalid,

        Error,

        Unknown

    }

    public enum ProfileType {

        Single

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

        public Result Result;

        public double SpeechExtracted;

        public double Score;

        public int ErrorCode;

        public HashMap<String, Object> Extra;

    }

    public static class IdentifierException extends Exception {
        public IdentifierException() {

        }

        public IdentifierException(String msg) {
            super(msg);
        }

        public IdentifierException(Result result) {
            super("Identify Result: " + result);
        }

        public IdentifierException(String msg, Result result) {
            super(msg + ", Identify Result: " + result);
        }
    }

    public interface PostCallback extends SveFailureCallback {
        void onPostComplete(Result result);
    }

    public interface EndCallback extends SveFailureCallback {
        void onEndComplete(Result result);
    }

    public interface CancelCallback extends SveFailureCallback {
        void onCancelComplete();
    }

    ///
    /// Constructors
    ///

    public SveIdentifier() {
        mIdentifier = new SveIdentifier_1();
    }

    ///
    /// Public Properties
    ///

    public SveIdentifier setInteractionId(String iid) {
        mIdentifier.setInteractionId(iid);
        return this;
    }

    public String getInteractionId() {
        return mIdentifier.getInteractionId();
    }

    public SveIdentifier setInteractionTag(String tag) {
        mIdentifier.setInteractionTag(tag);
        return this;
    }

    public String getInteractionTag() {
        return mIdentifier.getInteractionTag();
    }

    public String getResolvedClientId() {
        return mIdentifier.getResolvedClientId();
    }

    public String getSessionId() {
        return mIdentifier.getSessionId();
    }

    public Boolean isSessionOpen() {
        return mIdentifier.isSessionOpen();
    }

    public Boolean isSessionClosing() {
        return mIdentifier.isSessionClosing();
    }

    public void setMetaData(String name, boolean value) {
        mIdentifier.setMetaData(name, value);
    }

    public void setMetaData(String name, int value) {
        mIdentifier.setMetaData(name, value);
    }

    public void setMetaData(String name, double value) {
        mIdentifier.setMetaData(name, value);
    }

    public void setMetaData(String name, String value) {
        mIdentifier.setMetaData(name, value);
    }

    ///
    /// Public Getters
    ///

    public HashMap<Integer, Profile> getProfiles() {
        return mIdentifier.getProfiles();
    }

    public AudioCodec getCodec() {
        return mIdentifier.getCodec();
    }

    public double getVerifyScore() {
        return mIdentifier.getVerifyScore();
    }

    public double getSpeechExtracted() {
        return mIdentifier.getSpeechExtracted();
    }

    public double getSpeechRequired() {
        return mIdentifier.getSpeechRequired();
    }

    public int getSpeechProgress() {
        return mIdentifier.getSpeechProgress();
    }

    public boolean hasEnoughSpeech() {
        return mIdentifier.hasEnoughSpeech();
    }

    public Result getRawIdentifierResult() {
        return mIdentifier.getRawIdentifierResult();
    }

    public Result getIdentifierResult() {
        return mIdentifier.getIdentifierResult();
    }

    public HashMap<String, InstanceResult> getIdentifierResults() {
        return mIdentifier.getIdentifierResults();
    }

    public int getTotalProcessCalls() {
        return mIdentifier.getTotalProcessCalls();
    }

    public long getTotalBytesSent() {
        return mIdentifier.getTotalBytesSent();
    }

    public Boolean isLivenessRequired() {
        return mIdentifier.isLivenessRequired();
    }

    public Boolean isVerified() {
        return mIdentifier.isVerified();
    }

    public Boolean isAlive() {
        return mIdentifier.isAlive();
    }

    public Boolean hasRawResult() {
        return mIdentifier.hasRawResult();
    }

    public Boolean hasResult() {
        return mIdentifier.hasResult();
    }

    ///
    /// Public Methods
    ///

    public void reset() {
        mIdentifier.reset();
    }

    public Boolean append(File file) {
        return mIdentifier.append(file);
    }

    public Boolean append(File file, SpeechContexts contexts) {
        return mIdentifier.append(file, contexts);
    }

    public Boolean append(byte[] audioBuffer) {
        return mIdentifier.append(audioBuffer);
    }

    public Boolean append(byte[] audioBuffer, SpeechContexts contexts) {
        return mIdentifier.append(audioBuffer, contexts);
    }

    public Result post() {
        return mIdentifier.post();
    }

    public Boolean post(PostCallback callback) {
        return mIdentifier.post(callback);
    }

    public Result end() {
        return mIdentifier.end();
    }

    public Boolean end(EndCallback callback) {
        return mIdentifier.end(callback);
    }

    public Boolean cancel(String reason) {
        return mIdentifier.cancel(reason);
    }

    public Boolean cancel(String reason, CancelCallback callback) {
        return mIdentifier.cancel(reason, callback);
    }

    ///
    /// Public Static Functions
    ///

    public static String GetResultDescription(Result result) {
        return result.name();
    }

    public static ProfileType GetProfileType(int profileType) {
        switch(profileType) {
            case 2:
            case 3: return ProfileType.Single;
        }
        return ProfileType.Single;
    }

    ///
    /// Private Variables
    ///

    private ISveIdentifier mIdentifier;

}
