/*
package com.validvoice.dynamic.speech.sve.lib.enrollers;

import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.speech.sve.lib.tcmp.SveProtocol;
import com.validvoice.dynamic.tcmp.TCMPBuffer;
import com.validvoice.dynamic.tcmp.TCMPSession;
import com.validvoice.dynamic.tcmp.TCMPMessage;
import com.validvoice.dynamic.speech.sve.SveClient;
import com.validvoice.dynamic.speech.sve.SveEnroller;

import java.util.Map;

public class SveEnroller_Binary_1 extends ISveEnroller {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "SveEnroller_Binary_1";
    private static final String SVE_SERVICE_TAG = "sve";
    private static final String SVE_API_VERSION = "1";
    private static final String SVE_API_METHOD_FETCH_PROFILE = "enroll_profile";
    private static final String SVE_API_METHOD_START = "enroll_start";
    private static final String SVE_API_METHOD_PROCESS = "enroll_process";
    private static final String SVE_API_METHOD_END = "enroll_end";
    private static final String SVE_API_METHOD_CANCEL = "cancel";

    ///
    /// Private Variables
    ///
    private int mLastProfileIndex = -1;
    private TCMPSession mSession;
    private boolean mUsingDefault;

    public SveEnroller_Binary_1() {
        super();

        mSession = SveClient.GetDefaultSession();
        if(mSession == null) {
            mUsingDefault = false;
            mSession = new TCMPSession(SveClient.CurrentProtocol());
            DoConnect();
        }
    }

    @Override
    public SveEnroller.Profile PrefetchProfile() {
        if(!IsConnected()) {
            DoConnect();
        }

        ResetEnroller();

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_FETCH_PROFILE);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);

        Log.d(TAG, "Enroller.PrefetchProfile()");

        message = mSession.ExpectMessage(message);
        if (message == null) {
            Log.e(TAG, "Enroller.PrefetchProfile(): Timeout occurred while attempting to fetch enrollment profile");
            return null;
        }

        int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
        if (error != 0) {
            Log.e(TAG, "Enroller.PrefetchProfile(): Error occurred while attempting to fetch enrollment profile. Error: " + error);
            HandleError(message);
            return null;
        }

        if (!HandleResult(message)) {
            Log.e(TAG, "Enroller.PrefetchProfile(): Error occurred while handling the result.");
            return null;
        }

        return GetProfiles().get(mLastProfileIndex);
    }

    @Override
    public Boolean PrefetchProfile(final SveEnroller.ProfileCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        if(!IsConnected()) {
            DoConnect();
        }

        ResetEnroller();

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_FETCH_PROFILE);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);

        Log.d(TAG, "Enroller.PrefetchProfile(Callback)");

        return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
            @Override
            public void onResponse(TCMPMessage response) {
                int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                if (error != 0) {
                    Log.e(TAG, "Enroller.PrefetchProfile(Callback): Error occurred while attempting to fetch enrollment profile. Error: " + error);
                    HandleError(response);
                    callback.onFailure(new SveEnroller.EnrollException("Error occurred while attempting to fetch enrollment profile. Error: " + error));
                    return;
                }

                if (!HandleResult(response)) {
                    Log.e(TAG, "Enroller.PrefetchProfile(Callback): Error occurred while handling the result.");
                    callback.onFailure(new SveEnroller.EnrollException("Unable to handle response"));
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

        ResetEnroller();

        String clientId = GetClientId();
        SveEnroller.Gender gender = GetGender();
        if (clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "Start(): Missing Client-Id");
            return false;
        } else if (gender == SveEnroller.Gender.Unknown) {
            Log.e(TAG, "Start(): has invalid gender type");
            return false;
        }

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_START);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);
        message.AddElement(SveProtocol.SVE_CLIENT_ID, clientId);
        message.AddElement(SveProtocol.SVE_SUB_POP, SveEnroller.GetGender(gender));

        Log.d(TAG, "Enroller.Start()");

        if (GetInteractionId() != null && !GetInteractionId().isEmpty()) {
            message.AddElement(SveProtocol.SVE_INTERACTION_ID, GetInteractionId());
        }

        message = mSession.ExpectMessage(message);
        if (message == null) {
            UpdateSessionStatus(false);
            Log.e(TAG, "Enroller.Start(): Timeout occurred while attempting to fetch enrollment profile");
            return null;
        }

        int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
        if (error != 0) {
            UpdateSessionStatus(false);
            HandleError(message);
            Log.e(TAG, "Enroller.Start(): Error occurred while attempting to fetch enrollment profile. Error: " + error);
            return null;
        }

        if (!HandleResult(message)) {
            UpdateSessionStatus(false);
            Log.e(TAG, "Enroller.Start(): Error occurred while handling the result.");
            return null;
        }

        UpdateSessionId((String) message.GetElement(SveProtocol.SVE_SESSION_ID));
        UpdateSessionStatus(true);

        return true;
    }

    @Override
    public Boolean Start(final SveEnroller.StartCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Enroller.Start(Callback): Callback was null");
            return false;
        }

        if(!IsConnected()) {
            DoConnect();
        }

        ResetEnroller();

        String clientId = GetClientId();
        SveEnroller.Gender gender = GetGender();
        if (clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "Start(): Missing Client-Id");
            return false;
        } else if (gender == SveEnroller.Gender.Unknown) {
            Log.e(TAG, "Start(): has invalid gender type");
            return false;
        }

        TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
        message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
        message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_START);
        message.AddElement(SveProtocol.SVE_DEVELOPER_KEY, SveClient.CurrentConfiguration().DeveloperKey);
        message.AddElement(SveProtocol.SVE_APPLICATION_KEY, SveClient.CurrentConfiguration().ApplicationKey);
        message.AddElement(SveProtocol.SVE_CLIENT_ID, clientId);
        message.AddElement(SveProtocol.SVE_SUB_POP, SveEnroller.GetGender(gender));

        Log.d(TAG, "Enroller.Start(Callback)");

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
                    Log.e(TAG, "Enroller.Start(Callback): Error occurred while attempting to start enrollment. Error: " + error);
                    callback.onFailure(new SveEnroller.EnrollException("Error occurred while attempting to start enrollment. Error: " + error));
                    return;
                }

                if (!HandleResult(response)) {
                    UpdateSessionStatus(false);
                    Log.e(TAG, "Enroller.Start(Callback): Error occurred while handling the result.");
                    callback.onFailure(new SveEnroller.EnrollException("Unable to handle response"));
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
    public SveEnroller.Result Post(byte[] audioBuffer) {
        if(IsConnected() && IsSessionOpen()) {
            GetEnrollResults().clear();

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_PROCESS);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            TCMPBuffer buffer = new TCMPBuffer();
            buffer.Write(audioBuffer, audioBuffer.length);
            message.AddBuffer(SveProtocol.SVE_VOICE, buffer);

            Log.d(TAG, "Enroller.Post()");

            message = mSession.ExpectMessage(message);

            UpdateTotalBytesSent(audioBuffer.length);
            UpdateTotalIndependentCalls();

            if (message == null) {
                Log.e(TAG, "Enroller.Post(): Timeout occurred while attempting to post enrollment data");
                return SveEnroller.Result.Timeout;
            }

            int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
            if (error != 0) {
                Log.e(TAG, "Enroller.Post(): Error occurred while attempting to post enrollment data. Error: " + error);
                HandleError(message);
                return SveEnroller.Result.Error;
            }

            if (!HandleResult(message)) {
                Log.e(TAG, "Enroller.Post(): Error occurred while handling the result.");
                return SveEnroller.Result.Error;
            }

            Log.d(TAG, "Enroller.Post(): Speech Extracted: " + GetSpeechExtracted() + ", " + GetEnrollResult());

            return GetEnrollResult();
        }
        return SveEnroller.Result.Invalid;
    }

    @Override
    public Boolean Post(final byte[] audioBuffer, final SveEnroller.PostCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Enroller.Post(Callback): Callback was null");
            return false;
        }

        if(IsConnected() && IsSessionOpen()) {
            GetEnrollResults().clear();

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_PROCESS);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            TCMPBuffer buffer = new TCMPBuffer();
            buffer.Write(audioBuffer, audioBuffer.length);
            message.AddBuffer(SveProtocol.SVE_VOICE, buffer);

            Log.d(TAG, "Enroller.Post(Callback)");

            return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
                @Override
                public void onResponse(TCMPMessage response) {

                    UpdateTotalBytesSent(audioBuffer.length);
                    UpdateTotalIndependentCalls();

                    int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                    if (error != 0) {
                        HandleError(response);
                        Log.e(TAG, "Enroller.Post(Callback): Error occurred while attempting to post enrollment data. Error: " + error);
                        callback.onFailure(new SveEnroller.EnrollException("Error occurred while attempting to post enrollment data. Error: " + error));
                        return;
                    }

                    if (!HandleResult(response)) {
                        Log.e(TAG, "Enroller.Post(Callback): Error occurred while handling the result.");
                        callback.onFailure(new SveEnroller.EnrollException("Unable to handle response"));
                        return;
                    }

                    Log.d(TAG, "Enroller.Post(Callback): Speech Extracted: " + GetSpeechExtracted() + ", " + GetEnrollResult());

                    callback.onPostComplete(GetEnrollResult());
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
    public SveEnroller.Result End() {
        if(IsConnected() && IsSessionOpen()) {

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_END);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            Log.d(TAG, "Enroller.End()");

            message = mSession.ExpectMessage(message);

            if(!mUsingDefault) {
                mSession.Shutdown();
            }

            if (message == null) {
                Log.e(TAG, "Enroller.End(): Timeout occurred while attempting to end enrollment.");
                return SveEnroller.Result.Timeout;
            }

            int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
            if (error != 0) {
                Log.e(TAG, "Enroller.End(): Error occurred while attempting to end enrollment. Error: " + error);
                HandleError(message);
                return SveEnroller.Result.Error;
            }

            if (!HandleResult(message)) {
                Log.e(TAG, "Enroller.End(): Error occurred while handling the result.");
                return SveEnroller.Result.Error;
            }

            UpdateSessionStatus(false);
            UpdateSessionId(null);

            Log.d(TAG, "Trained Speech: " + GetSpeechTrained() + ", " + GetEnrollResult());

            return GetEnrollResult();
        }
        return SveEnroller.Result.Invalid;
    }

    @Override
    public Boolean End(final SveEnroller.EndCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Enroller.End(Callback): Callback was null");
            return false;
        }

        if(IsConnected() && IsSessionOpen()) {

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_END);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());

            Log.d(TAG, "Enroller.End(Callback)");

            return mSession.PushMessage(message, new TCMPSession.ResponseCallback() {
                @Override
                public void onResponse(TCMPMessage response) {

                    if(!mUsingDefault) {
                        mSession.Shutdown();
                    }

                    int error = (int) response.GetElement(SveProtocol.SVE_ERROR);
                    if (error != 0) {
                        Log.e(TAG, "Enroller.End(Callback): Error occurred while attempting to end enrollment. Error: " + error);
                        HandleError(response);
                        callback.onFailure(new SveEnroller.EnrollException("Error occurred while attempting to end enrollment. Error: " + error));
                        return;
                    }

                    if (!HandleResult(response)) {
                        Log.e(TAG, "Enroller.End(Callback): Error occurred while handling the result.");
                        callback.onFailure(new SveEnroller.EnrollException("Unable to handle response"));
                        return;
                    }

                    UpdateSessionStatus(false);
                    UpdateSessionId(null);

                    Log.d(TAG, "Enroller.Post(Callback): Speech Extracted: " + GetSpeechExtracted() + ", " + GetEnrollResult());

                    callback.onEndComplete(GetEnrollResult());
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

            Log.d(TAG, "Enroller.Cancel()");

            message = mSession.ExpectMessage(message);

            if(!mUsingDefault) {
                mSession.Shutdown();
            }

            if (message == null) {
                UpdateSessionStatus(false);
                UpdateSessionId(null);
                Log.e(TAG, "Enroller.Cancel(): Timeout occurred while attempting to cancel enrollment.");
                return false;
            }

            int error = (int) message.GetElement(SveProtocol.SVE_ERROR);
            if (error != 0) {
                UpdateSessionStatus(false);
                UpdateSessionId(null);
                Log.e(TAG, "Enroller.Cancel(): Error occurred while attempting to cancel enrollment. Error: " + error);
                HandleError(message);
                return false;
            }

            if (!HandleResult(message)) {
                UpdateSessionStatus(false);
                UpdateSessionId(null);
                Log.e(TAG, "Enroller.Cancel(): Error occurred while handling the result.");
                return false;
            }

            UpdateSessionStatus(false);
            UpdateSessionId(null);

            return true;
        }
        return false;
    }

    @Override
    public Boolean Cancel(String reason, final SveEnroller.CancelCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Enroller.Cancel(Callback): Callback was null");
            return false;
        }

        if(IsConnected() && IsSessionOpen()) {

            TCMPMessage message = new TCMPMessage(SVE_SERVICE_TAG);
            message.AddElement(SveProtocol.SVE_VERSION, SVE_API_VERSION);
            message.AddElement(SveProtocol.SVE_METHOD, SVE_API_METHOD_CANCEL);
            message.AddElement(SveProtocol.SVE_SESSION_ID, GetSessionId());
            message.AddElement(SveProtocol.SVE_REASON, reason.replace(' ', '-').substring(0, Math.min(reason.length(), 64)));

            Log.d(TAG, "Enroller.Cancel(Callback)");

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
                        Log.e(TAG, "Enroller.End(Callback): Error occurred while attempting to end enrollment. Error: " + error);
                        callback.onFailure(new SveEnroller.EnrollException("Error occurred while attempting to end enrollment. Error: " + error));
                        return;
                    }

                    if (!HandleResult(response)) {
                        UpdateSessionStatus(false);
                        UpdateSessionId(null);
                        Log.e(TAG, "Enroller.End(Callback): Error occurred while handling the result.");
                        callback.onFailure(new SveEnroller.EnrollException("Unable to handle response"));
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
            String result_type = (String) message.GetElement(SveProtocol.SVE_RESULT_TYPE);
            if (result_type.equals("application/json")) {
                JsonObject o = message.GetElementAsJsonObject(SveProtocol.SVE_RESULT);
                if (o.has("error")) {
                    String data = o.get("error").getAsString();
                    Log.e(TAG, "HandleResult(): Error detected: , Data: " + data);
                    o.remove("error");
                }

                for (Map.Entry<String, JsonElement> elem : o.entrySet()) {
                    if (elem.getKey().equals("profile.enroll")) {
                        mLastProfileIndex = AddProfile(elem.getValue().getAsJsonObject());
                    } else if (elem.getKey().equals("result.enroll")) {
                        JsonObject result_o = elem.getValue().getAsJsonObject();
                        for (Map.Entry<String, JsonElement> result_elem : result_o.entrySet()) {
                            AddResult(result_elem.getKey().toLowerCase(), result_elem.getValue().getAsJsonObject());
                        }

                        String clientId = GetClientId();
                        if (result_o.entrySet().size() > 0 && clientId != null && !clientId.isEmpty()) {
                            SveEnroller.InstanceResult result = GetEnrollResults().get(clientId.toLowerCase());
                            UpdateSpeechExtracted(result.SpeechExtracted);
                            UpdateSpeechTrained(result.SpeechTrained);
                            UpdateEnrollResult(result.Result);
                        }
                    }
                }
                return true;
            } else {
                UpdateEnrollResult(SveEnroller.Result.Error);
                Log.e(TAG, "Expected result content type to be 'application/json'. It was found to be: " + result_type);
                return false;
            }
        } else {
            UpdateEnrollResult(SveEnroller.Result.Error);
            Log.e(TAG, "Malformed Message: Expected field: 'sve.result.type', got nothing.");
            return false;
        }
    }

    private SveEnroller.Result HandleError(TCMPMessage message) {
        SveEnroller.Result result = SveEnroller.Result.Unknown;
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
        } else {
            Log.e(TAG, "Malformed Message: Expected field: 'sve.result.type', got nothing.");
        }
        UpdateEnrollResult(result);
        return result;
    }
}
*/