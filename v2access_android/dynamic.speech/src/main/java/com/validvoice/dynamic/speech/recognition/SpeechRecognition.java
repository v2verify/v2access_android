package com.validvoice.dynamic.speech.recognition;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.audio.AudioBuffer;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpResponse;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveJsonHttpBody;

import java.util.HashMap;

public class SpeechRecognition {

    public interface OnSpeechListener {
        void onSpeechResult(SpeechResult result);
        void onSpeechError(Exception ex);
    }

    private SveWebAgent mWebAgent = new SveWebAgent();

    public SpeechRecognition() {

    }

    public SpeechResult recognize(SpeechConfig config, AudioBuffer buffer) {
        SpeechAudio audio;
        try {
            audio = new SpeechAudio.Builder().setAudio(buffer).build();
        } catch(Exception ex) {
            return new SpeechResult.Builder()
                    .setError(500)
                    .setErrorData(ex.getLocalizedMessage())
                    .build();
        }
        return recognize(config, audio);
    }

    public SpeechResult recognize(SpeechConfig config, SpeechAudio audio) {
        String uri = "1/stt/Recognize";
        SveHttpResponse response = mWebAgent.post(uri, buildRequest(config, audio));
        return handleResponse(config, response);
    }

    public void recognize(SpeechConfig config, AudioBuffer buffer, @NonNull OnSpeechListener onSpeechListener) {
        SpeechAudio audio;
        try {
            audio = new SpeechAudio.Builder().setAudio(buffer).build();
        } catch(Exception ex) {
            onSpeechListener.onSpeechError(ex);
            return;
        }
        recognize(config, audio, onSpeechListener);
    }

    public void recognize(final SpeechConfig config, SpeechAudio audio, @NonNull final OnSpeechListener onSpeechListener) {
        String uri = "1/stt/Recognize";
        mWebAgent.post(uri, buildRequest(config, audio), new SveWebAgent.ResponseCallback() {
            @Override
            public void onResponse(SveHttpResponse response) {
                onSpeechListener.onSpeechResult(handleResponse(config, response));
            }

            @Override
            public void onFailure(Exception ex) {
                onSpeechListener.onSpeechError(ex);
            }
        });
    }

    private SveJsonHttpBody buildRequest(SpeechConfig config, SpeechAudio audio) {
        HashMap<String, Object> message = new HashMap<>();
        message.put("config", config.getSpeechElements());
        message.put("audio", audio.getSpeechElements());
        return SveJsonHttpBody.create(message);
    }

    private SpeechResult handleResponse(SpeechConfig config, SveHttpResponse response) {
        SpeechResult.Builder builder = new SpeechResult.Builder();
        if(response == null) {
            return builder.setError(408).build();
        } else if(response.getStatusCode() == 200) {
            if (response.getContentType().equals("application/json")) {
                JsonObject o = SveWebAgent.responseContentToJsonObject(response);
                if(!o.has("__type") || !o.has("code") || !o.has("result")) {
                    return builder.setError(400).build();
                } else {
                    JsonObject result = o.get("result").getAsJsonObject();
                    if(result.has("name")) {
                        builder.setName(result.get("name").getAsString());
                    }

                    if(result.has("error")) {
                        builder.setError(result.get("error").getAsInt());
                    }

                    if(result.has("highestTranscript")) {
                        builder.setHighestTranscript(result.get("highestTranscript").getAsString());
                    }

                    if(result.has("highestConfidence")) {
                        builder.setHighestConfidence(result.get("highestConfidence").getAsFloat());
                    }

                    if(result.has("closestTranscript")) {
                        builder.setClosestTranscript(result.get("closestTranscript").getAsString());
                    }

                    if(result.has("closestDistance")) {
                        builder.setClosestDistance(result.get("closestDistance").getAsInt());
                    }

                    if(result.has("alternatives")) {
                        JsonArray alts = result.get("alternatives").getAsJsonArray();
                        for(JsonElement elem : alts) {
                            if(!elem.isJsonObject()) {
                                continue;
                            }

                            JsonObject alt = elem.getAsJsonObject();

                            int index = 0;
                            if(alt.has("index")) {
                                index = alt.get("index").getAsInt();
                            }

                            String transcript = "";
                            if(alt.has("transcript")) {
                                transcript = alt.get("transcript").getAsString();
                            }

                            float confidence = 0;
                            if(alt.has("confidence")) {
                                confidence = alt.get("confidence").getAsFloat();
                            }

                            builder.addAlternative(index, confidence, transcript);
                        }
                    }
                }
            }
            return builder.setError(400).build();
        } else {
            return builder.setError(response.getStatusCode())
                    .setErrorData(response.getReasonPhrase())
                    .build();
        }
    }
}
