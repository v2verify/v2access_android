package com.validvoice.dynamic.speech.authorization;

import android.support.annotation.NonNull;

import com.validvoice.dynamic.audio.AudioCodec;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;
import com.validvoice.dynamic.speech.authorization.lib.enrollers.ISveEnroller;
import com.validvoice.dynamic.speech.authorization.lib.enrollers.SveEnroller_1;
import com.validvoice.dynamic.speech.authorization.lib.utils.SveFailureCallback;

import java.io.File;
import java.util.HashMap;

public class SveEnroller {

    ///
    /// Public Enums and Classes
    ///

    public enum Gender {

        Unknown,

        Male,

        Female

    }

    public enum Result {

        Success,

        NeedMore,

        LimitReached,

        AlreadyEnrolled,

        TokenExists,

        TokenRequired,

        Unauthorized,

        Timeout,

        Invalid,

        Error,

        Unknown

    }

    public static class Profile {

        public AudioCodec Codec;

        public double MinimumSecondsOfSpeech;

    }

    public static class InstanceResult {

        public InstanceResult() {
            Extra = new HashMap<>();
        }

        public SveEnroller.Result Result;

        public double SpeechExtracted;

        public double SpeechTrained;

        public int ErrorCode;

        public HashMap<String, Object> Extra;

    }

    public static class EnrollException extends Exception {

        public EnrollException(String msg) {
            super(msg);
        }

        public EnrollException(Result result) {
            super("Enroll Result: " + result);
        }

        public EnrollException(String msg, Result result) {
            super(msg + ", Enroll Result: " + result);
        }
    }

