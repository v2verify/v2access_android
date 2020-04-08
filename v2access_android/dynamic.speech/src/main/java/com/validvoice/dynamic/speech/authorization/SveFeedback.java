package com.validvoice.dynamic.speech.authorization;

import com.validvoice.dynamic.speech.authorization.lib.feedback.ISveFeedback;
import com.validvoice.dynamic.speech.authorization.lib.feedback.SveFeedback_1;
import com.validvoice.dynamic.speech.authorization.lib.utils.SveFailureCallback;

public class SveFeedback {

    ///
    /// Interfaces
    ///

    public interface PostCallback extends SveFailureCallback {
        void onPostComplete(boolean isOk);
    }

    ///
    /// Private Variables
    ///

    private ISveFeedback mFeedback;

    ///
    /// Constructors
    ///

    public SveFeedback(SveVerifier verifier) {
        mFeedback = new SveFeedback_1(verifier.getSessionId());
    }

    ///
    /// Public Methods
    ///

    public void setBreakAttempt(boolean isBreakAttempt) {
        mFeedback.setBreakAttempt(isBreakAttempt);
    }

    public boolean getBreakAttempt() {
        Boolean breakAttempt = mFeedback.getBreakAttempt();
        return breakAttempt != null ? breakAttempt : false;
    }

    public void setRecording(boolean isRecording) {
        mFeedback.setRecording(isRecording);
    }

    public boolean getRecording() {
        Boolean recording = mFeedback.getRecording();
        return recording != null ? recording : false;
    }

    public void setBackgroundNoise(boolean isBackgroundNoise) {
        mFeedback.setBackgroundNoise(isBackgroundNoise);
    }

    public boolean getBackgroundNoise() {
        Boolean backgroundNoise = mFeedback.getBackgroundNoise();
        return backgroundNoise != null ? backgroundNoise : false;
    }

    public void setComments(String comments) {
        mFeedback.setComments(comments);
    }

    public String getComments() {
        String comments = mFeedback.getComments();
        return comments != null ? comments : "N/A";
    }

    public Boolean post() {
        return mFeedback.post();
    }

    public Boolean post(SveFeedback.PostCallback callback) {
        return mFeedback.post(callback);
    }

}
