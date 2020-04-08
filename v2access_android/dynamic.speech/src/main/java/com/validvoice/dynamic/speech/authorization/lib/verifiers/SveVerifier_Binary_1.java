/*
package com.validvoice.dynamic.speech.sve.lib.verifiers;

import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.speech.sve.lib.tcmp.SveProtocol;
import com.validvoice.dynamic.tcmp.TCMPBuffer;
import com.validvoice.dynamic.tcmp.TCMPSession;
import com.validvoice.dynamic.tcmp.TCMPMessage;
import com.validvoice.dynamic.speech.sve.SveClient;
import com.validvoice.dynamic.speech.sve.SveVerifier;

import java.net.URI;
import java.util.Map;

public class SveVerifier_Binary_1 extends ISveVerifier {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "SveVerifier_Binary_1";
    private static final String SVE_SERVICE_TAG = "sve";
    private static final String SVE_API_VERSION = "1";
    private static final String SVE_API_METHOD_FETCH_PROFILE = "verify_profile";
    private static final String SVE_API_METHOD_START = "verify_start";
    private static final String SVE_API_METHOD_PROCESS = "verify_process";
    private static final String SVE_API_METHOD_END = "verify_end";
    private static final String SVE_API_METHOD_CANCEL = "cancel";
    private static final String SVE_API_METHOD_LIVENESS = "liveness";

    ///
    /// Private Variables
    ///
    private int mLastProfileIndex = -1;
    private TCMPSession mSession;
    private boolean mUsingDefault;

    public SveVerifier_Binary_1() {
        super();

        mUsingDefault = true;
        mSession = SveClient.GetDefaultSession();
        if(mSession == null) {
            mUsingDefault = false;
            mSession = new TCMPSession(SveClient.CurrentProtocol());
            DoConnect();
        }
    }

    @Override
    public boolean PostLivenessComplete(String expected, String captured, Boolean isAlive) {
        LivenessData ld = GetLivenessData();
        if(ld != null) {
            ClearLivenessData();

            if(!IsConnected()) {
                DoConnect();
            }

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_LIVENESS);
            message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, ld.DeveloperKey());
            message.AddElement(SveProtocol.SVE_APPLICATION_KEY, ld.ApplicationKey());
            message.AddElement(SveProtocol.SVE_SESSION_ID, ld.SessionId());
            message.AddElement(SveProtocol.SVE_EXPECTED_TEXT, expected);
            message.AddElement(SveProtocol.SVE_CAPTURED, captured);
            message.AddElement(SveProtocol.SVE_IS_ALIVE, isAlive);

            Log.d(TAG, "Verifier.PostLivenessComplete()");

            message = mSession.ExpectMessage(message);
            if (message == null) {
                Log.e(TAG, "Verifier.PostLivenessComplete(): Timeout occurred while attempting to post liveness verification data");
                return false;
            }

        } else {
            Log.e(TAG, "Liveness not prepared for session");
        }
        return false;
    }

    @Override
    public boolean PostLivenessComplete(String expected, String captured, Boolean isAlive, final SveVerifier.LivenessCallback callback) {
        LivenessData ld = GetLivenessData();
        if(ld != null) {
            if (callback == null) {
                Log.e(TAG, "Callback was null");
                return false;
            }

            if(!IsConnected()) {
                DoConnect();
            }

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_LIVENESS);
            message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, ld.DeveloperKey());
            message.AddElement(SveProtocol.SVE_APPLICATION_KEY, ld.ApplicationKey());
            message.AddElement(SveProtocol.SVE_SESSION_ID, ld.SessionId());
            message.AddElement(SveProtocol.SVE_EXPECTED_TEXT, expected);
            message.AddElement(SveProtocol.SVE_CAPTURED, captured);
            message.AddElement(SveProtocol.SVE_IS_ALIVE, isAlive);

            ClearLivenessData();
            return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
                @Override
                public void onResponse(TCMPMessage response) {
                    callback.onLivenessComplete();
                }

                @Override
                public void onFailure(Exception ex) {
                    callback.onFailure(ex);
                }
            });
        }
        Log.e(TAG, "Liveness not prepared for session");
        return false;
    }

    @Override
    public SveVerifier.Profile PrefetchProfile() {
        if(!IsConnected()) {
            DoConnect();
        }

        ResetVerifier();

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_FETCH_PROFILE);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);

        Log.d(TAG, "Verifier.PrefetchProfile()");

        message = mSession.ExpectMessage(message);
        if (message == null) {
            Log.e(TAG, "Verifier.PrefetchProfile(): Timeout occurred while attempting to fetch verification profile");
            return null;
        }

        int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
        if (error != 0) {
            Log.e(TAG, "Verifier.PrefetchProfile(): Error occurred while attempting to fetch verification profile. Error: " + error);
            HandleError(message);
            return null;
        }

        if (!HandleResult(message)) {
            Log.e(TAG, "Verifier.PrefetchProfile(): Error occurred while handling the result.");
            return null;
        }

        return GetProfiles().get(mLastProfileIndex);
    }

    @Override
    public Boolean PrefetchProfile(final SveVerifier.ProfileCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Verifier.PrefetchProfile(Callback): Callback was null");
            return false;
        }

        if(!IsConnected()) {
            DoConnect();
        }

        ResetVerifier();

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_FETCH_PROFILE);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);

        Log.d(TAG, "Verifier.PrefetchProfile(Callback)");

        return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
            @Override
            public void onResponse(TCMPMessage response) {
                int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                if (error != 0) {
                    Log.e(TAG, "Verifier.PrefetchProfile(Callback): Error occurred while attempting to fetch verification profile. Error: " + error);
                    HandleError(response);
                    callback.onFailure(new SveVerifier.VerifyException("Error occurred while attempting to fetch verification profile. Error: " + error));
                    return;
                }

                if (!HandleResult(response)) {
                    Log.e(TAG, "Verifier.PrefetchProfile(Callback): Error occurred while handling the result.");
                    callback.onFailure(new SveVerifier.VerifyException("Unable to handle response"));
                    return;
                }

                callback.onProfileComplete(GetProfiles().get(mLastProfileIndex));
            }

            @Override
            public void onFailure(Exception ex) {
                callback.onFailure(ex);
            }
        });
    }

    @Override
    public Boolean Start() {
        if(!IsConnected()) {
            DoConnect();
        }

        ResetVerifier();

        String clientId = GetClientId();
        if(clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "Verifier.Start(): Missing Client-Id");
            UpdateVerifyResult(SveVerifier.Result.Invalid);
            return false;
        }

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_START);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);
        message.AddElement(SveProtocol.SVE_CLIENT_ID, clientId);

        Log.d(TAG, "Verifier.Start()");

        if (GetInteractionId() != null && !GetInteractionId().isEmpty()) {
            message.AddElement(SveProtocol.SVE_INTERACTION_ID, GetInteractionId());
        }

        message = mSession.ExpectMessage(message);

        if (message == null) {
            UpdateSessionStatus(false);
            UpdateVerifyResult(SveVerifier.Result.Timeout);
            Log.e(TAG, "Verifier.Start(): Timeout occurred while attempting to start verification.");
            return null;
        }

        int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
        if (error != 0) {
            UpdateSessionStatus(false);
            HandleError(message);
            Log.e(TAG, "Verifier.Start(): Error occurred while attempting to start verification. Error: " + error);
            return null;
        }

        if (!HandleResult(message)) {
            UpdateSessionStatus(false);
            Log.e(TAG, "Verifier.Start(): Error occurred while handling the result.");
            return null;
        }

        UpdateSessionId((String) message.GetElement(SveProtocol.SVE_SESSION_ID));
        UpdateSessionStatus(true);

        return true;
    }

    @Override
    public Boolean Start(final SveVerifier.StartCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Verifier.Start(Callback): Callback was null");
            return false;
        }

        if(!IsConnected()) {
            DoConnect();
        }

        ResetVerifier();

        String clientId = GetClientId();
        if(clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "Verifier.Start(Callback): Missing Client-Id");
            UpdateVerifyResult(SveVerifier.Result.Invalid);
            return false;
        }

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_START);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);
        message.AddElement(SveProtocol.SVE_CLIENT_ID, clientId);

        Log.d(TAG, "Verifier.Start(Callback)");

        if (GetInteractionId() != null && !GetInteractionId().isEmpty()) {
            message.AddElement(SveProtocol.SVE_INTERACTION_ID, GetInteractionId());
        }

        return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
            @Override
            public void onResponse(TCMPMessage response) {
                int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                if (error != 0) {
                    UpdateSessionStatus(false);
                    HandleError(response);
                    Log.e(TAG, "Verifier.Start(Callback): Error occurred while attempting to start verification. Error: " + error);
                    callback.onFailure(new SveVerifier.VerifyException("Error occurred while attempting to start verification. Error: " + error));
                    return;
                }

                if (!HandleResult(response)) {
                    UpdateSessionStatus(false);
                    Log.e(TAG, "Verifier.Start(Callback): Error occurred while handling the result.");
                    callback.onFailure(new SveVerifier.VerifyException("Unable to handle response"));
                    return;
                }

                UpdateSessionId((String) response.GetElement(SveProtocol.SVE_SESSION_ID));
                UpdateSessionStatus(true);

                callback.onStartComplete();
            }

            @Override
            public void onFailure(Exception ex) {
                UpdateSessionStatus(false);
                callback.onFailure(ex);
            }
        });
    }


    @Override
    public SveVerifier.Result Post(byte[] audioBuffer) {
        if(IsConnected() && IsSessionOpen()) {
            GetVerifyResults().clear();

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_PROCESS);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            TCMPBuffer buffer = new TCMPBuffer();
            buffer.Write(audioBuffer, audioBuffer.length);
            message.AddBuffer(SveProtocol.SVE_VOICE, buffer);

            Log.d(TAG, "Verifier.Post()");

            message = mSession.ExpectMessage(message);

            UpdateTotalBytesSent(audioBuffer.length);
            UpdateTotalIndependentCalls();

            if (message == null) {
                Log.e(TAG, "Verifier.Post(): Timeout occurred while attempting to post enrollment data");
                return SveVerifier.Result.Timeout;
            }

            int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
            if (error != 0) {
                Log.e(TAG, "Verifier.Post(): Error occurred while attempting to post enrollment data. Error: " + error);
                HandleError(message);
                return SveVerifier.Result.Error;
            }

            if (!HandleResult(message)) {
                Log.e(TAG, "Verifier.Post(): Error occurred while handling the result.");
                return SveVerifier.Result.Error;
            }

            Log.d(TAG, "Verifier.Post(): Score: " + GetVerifyScore() + ", " + GetVerifyResult());

            return GetVerifyResult();
        }
        return SveVerifier.Result.Invalid;
    }

    @Override
    public Boolean Post(final byte[] audioBuffer, final SveVerifier.PostCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Verifier.Post(Callback): Callback was null");
            return false;
        }

        if(IsConnected() && IsSessionOpen()) {
            GetVerifyResults().clear();

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_PROCESS);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            TCMPBuffer buffer = new TCMPBuffer();
            buffer.Write(audioBuffer, audioBuffer.length);
            message.AddBuffer(SveProtocol.SVE_VOICE, buffer);

            Log.d(TAG, "Verifier.Post(Callback)");

            return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
                @Override
                public void onResponse(TCMPMessage response) {

                    UpdateTotalBytesSent(audioBuffer.length);
                    UpdateTotalIndependentCalls();

                    int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                    if (error != 0) {
                        HandleError(response);
                        Log.e(TAG, "Verifier.Post(Callback): Error occurred while attempting to post enrollment data. Error: " + error);
                        callback.onFailure(new SveVerifier.VerifyException("Error occurred while attempting to post enrollment data. Error: " + error));
                        return;
                    }

                    if (!HandleResult(response)) {
                        Log.e(TAG, "Verifier.Post(Callback): Error occurred while handling the result.");
                        callback.onFailure(new SveVerifier.VerifyException("Unable to handle response"));
                        return;
                    }

                    Log.d(TAG, "Verifier.Post(Callback): Score: " + GetVerifyScore() + ", " + GetVerifyResult());

                    callback.onPostComplete(GetVerifyResult());
                }

                @Override
                public void onFailure(Exception ex) {
                    UpdateTotalBytesSent(audioBuffer.length);
                    UpdateTotalIndependentCalls();
                    callback.onFailure(ex);
                }
            });
        }
        return false;
    }

    @Override
    public SveVerifier.Result End() {
        if(IsConnected() && IsSessionOpen()) {

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_END);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            Log.d(TAG, "Verifier.End()");

            message = mSession.ExpectMessage(message);

            if(!mUsingDefault) {
                mSession.Shutdown();
            }

            if (message == null) {
                Log.e(TAG, "Verifier.End(): Timeout occurred while attempting to end enrollment.");
                return SveVerifier.Result.Timeout;
            }

            int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
            if (error != 0) {
                Log.e(TAG, "Verifier.End(): Error occurred while attempting to end enrollment. Error: " + error);
                HandleError(message);
                return SveVerifier.Result.Error;
            }

            if (!HandleResult(message)) {
                Log.e(TAG, "Verifier.End(): Error occurred while handling the result.");
                return SveVerifier.Result.Error;
            }

            UpdateSessionStatus(false);
            UpdateSessionId(null);

            Log.d(TAG, "Verifier.End(): Score: " + GetVerifyScore() + ", " + GetVerifyResult());

            return GetVerifyResult();
        }
        return SveVerifier.Result.Invalid;
    }

    @Override
    public Boolean End(final SveVerifier.EndCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Verifier.End(Callback): Callback was null");
            return false;
        }

        if(IsConnected() && IsSessionOpen()) {

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_END);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            Log.d(TAG, "Verifier.End(Callback)");

            return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
                @Override
                public void onResponse(TCMPMessage response) {
                    if(!mUsingDefault) {
                        mSession.Shutdown();
                    }

                    int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                    if (error != 0) {
                        Log.e(TAG, "Verifier.End(Callback): Error occurred while attempting to end enrollment. Error: " + error);
                        HandleError(response);
                        callback.onFailure(new SveVerifier.VerifyException("Error occurred while attempting to end enrollment. Error: " + error));
                        return;
                    }

                    if (!HandleResult(response)) {
                        Log.e(TAG, "Verifier.End(Callback): Error occurred while handling the result.");
                        callback.onFailure(new SveVerifier.VerifyException("Unable to handle response"));
                        return;
                    }

                    UpdateSessionStatus(false);
                    UpdateSessionId(null);

                    Log.d(TAG, "Verifier.End(Callback): Score: " + GetVerifyScore() + ", " + GetVerifyResult());

                    callback.onEndComplete(GetVerifyResult());
                }

                @Override
                public void onFailure(Exception ex) {
                    if(!mUsingDefault) {
                        mSession.Shutdown();
                    }
                    callback.onFailure(ex);
                }
            });
        }
        return false;
    }

    @Override
    public Boolean Cancel(String reason) {
        if(IsConnected() && IsSessionOpen()) {

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_CANCEL);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());
            message.AddElement(SveProtocol.SVE_REASON, reason.replace(' ', '-').substring(0, Math.min(reason.length(), 64)));

            Log.d(TAG, "Verifier.Cancel()");

            message = mSession.ExpectMessage(message);

            if(!mUsingDefault) {
                mSession.Shutdown();
            }

            if (message == null) {
                UpdateSessionStatus(false);
                UpdateSessionId(null);
                Log.e(TAG, "Verifier.Cancel(): Timeout occurred while attempting to cancel verification.");
                return false;
            }

            int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
            if (error != 0) {
                UpdateSessionStatus(false);
                UpdateSessionId(null);
                Log.e(TAG, "Verifier.Cancel(): Error occurred while attempting to cancel verification. Error: " + error);
                HandleError(message);
                return false;
            }

            if (!HandleResult(message)) {
                UpdateSessionStatus(false);
                UpdateSessionId(null);
                Log.e(TAG, "Verifier.Cancel(): Error occurred while handling the result.");
                return false;
            }

            UpdateSessionStatus(false);
            UpdateSessionId(null);

            return true;
        }
        return false;
    }

    @Override
    public Boolean Cancel(String reason, final SveVerifier.CancelCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Verifier.Cancel(Callback): Callback was null");
            return false;
        }

        if(IsConnected() && IsSessionOpen()) {

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_CANCEL);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());
            message.AddElement(SveProtocol.SVE_REASON, reason.replace(' ', '-').substring(0, Math.min(reason.length(), 64)));

            Log.d(TAG, "Verifier.Cancel(Callback)");

            return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
                @Override
                public void onResponse(TCMPMessage response) {
                    if(!mUsingDefault) {
                        mSession.Shutdown();
                    }

                    int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                    if (error != 0) {
                        UpdateSessionStatus(false);
                        UpdateSessionId(null);
                        HandleError(response);
                        Log.e(TAG, "Verifier.End(Callback): Error occurred while attempting to end verification. Error: " + error);
                        callback.onFailure(new SveVerifier.VerifyException("Error occurred while attempting to end verification. Error: " + error));
                        return;
                    }

                    if (!HandleResult(response)) {
                        UpdateSessionStatus(false);
                        UpdateSessionId(null);
                        Log.e(TAG, "Verifier.End(Callback): Error occurred while handling the result.");
                        callback.onFailure(new SveVerifier.VerifyException("Unable to handle response"));
                        return;
                    }

                    UpdateSessionStatus(false);
                    UpdateSessionId(null);

                    callback.onCancelComplete();
                }

                @Override
                public void onFailure(Exception ex) {
                    if(!mUsingDefault) {
                        mSession.Shutdown();
                    }
                    callback.onFailure(ex);
                }
            });
        }
        return false;
    }

    ///
    /// Private Methods
    ///

    private boolean IsConnected() {
        return mSession != null && mSession.IsConnected();
    }

    private void DoConnect() {
        if(!mSession.IsConnected() && !mSession.IsConnecting()) {
            Uri uri = Uri.parse(SveClient.CurrentConfiguration().Server);
            mSession.Connect(uri.getHost(), uri.getPort());
        }
    }

    private boolean HandleResult(TCMPMessage message) {
        if(message.HasElement(SveProtocol.SVE_RESULT_TYPE)) {
            String result_type = (String)message.GetElement(SveProtocol.SVE_RESULT_TYPE);
            if(result_type.equals("application/json")) {
                JsonObject o = message.GetElementAsJsonObject(SveProtocol.SVE_RESULT);
                if(o.has("error")) {
                    String data = o.get("error").getAsString();
                    Log.e(TAG, "HandleResult(): Error detected: , Data: " + data);
                    o.remove("error");
                }

                for(Map.Entry<String, JsonElement> elem : o.entrySet()) {
                    if(elem.getKey().equals("profile.verify")) {
                        mLastProfileIndex = AddProfile(elem.getValue().getAsJsonObject());
                    } else if(elem.getKey().equals("result.verify")) {
                        JsonObject result_o = elem.getValue().getAsJsonObject();
                        for(Map.Entry<String, JsonElement> result_elem : result_o.entrySet()) {
                            AddResult(result_elem.getKey().toLowerCase(), result_elem.getValue().getAsJsonObject());
                        }

                        String clientId = GetClientId();
                        if(result_o.entrySet().size() > 0 && clientId != null && !clientId.isEmpty()) {
                            SveVerifier.InstanceResult result = GetVerifyResults().get(clientId.toLowerCase());
                            UpdateSpeechExtracted(result.SpeechExtracted);
                            UpdateVerifyResult(result.Result);
                            UpdateVerifyScore(result.Score);
                        }
                    }
                }
                return true;
            } else {
                UpdateVerifyResult(SveVerifier.Result.Error);
                Log.e(TAG, "Expected result content type to be 'application/json'. It was found to be: " + result_type);
                return false;
            }
        } else {
            UpdateVerifyResult(SveVerifier.Result.Error);
            Log.e(TAG, "Malformed Message: Expected field: 'sve.result.type', got nothing.");
            return false;
        }
    }

    private SveVerifier.Result HandleError(TCMPMessage message) {
        SveVerifier.Result result = SveVerifier.Result.Invalid;
        if(message.HasElement(SveProtocol.SVE_RESULT_TYPE)) {
            String result_type = (String) message.GetElement(SveProtocol.SVE_RESULT_TYPE);
            if (result_type.equals("application/json")) {
                JsonObject o = message.GetElementAsJsonObject(SveProtocol.SVE_RESULT);
                if (o.has("error") && o.has("description")) {
                    int code = o.get("error").getAsInt();
                    String description = o.get("description").getAsString();
                    result = GetErrorResult(code);
                    Log.e(TAG, "HandleError: Error detected. Result: " + result + ", Code: " + code + ", Description: " + description);
                }
            }
        }  else {
            Log.e(TAG, "Malformed Message: Expected field: 'sve.result.type', got nothing.");
        }
        UpdateVerifyResult(result);
        return result;
    }
}
*/