    ///
    /// Callbacks
    ///

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
        void onEndComplete(Result result);
    }

    public interface CancelCallback extends SveFailureCallback {
        void onCancelComplete();
    }

    ///
    /// Constructors
    ///

    public SveEnroller() {
        mEnroller = new SveEnroller_1();
    }

    ///
    /// Public Properties
    ///

    public SveEnroller setInteractionId(String iid) {
        mEnroller.setInteractionId(iid);
        return this;
    }

    public String getInteractionId() {
        return mEnroller.getInteractionId();
    }

    public SveEnroller setInteractionTag(String tag) {
        mEnroller.setInteractionTag(tag);
        return this;
    }

    public String getInteractionTag() {
        return mEnroller.getInteractionTag();
    }

    public SveEnroller setClientId(String cid) {
        mEnroller.setClientId(cid);
        return this;
    }

    public String getClientId() {
        return mEnroller.getClientId();
    }

    public SveEnroller setGender(Gender gender) {
        mEnroller.setGender(gender);
        return this;
    }

    public Gender getGender() {
        return mEnroller.getGender();
    }

    public SveEnroller setAuthToken(String authToken) {
        mEnroller.setAuthToken(authToken);
        return this;
    }

    public String getAuthToken() {
        return mEnroller.getAuthToken();
    }

    public void setMetaData(String name, boolean value) {
        mEnroller.setMetaData(name, value);
    }

    public void setMetaData(String name, int value) {
        mEnroller.setMetaData(name, value);
    }

    public void setMetaData(String name, double value) {
        mEnroller.setMetaData(name, value);
    }

    public void setMetaData(String name, String value) {
        mEnroller.setMetaData(name, value);
    }

    ///
    /// Public Getters
    ///

    public String getSessionId() {
        return mEnroller.getSessionId();
    }

    public Boolean isSessionOpen() {
        return mEnroller.isSessionOpen();
    }

    public HashMap<Integer, Profile> getProfiles() {
        return mEnroller.getProfiles();
    }

    public HashMap<String, Object> getExtra() {
        return mEnroller.getExtra();
    }

    public AudioCodec getCodec() {
        return mEnroller.getCodec();
    }

    public double getSpeechRequired() {
        return mEnroller.getSpeechRequired();
    }

    public double getSpeechExtracted() {
        return mEnroller.getSpeechExtracted();
    }

    public double getSpeechTrained() {
        return mEnroller.getSpeechTrained();
    }

    public Result getEnrollResult() {
        return mEnroller.getEnrollResult();
    }

    public HashMap<String, InstanceResult> getEnrollResults() {
        return mEnroller.getEnrollResults();
    }

    public int getTotalProcessCalls() {
        return mEnroller.getTotalProcessCalls();
    }

    public long getTotalBytesSent() {
        return mEnroller.getTotalBytesSent();
    }

    public Boolean hasEnoughSpeech() {
        return mEnroller.hasEnoughSpeech();
    }

    public Boolean isTrained() {
        return mEnroller.isTrained();
    }

    ///
    /// Public Methods
    ///

    // Pre-fetch the enrollment speech profile. (Synchronously)
    public Profile prefetchProfile() {
        return mEnroller.prefetchProfile();
    }

    // Pre-fetch the enrollment speech profile. (Asynchronously)
    public Boolean prefetchProfile(ProfileCallback callback) {
        return mEnroller.prefetchProfile(callback);
    }

    // Start an Enrollment Session. (Synchronously)
    public Boolean start() {
        return mEnroller.start();
    }

    // Start an Enrollment Session. (Asynchronously)
    public Boolean start(StartCallback callback) {
        return mEnroller.start(callback);
    }

    // Append a Wav file with speech to the outbound queue.
    public Boolean append(File file) {
        return mEnroller.append(file);
    }

    // Append a content list
    public Boolean append(@NonNull SpeechContent content) {
        for(SpeechContent.ContentPart part : content.mParts) {
            if(part.mBuffer != null) {
                if(!append(part.mBuffer)) return false;
            } else if(part.mFile != null) {
                if(!append(part.mFile)) return false;
            }
        }
        return true;
    }



    // Append a raw audio buffer with speech to the outbound queue.
    public Boolean append(byte[] audioBuffer) {
        return mEnroller.append(audioBuffer);
    }

    // Post the queued speech data. (Synchronously)
    public Result post() {
        return mEnroller.post();
    }

    // Post the queued speech data. (Asynchronously)
    public Boolean post(PostCallback callback) {
        return mEnroller.post(callback);
    }

    // Append a Wav file with speech to the outbound queue and immediately 
    // post it. (Synchronously)
    public Result post(File file) {
        return mEnroller.post(file);
    }

    // Append a raw audio buffer with speech to the outbound queue and 
    // immediately post it. (Synchronously)
    public Result post(byte[] audioBuffer) {
        return mEnroller.post(audioBuffer);
    }

    // Append a Wav file with speech to the outbound queue and immediately 
    // post it. (Asynchronously)
    public Boolean post(File file, PostCallback callback) {
        return mEnroller.post(file, callback);
    }

    // Append a raw audio buffer with speech to the outbound queue and 
    // immediately post it. (Asynchronously)
    public Boolean post(byte[] audioBuffer, PostCallback callback) {
        return mEnroller.post(audioBuffer, callback);
    }

    // End and train an enrollment session. (Synchronously)
    public Result end() {
        return mEnroller.end();
    }

    // End and train an enrollment session. (Asynchronously)
    public Boolean end(EndCallback callback) {
        return mEnroller.end(callback);
    }

    // Cancel an enrollment session. (Synchronously)
    public Boolean cancel(String reason) {
        return mEnroller.cancel(reason);
    }

    // Cancel an enrollment session. (Asynchronously)
    public Boolean cancel(String reason, CancelCallback callback) {
        return mEnroller.cancel(reason, callback);
    }

    ///
    /// Public Static Functions
    ///

    public static String getResultDescription(Result result) {
        return result.name();
    }

    public static Gender getGender(String gender)
    {
        switch (gender.toLowerCase())
        {
            case "m":
            case "male": return Gender.Male;
            case "f":
            case "female": return Gender.Female;
        }
        return Gender.Unknown;
    }

    public static String getGender(Gender gender)
    {
        switch (gender)
        {
            case Male: return "m";
            case Female: return "f";
        }
        return "u";
    }

    ///
    /// Private Variables
    ///

    private ISveEnroller mEnroller;
}
