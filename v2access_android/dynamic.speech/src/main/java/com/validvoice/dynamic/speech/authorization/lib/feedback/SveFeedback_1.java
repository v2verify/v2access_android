package com.validvoice.dynamic.speech.authorization.lib.feedback;

import android.util.Log;

import com.validvoice.dynamic.speech.authorization.SveFeedback;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpResponse;

import java.util.HashMap;

public class SveFeedback_1 extends ISveFeedback {
    ///
    /// Private Static Variables
    ///
    private static final String TAG = "SveFeedback_1";
    private static final String SESSION_HEADER = "Vv-Session-Id";
    private static final String FEEDBACK_BREAK_ATTEMPT_HEADER = "Feedback-BreakAttempt";
    private static final String FEEDBACK_RECORDING_HEADER = "Feedback-Recording";
    private static final String FEEDBACK_BACKGROUND_NOISE_HEADER = "Feedback-BackgroundNoise";
    private static final String FEEDBACK_COMMENTS_HEADER = "Feedback-Comments";
    private static final String URI_PATH_FEEDBACK = "1/sve/Feedback";

    ///
    /// Constructor
    ///

    public SveFeedback_1(String sessionId) {
        super(sessionId);
    }

    ///
    /// Public Overridden Methods
    ///

    @Override
    public Boolean post() {
        HashMap<String, String> headers = new HashMap<>(5);

        String sessionId = getSessionId();
        if(sessionId == null || sessionId.isEmpty()) {
            Log.e(TAG, "Session Id Acquired from SveVerifier was invalid");
            return false;
        }
        headers.put(SESSION_HEADER, sessionId);

        Boolean isBreakAttempt = getBreakAttempt();
        if(isBreakAttempt != null) {
            headers.put(FEEDBACK_BREAK_ATTEMPT_HEADER, isBreakAttempt ? "1" : "0");
        }

        Boolean isRecording = getRecording();
        if(isRecording != null) {
            headers.put(FEEDBACK_RECORDING_HEADER, isRecording ? "1" : "0");
        }

        Boolean isBackgroundNoise = getBackgroundNoise();
        if(isBackgroundNoise != null) {
            headers.put(FEEDBACK_BACKGROUND_NOISE_HEADER, isBackgroundNoise ? "1" : "0");
        }

        String comments = getComments();
        if(comments != null) {
            headers.put(FEEDBACK_COMMENTS_HEADER, comments);
        }

        SveHttpResponse response = webAgent().post(URI_PATH_FEEDBACK, headers);

        if(response == null) {
            Log.e(TAG, "webAgent Timed-out: " + URI_PATH_FEEDBACK);
            return false;
        } else {
            try {
                if (response.getStatusCode() == 200) {
                    return true;
                }
            } finally {
                // Have to explicitly close, in case it was not closed already
                response.close();
            }
        }
        return false;
    }

    @Override
    public Boolean post(final SveFeedback.PostCallback callback) {
        HashMap<String, String> headers = new HashMap<>(5);


        String sessionId = getSessionId();
        if(sessionId == null || sessionId.isEmpty()) {
            Log.e(TAG, "Session Id Acquired from SveVerifier was invalid");
            return false;
        }
        headers.put(SESSION_HEADER, sessionId);

        Boolean isBreakAttempt = getBreakAttempt();
        if(isBreakAttempt != null) {
            headers.put(FEEDBACK_BREAK_ATTEMPT_HEADER, isBreakAttempt ? "1" : "0");
        }

        Boolean isRecording = getRecording();
        if(isRecording != null) {
            headers.put(FEEDBACK_RECORDING_HEADER, isRecording ? "1" : "0");
        }

        Boolean isBackgroundNoise = getBackgroundNoise();
        if(isBackgroundNoise != null) {
            headers.put(FEEDBACK_BACKGROUND_NOISE_HEADER, isBackgroundNoise ? "1" : "0");
        }

        String comments = getComments();
        if(comments != null) {
            headers.put(FEEDBACK_COMMENTS_HEADER, comments);
        }

        return webAgent().post(URI_PATH_FEEDBACK, headers, new SveWebAgent.ResponseCallback() {
            @Override
            public void onResponse(SveHttpResponse response) {
                callback.onPostComplete(response.getStatusCode() == 200);
            }

            @Override
            public void onFailure(Exception ex) {
                callback.onFailure(ex);
            }
        });
    }

}
