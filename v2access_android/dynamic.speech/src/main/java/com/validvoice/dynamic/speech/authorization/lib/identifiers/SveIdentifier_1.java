package com.validvoice.dynamic.speech.authorization.lib.identifiers;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.speech.authorization.SveIdentifier;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpResponse;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveOkRequestHttpBody;

import java.io.IOException;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class SveIdentifier_1 extends ISveIdentifier {

    ///
    /// Private Static Variables
    ///
    private static final String TAG = "SveIdentifier_1";
    private static final String SESSION_HEADER = "Vv-Session-Id";
    private static final String URI_PATH_PROCESS = "1/sve/Identification";
    private static final String URI_PATH_END = "1/sve/Identification";
    private static final String URI_PATH_CANCEL = "1/sve/Cancel/%1$s";

    ///
    /// Private Variables
    ///

    public SveIdentifier_1() {
        super();
        reset();
    }

    @Override
    public SveIdentifier.Result post() {
        webAgent().clear();
        getIdentifierResults().clear();

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(ContentPart part : getContentParts()) {
            builder.addFormDataPart(part.Name, part.FileName, part.Data.toRequestBody());
        }
        RequestBody requestBody = builder.build();

        SveHttpResponse response;
        if(!isSessionOpen()) {
            response = webAgent().post(URI_PATH_PROCESS, getMetaData(), new SveOkRequestHttpBody(requestBody));
        } else {
            response = webAgent().put(URI_PATH_PROCESS, getMetaData(), new SveOkRequestHttpBody(requestBody));
        }

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
            return SveIdentifier.Result.Timeout;
        } else {
            try {
                if (response.getStatusCode() != 200) {
                    return HandleError(response);
                } else {
                    if(HandleResult(response)) {
                        if(!isSessionOpen() && response.hasHeader(SESSION_HEADER)) {
                            String sessionId = response.getHeader(SESSION_HEADER);
                            updateSessionId(sessionId);
                            updateSessionStatus(true);
                            webAgent().getExtraHeaders().put(SESSION_HEADER, sessionId);
                        }
                    }
                    Log.d(TAG, "Score: " + getVerifyScore() + ", " + getIdentifierResult());
                    return getIdentifierResult();
                }
            } finally {
                // Have to explicitly close, in case it was not closed already
                response.close();
            }
        }
    }

    @Override
    public Boolean post(final SveIdentifier.PostCallback callback) {
        webAgent().clear();
        getIdentifierResults().clear();

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for(ContentPart part : getContentParts()) {
            builder.addFormDataPart(part.Name, part.FileName, part.Data.toRequestBody());
        }
        final RequestBody requestBody = builder.build();

        final SveWebAgent.ResponseCallback responseCallback = new SveWebAgent.ResponseCallback() {
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
                        if(!isSessionOpen() && response.hasHeader(SESSION_HEADER)) {
                            String sessionId = response.getHeader(SESSION_HEADER);
                            updateSessionId(sessionId);
                            updateSessionStatus(true);
                            webAgent().getExtraHeaders().put(SESSION_HEADER, sessionId);
                        }
                        Log.d(TAG, "Score: " + getVerifyScore() + ", " + getIdentifierResult());
                        callback.onPostComplete(getIdentifierResult());
                    } else {
                        callback.onFailure(new SveIdentifier.IdentifierException("Unable to handle verification response"));
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
        };

        boolean result;
        if(!isSessionOpen()) {
            result = webAgent().post(URI_PATH_PROCESS, getMetaData(), new SveOkRequestHttpBody(requestBody), responseCallback);
        } else {
            result = webAgent().put(URI_PATH_PROCESS, getMetaData(), new SveOkRequestHttpBody(requestBody), responseCallback);
        }

        getMetaData().clear();

        return result;
    }

    @Override
    public SveIdentifier.Result end() {
        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);
            SveHttpResponse response = webAgent().delete(URI_PATH_END, getMetaData());
            getMetaData().clear();
            if (response == null) {
                Log.e(TAG, "webAgent Timed-out: " + URI_PATH_END);
                return SveIdentifier.Result.Timeout;
            } else {
                try {
                    if (response.getStatusCode() != 200) {
                        return HandleError(response);
                    } else {
                        updateSessionStatus(false);
                        updateSessionId(null);
                        webAgent().getExtraHeaders().remove(SESSION_HEADER);
                        if (HandleResult(response)) {
                            Log.d(TAG, "Score: " + getVerifyScore() + ", " + getIdentifierResult());
                            return getIdentifierResult();
                        }
                    }
                } finally {
                    // Have to explicitly close, in case it was not closed already
                    response.close();
                }
            }
        }
        return SveIdentifier.Result.Invalid;
    }

    @Override
    public Boolean end(final SveIdentifier.EndCallback callback) {
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
                        callback.onEndComplete(HandleError(response));
                    } else {
                        updateSessionStatus(false);
                        updateSessionId(null);
                        webAgent().getExtraHeaders().remove(SESSION_HEADER);
                        if(HandleResult(response)) {
                            Log.d(TAG, "Score: " + getVerifyScore() + ", " + getIdentifierResult());
                            callback.onEndComplete(getIdentifierResult());
                        } else {
                            callback.onFailure(new SveIdentifier.IdentifierException("Uri: " + URI_PATH_END, getIdentifierResult()));
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
            SveHttpResponse response = webAgent().delete(uri, getMetaData());
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
    public Boolean cancel(String reason, final SveIdentifier.CancelCallback callback) {
        if(callback == null) {
            Log.e(TAG, "Callback was null");
            return false;
        }

        if(isSessionOpen() && !isSessionClosing()) {
            updateSessionClosing(true);
            final String uri = String.format(URI_PATH_CANCEL, reason.replace(' ', '-').substring(0, Math.min(reason.length(), 64)));
            boolean result = webAgent().delete(uri, getMetaData(), new SveWebAgent.ResponseCallback() {
                @Override
                public void onResponse(SveHttpResponse response) {
                    if (response.getStatusCode() != 200) {
                        SveIdentifier.Result result = HandleError(response);
                        callback.onFailure(new SveIdentifier.IdentifierException("Uri: " + uri, result));
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
        if(response.getContentType().equals("application/json")) {
            JsonObject o = SveWebAgent.responseContentToJsonObject(response);
            if(o.has("error")) {
                String data = o.get("error").getAsString();
                Log.e(TAG, "HandleResult(): Error detected: Url: " + response.getRequestUrl() + ", Data: " + data);
                o.remove("error");
            }

            for(Map.Entry<String, JsonElement> elem : o.entrySet()) {
                if(elem.getKey().equals("profile.verify")) {
                    addProfile(elem.getValue().getAsJsonObject());
                } else if(elem.getKey().equals("result.verify")) {
                    JsonObject result_o = elem.getValue().getAsJsonObject();
                    for(Map.Entry<String, JsonElement> result_elem : result_o.entrySet()) {
                        addResult(result_elem.getKey().toLowerCase(), result_elem.getValue().getAsJsonObject());
                    }

                    for(Map.Entry<String, SveIdentifier.InstanceResult> result : getIdentifierResults().entrySet()) {
                        updateClientId(result.getKey());
                        updateSpeechExtracted(result.getValue().SpeechExtracted);
                        updateVerifyResult(result.getValue().Result);
                        updateVerifyScore(result.getValue().Score);
                        break;
                    }

                } else if(elem.getKey().equals("result.liveness")) {
                    JsonObject liveness_o = elem.getValue().getAsJsonObject();
                    if(liveness_o.has("is_alive")) {
                        updateIsAlive(liveness_o.get("is_alive").getAsBoolean());
                    } else {
                        updateIsAlive(false);
                    }
                }
            }
            return true;
        } else {
            updateVerifyResult(SveIdentifier.Result.Error);
            Log.e(TAG, "Expected result content type to be 'application/json'. It was found to be: " + response.getContentType());
            return false;
        }
    }

    private SveIdentifier.Result HandleError(SveHttpResponse response) {
        SveIdentifier.Result result = SveIdentifier.Result.Invalid;
        String contentType = response != null ? response.getContentType() : null;
        if(contentType != null && contentType.equals("application/json")) {
            JsonObject o = SveWebAgent.responseContentToJsonObject(response);
            if(o.has("error") && o.has("description")) {
                int code = o.get("error").getAsInt();
                String description = o.get("description").getAsString();
                result = getErrorResult(code);
                if((result == SveIdentifier.Result.BadEnrollment ||
                        result == SveIdentifier.Result.NotFound) &&
                        o.has("extra")) {
                    String extra = o.get("extra").getAsString();
                    Log.e(TAG, "HandleError: Error detected. Url: "
                            + response.getRequestUrl()
                            + ", Result: "
                            + result
                            + ", Code: "
                            + code
                            + ", Description: "
                            + description
                            + ", Extra: "
                            + extra
                    );
                    updateClientId(extra);
                } else {
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
        } else {
            Log.e(TAG, "HandleError: Unknown Error detected.");
        }
        updateVerifyResult(result);
        return result;
    }
}
