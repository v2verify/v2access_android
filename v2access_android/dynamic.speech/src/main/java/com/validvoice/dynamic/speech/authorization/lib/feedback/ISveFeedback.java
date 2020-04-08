package com.validvoice.dynamic.speech.authorization.lib.feedback;

import com.validvoice.dynamic.speech.authorization.SveFeedback;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;

public class ISveFeedback {

    ///
    /// Private Variables
    ///
    private SveWebAgent mWebAgent = new SveWebAgent();
    private String mSessionId = null;
    private Boolean mIsBreakAttempt = null;
    private Boolean mIsRecording = null;
    private Boolean mIsBackgroundNoise = null;
    private String mComments = null;

    ///
    /// Constructor
    ///

    ISveFeedback(String sessionId) {
        mSessionId = sessionId;
    }

    ///
    /// Public Methods
    ///

    public String getSessionId() {
        return mSessionId;
    }

    public void setBreakAttempt(boolean isBreakAttempt)  {
        mIsBreakAttempt = isBreakAttempt;
    }

    public Boolean getBreakAttempt() {
        return mIsBreakAttempt;
    }

    public void setRecording(boolean isRecording)  {
        mIsRecording = isRecording;
    }

    public Boolean getRecording() {
        return mIsRecording;
    }

    public void setBackgroundNoise(boolean isBackgroundNoise)  {
        mIsBackgroundNoise = isBackgroundNoise;
    }

    public Boolean getBackgroundNoise() {
        return mIsBackgroundNoise;
    }

    public void setComments(String comments)  {
        comments = (comments != null && !comments.isEmpty())
                ? comments.substring(0, Math.min(comments.length(), 255))
                : "N/A";

        mComments = comments;
    }

    public String getComments() {
        return mComments;
    }

    public Boolean post() {
        return false;
    }

    public Boolean post(SveFeedback.PostCallback callback) {
        callback.onPostComplete(false);
        return false;
    }

    ///
    /// Protected Methods
    ///

    SveWebAgent webAgent() {
        return mWebAgent;
    }

}
