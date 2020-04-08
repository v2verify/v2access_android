package com.validvoice.dynamic.speech.authorization.lib.verifiers;

import android.util.Log;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.dynamic.speech.authorization.SveVerifier;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpResponse;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveOkRequestHttpBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class SveVerifier_1 extends ISveVerifier {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "SveIdentifier_1";
    private static final String SESSION_HEADER = "Vv-Session-Id";
    private static final String URI_PATH_PROFILE = "1/sve/Verification/Profile";
    private static final String URI_PATH_START = "1/sve/Verification/%1$s";
    private static final String URI_PATH_PROCESS = "1/sve/Verification";
    private static final String URI_PATH_END = "1/sve/Verification";
    private static final String URI_PATH_CANCEL = "1/sve/Cancel/%1$s";

    ///
    /// Private Variables
    ///
    private int mLastProfileIndex = -1;

    public SveVerifier_1() {
        super();
    }

    @Override
    public SveVerifier.Profile prefetchProfile() {

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
    public Boolean prefetchProfile(final SveVerifier.ProfileCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        boolean result = webAgent().get(URI_PATH_PROFILE, new SveWebAgent.ResponseCallback() {
            @Override
            public void onResponse(SveHttpResponse response) {
                Log.d("onResponse", "prefetchProfile::::::::::::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));

                if(response.getStatusCode() == 200) {
                    if (HandleResult(response)) {
                        callback.onProfileComplete(getProfiles().get(mLastProfileIndex));
                    } else {
                        callback.onFailure(new SveVerifier.VerifyException("Unable to handle response"));
                    }
                } else {
                    SveVerifier.Result result = HandleError(response);
                    callback.onFailure(new SveVerifier.VerifyException(result));
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
        resetVerifier();
        updateSessionId("");
        webAgent().getExtraHeaders().remove(SESSION_HEADER);

        String clientId = getClientId();
        if(clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "start(): Missing Client-Id");
            updateVerifyResult(SveVerifier.Result.Invalid);
            return false;
        }

        HashMap<String, String> headers = new HashMap<>(getMetaData().size() + 2);

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

        String uri = String.format(URI_PATH_START, clientId);
        SveHttpResponse response = webAgent().post(uri, headers);

        getMetaData().clear();

        if(response == null) {
            Log.e(TAG, "webAgent Timed-out: " + uri);
            updateVerifyResult(SveVerifier.Result.Timeout);
        } else {
            try {
                if (response.getStatusCode() == 200) {
                    if (HandleResult(response)) {
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
    public Boolean start(final SveVerifier.StartCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            updateVerifyResult(SveVerifier.Result.Invalid);
            return false;
        }

        resetVerifier();
        updateSessionId("");
        webAgent().getExtraHeaders().remove(SESSION_HEADER);

        final String clientId = getClientId();
        if(clientId == null || clientId.isEmpty()) {
            Log.e(TAG, "start(): Missing Client-Id");
            updateVerifyResult(SveVerifier.Result.Invalid);
            return false;
        }

        HashMap<String, String> headers = new HashMap<>(getMetaData().size() + 2);

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

        final String uri = String.format(URI_PATH_START, clientId);
        boolean result = webAgent().post(uri, headers, new SveWebAgent.ResponseCallback() {
            @Override
            public void onResponse(SveHttpResponse response) {
                Log.d("onResponse", "Response:::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));
                if(response.getStatusCode() == 200) {
                    if (HandleResult(response)) {
                        String sessionId = response.getHeader(SESSION_HEADER);
                        updateSessionId(sessionId);
                        updateSessionStatus(true);
                        webAgent().getExtraHeaders().put(SESSION_HEADER, sessionId);
                        callback.onStartComplete();
                    } else {
                        updateSessionStatus(false);
                        callback.onFailure(new SveVerifier.VerifyException("Unable to handle response"));
                    }
                } else {
                    SveVerifier.Result result = HandleError(response);
                    updateSessionStatus(false);
                    if(result == SveVerifier.Result.NotFound) {
                        callback.onFailure(new SveVerifier.VerifyException(result));
                    } else if(result == SveVerifier.Result.LockedOut){
                        callback.onFailure(new SveVerifier.VerifyException("LockedOut"));
                    }else{
                        callback.onFailure(new SveVerifier.VerifyException("Uri: " + uri, result));
                    }
                }
            }

            @Override
            public void onFailure(Exception ex) {
                updateVerifyResult(SveVerifier.Result.Timeout);
                updateSessionStatus(false);
                callback.onFailure(ex);
            }
        });

        getMetaData().clear();

        return result;
    }

    @Override
    public SveVerifier.Result post() {
        if(isSessionOpen() && !isSessionClosing()) {
            getVerifyResults().clear();

            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for(ContentPart part : getContentParts()) {
                builder.addFormDataPart(part.Name, part.FileName, part.Data.toRequestBody());
            }
            RequestBody requestBody = builder.build();

            SveHttpResponse response = webAgent().post(URI_PATH_PROCESS, getMetaData(),
                    new SveOkRequestHttpBody(requestBody));

            Log.d("onResponse", "post::::::11111::::::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));

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
                return SveVerifier.Result.Timeout;
            } else {
                try {
                    if (response.getStatusCode() != 200) {
                        return HandleError(response);
                    } else {
                        HandleResult(response);
                        Log.d(TAG, "Score: " + getVerifyScore() + ", " + getVerifyResult());
                        return getVerifyResult();
                    }
                } finally {
                    // Have to explicitly close, in case it was not closed already
                    response.close();
                }
            }
        }
        return SveVerifier.Result.Invalid;
    }

    @Override
    public Boolean post(final SveVerifier.PostCallback callback) {
        if(isSessionOpen() && !isSessionClosing()) {
            getVerifyResults().clear();

            final String uri = "1/sve/Verification";
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for(ContentPart part : getContentParts()) {
                builder.addFormDataPart(part.Name, part.FileName, part.Data.toRequestBody());
            }
            final RequestBody requestBody = builder.build();

            boolean result = webAgent().post(uri, getMetaData(), new SveOkRequestHttpBody(requestBody),
                    new SveWebAgent.ResponseCallback() {
                @Override
                public void onResponse(SveHttpResponse response) {
                    Log.d("onResponse", "post::::::222::::::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));
                    try {
                        updateTotalBytesSent(requestBody.contentLength());
                        updateTotalProcessCalls();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    getContentParts().clear();

                    if(response.getStatusCode() == 200) {
                        if (HandleResult(response)) {
                            Log.d(TAG, "Score: " + getVerifyScore() + ", " + getVerifyResult());
                            callback.onPostComplete(getVerifyResult());
                        } else {
                            callback.onFailure(new SveVerifier.VerifyException("Unable to handle verification response"));
                        }
                    } else {
                        callback.onPostComplete(HandleError(response));
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
    public SveVerifier.Result end() {
        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);

            HashMap<String, String> headers = new HashMap<>(getMetaData().size() + 1);

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

            String authToken = getAuthToken();
            if(authToken != null && !authToken.isEmpty()) {
                headers.put("Vv-Override-Token", authToken);
            }

            SveHttpResponse response = webAgent().delete(URI_PATH_END, headers);
            getMetaData().clear();
            if (response == null) {
                Log.e(TAG, "webAgent Timed-out: " + URI_PATH_END);
                return SveVerifier.Result.Timeout;
            } else {

                Log.d("onResponse", "end::::::111::::::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));

                try {
                    if (response.getStatusCode() != 200) {
                        return HandleError(response);
                    } else {
                        updateSessionStatus(false);
                        updateSessionId(null);
                        webAgent().getExtraHeaders().remove(SESSION_HEADER);
                        if (HandleResult(response)) {
                            Log.d(TAG, "Score: " + getVerifyScore() + ", " + getVerifyResult());
                            return getVerifyResult();
                        }
                    }
                } finally {
                    // Have to explicitly close, in case it was not closed already
                    response.close();
                }
            }
        }
        return SveVerifier.Result.Invalid;
    }

    @Override
    public Boolean end(final SveVerifier.EndCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);

            HashMap<String, String> headers = new HashMap<>(getMetaData().size() + 1);

            headers.putAll(getMetaData());

            String authToken = getAuthToken();
            if(authToken != null && !authToken.isEmpty()) {
                headers.put("Vv-Override-Token", authToken);
            }

            boolean result = webAgent().delete(URI_PATH_END, headers, new SveWebAgent.ResponseCallback() {
                @Override
                public void onResponse(SveHttpResponse response) {
                    Log.d("onResponse", "end::::::222::::::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));

                    if (response.getStatusCode() != 200) {
                        HandleError(response);
                        callback.onEndComplete(false);
                    } else {
                        updateSessionStatus(false);
                        updateSessionId(null);
                        webAgent().getExtraHeaders().remove(SESSION_HEADER);
                        if(HandleResult(response)) {
                            Log.d(TAG, "Score: " + getVerifyScore() + ", " + getVerifyResult());
                            callback.onEndComplete(isAuthorized());
                        } else {
                            callback.onFailure(new SveVerifier.VerifyException("Uri: " + URI_PATH_END, getVerifyResult()));
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
    public Boolean cancel(String reason, final SveVerifier.CancelCallback callback) {
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
                    Log.d("onResponse", "cancel:::::222222::::::::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));

                    if (response.getStatusCode() != 200) {
                        SveVerifier.Result result = HandleError(response);
                        callback.onFailure(new SveVerifier.VerifyException("Uri: " + uri, result));
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

            Log.d("onResponse", "HandleResult:::::::::::::::::::"+new GsonBuilder().setPrettyPrinting().create().toJson(response));

            for(Map.Entry<String, JsonElement> elem : o.entrySet()) {
                if(elem.getKey().equals("profile.verify")) {
                    mLastProfileIndex = addProfile(elem.getValue().getAsJsonObject());
                } else if(elem.getKey().equals("result.verify")) {
                    JsonObject result_o = elem.getValue().getAsJsonObject();
                    for(Map.Entry<String, JsonElement> result_elem : result_o.entrySet()) {
                        addResult(result_elem.getKey().toLowerCase(), result_elem.getValue().getAsJsonObject());
                    }

                    String clientId = getClientId();
                    if(result_o.entrySet().size() > 0 && clientId != null && !clientId.isEmpty()) {
                        SveVerifier.InstanceResult result = getVerifyResults().get(clientId.toLowerCase());
                        updateSpeechExtracted(result.SpeechExtracted);
                        updateVerifyResult(result.Result);
                        updateVerifyScore(result.Score);
                    }
                } else if(elem.getKey().equals("result.liveness")) {
                    JsonObject liveness_o = elem.getValue().getAsJsonObject();
                    if(liveness_o.has("is_alive")) {
                        updateIsAlive(liveness_o.get("is_alive").getAsBoolean());
                    } else {
                        updateIsAlive(false);
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
            updateVerifyResult(SveVerifier.Result.Error);
            Log.e(TAG, "Expected result content type to be 'application/json'. It was found to be: " + response.getContentType());
            return false;
        }
    }

    private SveVerifier.Result HandleError(SveHttpResponse response) {
        getExtra().clear();
        SveVerifier.Result result = SveVerifier.Result.Invalid;
        String contentType = response != null ? response.getContentType() : null;
        if(contentType != null && contentType.equals("application/json")) {

            JsonObject o = SveWebAgent.responseContentToJsonObject(response);
            if(o.has("error") && o.has("description")) {
                int code = o.get("error").getAsInt();
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
        } else {
            Log.d(TAG, "HandleError: Unknown Error detectedL:::::::::::::::::::."+response.getStatusCode());
            Log.d(TAG, "HandleError: Unknown Error detectedL:::::::::::::::::::."+response.getContent());
            Log.d(TAG, "HandleError: Unknown Error detectedL:::::::::::::::::::."+response.getAllHeaders());

        }
        updateVerifyResult(result);
        return result;
    }
}
