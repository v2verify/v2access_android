package com.validvoice.dynamic.speech.authorization.lib.enrollers;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.dynamic.speech.authorization.SveEnroller;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpResponse;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveOkRequestHttpBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class SveEnroller_1 extends ISveEnroller {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "SveEnroller_1";
    private static final String SESSION_HEADER = "Vv-Session-Id";
    private static final String URI_PATH_PROFILE = "1/sve/Enrollment/Profile";
    private static final String URI_PATH_START = "1/sve/Enrollment/%1$s/%2$s";
    private static final String URI_PATH_PROCESS = "1/sve/Enrollment";
    private static final String URI_PATH_END = "1/sve/Enrollment";
    private static final String URI_PATH_CANCEL = "1/sve/Cancel/%1$s";

    ///
    /// Private Variables
    ///
    private int mLastProfileIndex = -1;

    public SveEnroller_1() {
        super();
    }

    @Override
    public SveEnroller.Profile prefetchProfile() {
        SveHttpResponse response = webAgent().get(URI_PATH_PROFILE);
        getMetaData().clear();
        if(response == null) {
            Log.e(TAG, "webAgent Timed-out: " + URI_PATH_PROFILE);
        } else {
            try {
                if (response.getStatusCode() == 200) {
                    if (HandleResult(response)) {
                        return getProfiles().get(mLastProfileIndex);
                    }
                } else {
                    HandleError(response);
                }
            } finally {
                // Have to explicitly close, in case it was not closed already
                response.close();
            }
        }
        return null;
    }

    @Override
    public Boolean prefetchProfile(final SveEnroller.ProfileCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        boolean result = webAgent().get(URI_PATH_PROFILE, new SveWebAgent.ResponseCallback() {
            @Override
            public void onResponse(SveHttpResponse response) {
                if(response.getStatusCode() == 200) {
                    if (HandleResult(response)) {
                        callback.onProfileComplete(getProfiles().get(mLastProfileIndex));
                    } else {
                        callback.onFailure(new SveEnroller.EnrollException("Unable to handle response"));
                    }
                } else {
                    SveEnroller.Result result = HandleError(response);
                    callback.onFailure(new SveEnroller.EnrollException(result));
                }
            }

            @Override
            public void onFailure(Exception ex) {
                callback.onFailure(ex);
            }
        });

        getMetaData().clear();

        return result;
    }

    @Override
    public Boolean start() {
        resetEnroller();
        updateSessionId("");
        webAgent().getExtraHeaders().remove(SESSION_HEADER);

        String clientId = getClientId();
        Log.d(TAG, "start::::::::::::::::::::::::::::::clientId::::::::::::::"+clientId);
        SveEnroller.Gender gender = getGender();
        if(clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "start(): Missing Client-Id");
            return false;
        } else if(gender == SveEnroller.Gender.Unknown) {
            Log.e(TAG, "start(): has invalid gender type");
            return false;
        }

        HashMap<String, String> headers = new HashMap<>(getMetaData().size() + 3);

        headers.putAll(getMetaData());
        try {
            headers.put("Cloud-Developer-Key", SpeechApi.getDeveloperKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            headers.put("Cloud-Application-Key", SpeechApi.getApplicationKey());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String interactionId = getInteractionId();
        if(interactionId != null && !interactionId.isEmpty()) {
            headers.put("Interaction-Id", interactionId);
        }

        String interactionTag = getInteractionTag();
        if(interactionTag != null && !interactionTag.isEmpty()) {
            headers.put("Interaction-Tag", interactionTag);
        }

        String authToken = getAuthToken();
        if(authToken != null && !authToken.isEmpty()) {
            headers.put("Vv-Override-Token", authToken);
        }

        String uri = String.format(URI_PATH_START, clientId, SveEnroller.getGender(gender));
        SveHttpResponse response = webAgent().post(uri, headers);
        getMetaData().clear();
        if(response == null) {
            Log.e(TAG, "webAgent Timed-out: " + uri);
        } else {
            try {
                if(response.getStatusCode() == 200) {
                    if(HandleResult(response)) {
                        String sessionId = response.getHeader(SESSION_HEADER);
                        updateSessionId(sessionId);
                        updateSessionStatus(true);
                        webAgent().getExtraHeaders().put(SESSION_HEADER, sessionId);
                        return true;
                    }
                    updateSessionStatus(false);
                } else {
                    updateSessionStatus(false);
                    HandleError(response);
                }
            } finally {
                // Have to explicitly close, in case it was not closed already
                response.close();
            }
        }
        return false;
    }

    @Override
    public Boolean start(final SveEnroller.StartCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        resetEnroller();
        updateSessionId("");
        webAgent().getExtraHeaders().remove(SESSION_HEADER);

        String clientId = getClientId();


        SveEnroller.Gender gender = getGender();
        if(clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "start(): Missing Client-Id");
            return false;
        } else if(gender == SveEnroller.Gender.Unknown) {
            Log.e(TAG, "start(): has invalid gender type");
            return false;
        }

        HashMap<String, String> headers = new HashMap<>(getMetaData().size() + 3);

        headers.putAll(getMetaData());
        try {
            headers.put("Cloud-Developer-Key", SpeechApi.getDeveloperKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            headers.put("Cloud-Application-Key", SpeechApi.getApplicationKey());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String interactionId = getInteractionId();
        if(interactionId != null && !interactionId.isEmpty()) {
            headers.put("Interaction-Id", interactionId);
        }

        String interactionTag = getInteractionTag();
        if(interactionTag != null && !interactionTag.isEmpty()) {
            headers.put("Interaction-Tag", interactionTag);
        }

        String authToken = getAuthToken();
        if(authToken != null && !authToken.isEmpty()) {
            headers.put("Vv-Override-Token", authToken);
        }
        Log.d(TAG, "start::::::::::::::::::::::;headers:::::::::::::::::::::::::::::::::::::"+headers);
        final String uri = String.format(URI_PATH_START, clientId, SveEnroller.getGender(gender));
        boolean result = webAgent().post(uri, headers, new SveWebAgent.ResponseCallback() {
            @Override
            public void onResponse(SveHttpResponse response) {

                if(response.getStatusCode() == 200) {
                    if (HandleResult(response)) {
                        String sessionId = response.getHeader(SESSION_HEADER);
                        updateSessionId(sessionId);
                        updateSessionStatus(true);
                        webAgent().getExtraHeaders().put(SESSION_HEADER, sessionId);
                        callback.onStartComplete();
                    } else {
                        updateSessionStatus(false);
                        callback.onFailure(new SveEnroller.EnrollException("Unable to handle response"));
                    }
                } else {
                    SveEnroller.Result result = HandleError(response);
                    updateSessionStatus(false);
                    callback.onFailure(new SveEnroller.EnrollException("Uri: " + uri, result));
                }
            }

            @Override
            public void onFailure(Exception ex) {
                updateSessionStatus(false);
                callback.onFailure(ex);
            }
        });

        getMetaData().clear();

        return result;
    }

    @Override
    public SveEnroller.Result post() {
        if(isSessionOpen() && !isSessionClosing()) {
            getEnrollResults().clear();

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for(ContentPart part : getContentParts()) {
                builder.addFormDataPart(part.Name, part.FileName, part.Data.toRequestBody());
            }
            RequestBody requestBody = builder.build();

            SveHttpResponse response = webAgent().post(URI_PATH_PROCESS, getMetaData(),
                    new SveOkRequestHttpBody(requestBody));

            getMetaData().clear();

            try {
                updateTotalBytesSent(requestBody.contentLength());
                updateTotalProcessCalls();
            } catch (IOException e) {
                e.printStackTrace();
            }

            getContentParts().clear();

            if (response == null) {
                Log.e(TAG, "webAgent Timed-out: " + URI_PATH_PROCESS);
                return SveEnroller.Result.Timeout;
            } else {
                try {
                    if (response.getStatusCode() != 200) {
                        return HandleError(response);
                    } else {
                        HandleResult(response);
                        Log.d(TAG, "Speech Extracted: " + getSpeechExtracted() + ", " + getEnrollResult());
                        return getEnrollResult();
                    }
                } finally {
                    // Have to explicitly close, in case it was not closed already
                    response.close();
                }
            }
        }
        return SveEnroller.Result.Invalid;
    }

    @Override
    public Boolean post(final SveEnroller.PostCallback callback) {
        if(isSessionOpen() && !isSessionClosing()) {
            getEnrollResults().clear();

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for(ContentPart part : getContentParts()) {
                builder.addFormDataPart(part.Name, part.FileName, part.Data.toRequestBody());
            }
            final RequestBody requestBody = builder.build();

            boolean result = webAgent().post(URI_PATH_PROCESS, getMetaData(),
                    new SveOkRequestHttpBody(requestBody), new SveWebAgent.ResponseCallback() {
                @Override
                public void onResponse(SveHttpResponse response) {
                    try {
                        updateTotalBytesSent(requestBody.contentLength());
                        updateTotalProcessCalls();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    getContentParts().clear();

                    if(response.getStatusCode() == 200) {
                        if (HandleResult(response)) {
                            Log.d(TAG, "Speech Extracted: " + getSpeechExtracted() + ", " + getEnrollResult());
                            callback.onPostComplete(getEnrollResult());
                        } else {
                            callback.onFailure(new SveEnroller.EnrollException("Unable to handle enrollment response"));
                        }
                    } else {
                        SveEnroller.Result result = HandleError(response);
                        callback.onFailure(new SveEnroller.EnrollException("Uri: " + URI_PATH_PROCESS, result));
                    }
                }

                @Override
                public void onFailure(Exception ex) {
                    try {
                        updateTotalBytesSent(requestBody.contentLength());
                        updateTotalProcessCalls();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    callback.onFailure(ex);
                }
            });

            getMetaData().clear();

            return result;
        }
        return false;
    }

    @Override
    public SveEnroller.Result end() {
        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);
            SveHttpResponse response = webAgent().delete(URI_PATH_END, getMetaData());
            getMetaData().clear();
            if (response == null) {
                Log.e(TAG, "webAgent Timed-out: " + URI_PATH_END);
                return SveEnroller.Result.Timeout;
            } else {
                try {
                    if (response.getStatusCode() != 200) {
                        return HandleError(response);
                    } else {
                        updateSessionStatus(false);
                        updateSessionId(null);
                        webAgent().getExtraHeaders().remove(SESSION_HEADER);
                        if (HandleResult(response)) {
                            Log.d(TAG, "Trained Speech: " + getSpeechTrained() + ", " + getEnrollResult());
                            return getEnrollResult();
                        }
                    }
                } finally {
                    // Have to explicitly close, in case it was not closed already
                    response.close();
                }
            }
        }
        return SveEnroller.Result.Invalid;
    }

    @Override
    public Boolean end(final SveEnroller.EndCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);
            boolean result = webAgent().delete(URI_PATH_END, getMetaData(), new SveWebAgent.ResponseCallback() {
                @Override
                public void onResponse(SveHttpResponse response) {
                    if (response.getStatusCode() != 200) {
                        SveEnroller.Result result = HandleError(response);
                        callback.onFailure(new SveEnroller.EnrollException(result));
                    } else {
                        updateSessionStatus(false);
                        updateSessionId(null);
                        webAgent().getExtraHeaders().remove(SESSION_HEADER);
                        if(HandleResult(response)) {
                            Log.d(TAG, "Trained Speech: " + getSpeechTrained() + ", " + getEnrollResult());
                            callback.onEndComplete(getEnrollResult());
                        } else {
                            callback.onFailure(new SveEnroller.EnrollException("Uri: " + URI_PATH_END, getEnrollResult()));
                        }
                    }
                }

                @Override
                public void onFailure(Exception ex) {
                    callback.onFailure(ex);
                }
            });

            getMetaData().clear();

            return result;
        }
        return true;
    }

    @Override
    public Boolean cancel(String reason) {
        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);
            String uri = String.format(URI_PATH_CANCEL, reason.replace(' ', '-').substring(0, Math.min(reason.length(), 64)));
            SveHttpResponse response = webAgent().delete(uri);
            getMetaData().clear();
            if (response == null) {
                Log.e(TAG, "webAgent Timed-out: " + uri);
            } else {
                try {
                    if (response.getStatusCode() != 200) {
                        HandleError(response);
                        return false;
                    }
                } finally {
                    // Have to explicitly close, in case it was not closed already
                    response.close();
                }
            }
        }
        updateSessionStatus(false);
        updateSessionId(null);
        webAgent().getExtraHeaders().remove(SESSION_HEADER);
        return true;
    }

    @Override
    public Boolean cancel(String reason, final SveEnroller.CancelCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);
            final String uri = String.format(URI_PATH_CANCEL, reason.replace(' ', '-').substring(0, Math.min(reason.length(), 64)));
            boolean result = webAgent().delete(uri, new SveWebAgent.ResponseCallback() {
                @Override
                public void onResponse(SveHttpResponse response) {
                    if (response.getStatusCode() != 200) {
                        SveEnroller.Result result = HandleError(response);
                        callback.onFailure(new SveEnroller.EnrollException("Uri: " + uri, result));
                    } else {
                        updateSessionStatus(false);
                        updateSessionId(null);
                        webAgent().getExtraHeaders().remove(SESSION_HEADER);
                        callback.onCancelComplete();
                    }
                }

                @Override
                public void onFailure(Exception ex) {
                    callback.onFailure(ex);
                }
            });
            getMetaData().clear();
            return result;
        }
        return true;
    }

    ///
    /// Private Methods
    ///

    private boolean HandleResult(SveHttpResponse response) {
        getExtra().clear();
        if(response.getContentType().equals("application/json")) {
            JsonObject o = SveWebAgent.responseContentToJsonObject(response);
            if(o.has("error")) {
                String data = o.get("error").getAsJsonPrimitive().getAsString();
                Log.e(TAG, "HandleResult(): Error detected: Url: " + response.getRequestUrl() + ", Data: " + data);
                o.remove("error");
            }

            for(Map.Entry<String, JsonElement> elem : o.entrySet()) {
                if(elem.getKey().equals("profile.enroll")) {
                    mLastProfileIndex = addProfile(elem.getValue().getAsJsonObject());
                } else if(elem.getKey().equals("result.enroll")) {
                    JsonObject result_o = elem.getValue().getAsJsonObject();
                    for(Map.Entry<String, JsonElement> result_elem : result_o.entrySet()) {
                        addResult(result_elem.getKey().toLowerCase(), result_elem.getValue().getAsJsonObject());
                    }

                    String clientId = getClientId();
                    if(result_o.entrySet().size() > 0 && clientId != null && !clientId.isEmpty()) {
                        SveEnroller.InstanceResult result = getEnrollResults().get(clientId.toLowerCase());
                        updateSpeechExtracted(result.SpeechExtracted);
                        updateSpeechTrained(result.SpeechTrained);
                        updateEnrollResult(result.Result);
                    }
                } else if(elem.getKey().equals("result.data")) {
                    JsonObject result_o = elem.getValue().getAsJsonObject();
                    for(Map.Entry<String, JsonElement> extra : result_o.entrySet()) {
                        JsonElement extra_elem = extra.getValue();
                        if(extra_elem.isJsonPrimitive()) {
                            JsonPrimitive extra_prim = extra_elem.getAsJsonPrimitive();
                            if(extra_prim.isString()) {
                                getExtra().put(extra.getKey().toLowerCase(), extra_prim.getAsString());
                            } else if(extra_prim.isBoolean()) {
                                getExtra().put(extra.getKey().toLowerCase(), extra_prim.getAsBoolean());
                            } else if(extra_prim.isNumber()) {
                                getExtra().put(extra.getKey().toLowerCase(), extra_prim.getAsNumber());
                            }
                        } else if(extra_elem.isJsonArray()) {
                            getExtra().put(extra.getKey().toLowerCase(), extra_elem.getAsJsonArray());
                        } else if(extra_elem.isJsonObject()) {
                            getExtra().put(extra.getKey().toLowerCase(), extra_elem.getAsJsonObject());
                        }
                    }
                }
            }
            return true;
        } else {
            updateEnrollResult(SveEnroller.Result.Error);
            Log.e(TAG, "Expected result content type to be 'application/json'. It was found to be: " + response.getContentType());
            return false;
        }
    }

    private SveEnroller.Result HandleError(SveHttpResponse response) {
        getExtra().clear();
        SveEnroller.Result result = SveEnroller.Result.Unknown;
        if(response.getContentType().equals("application/json")) {
            JsonObject o = SveWebAgent.responseContentToJsonObject(response);
            if(o.has("error") && o.has("description")) {
                int code = o.get("error").getAsJsonPrimitive().getAsInt();
                String description = o.get("description").getAsString();
                result = getErrorResult(code);
                Log.e(TAG, "HandleError: Error detected. Url: "
                    + response.getRequestUrl()
                        + ", Result: "
                        + result
                        + ", Code: "
                        + code
                        + ", Description: "
                        + description
                );
            }
        }
        updateEnrollResult(result);
        return result;
    }

}
