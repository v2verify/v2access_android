package com.validvoice.dynamic.speech.recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SpeechConfig {

    public enum AudioEncoding {
        Unknown,
        Linear8,
        Linear16,
        MuLaw
    }

    public static class SpeechContext {

        private String mGrammar;
        private String mName;
        private List<String> mPhrases;

        private SpeechContext() {
            mGrammar = "";
            mName = UUID.randomUUID().toString();
            mPhrases = new ArrayList<>();
        }

        private SpeechContext(String name) {
            mGrammar = "";
            mName = name;
            mPhrases = new ArrayList<>();
        }

        public String getGrammar() {
            return mGrammar;
        }

        public String getName() {
            return mName;
        }

        public List<String> getPhrases() {
            return Collections.unmodifiableList(mPhrases);
        }
    }

    public static class Builder {

        private AudioEncoding mAudioEncoding = AudioEncoding.Linear16;
        private int mSampleRate = 0;
        private int mMaxAlternatives = 0;
        private String mLanguageCode = "";
        private boolean mClosestMatch = false;
        private List<SpeechContext> mSpeechContexts = new ArrayList<>();

        public Builder() {

        }

        public AudioEncoding getAudioEncoding() {
            return mAudioEncoding;
        }

        public Builder setAudioEncoding(AudioEncoding encoding) {
            mAudioEncoding = encoding;
            return this;
        }

        public int getSampleRate() {
            return mSampleRate;
        }

        public Builder setSampleRate(int sampleRate) {
            mSampleRate = sampleRate;
            return this;
        }

        public int getMaxAlternatives() {
            return mMaxAlternatives;
        }

        public Builder setMaxAlternatives(int maxAlternatives) {
            mMaxAlternatives = maxAlternatives;
            return this;
        }

        public String getLanguageCode() {
            return mLanguageCode;
        }

        public Builder setLanguageCode(String languageCode) {
            mLanguageCode = languageCode;
            return this;
        }

        public boolean getClosestMatch() {
            return mClosestMatch;
        }

        public Builder setClosestMatch(boolean closestMatch) {
            mClosestMatch = closestMatch;
            return this;
        }

        public List<SpeechContext> getSpeechContexts() {
            return Collections.unmodifiableList(mSpeechContexts);
        }

        public Builder addSpeechContext(String grammar) {
            SpeechContext context = new SpeechContext();
            context.mGrammar = grammar;
            mSpeechContexts.add(context);
            return this;
        }

        public Builder addSpeechContext(String name, String grammar) {
            SpeechContext context = new SpeechContext(name);
            context.mGrammar = grammar;
            mSpeechContexts.add(context);
            return this;
        }

        public Builder addSpeechContext(List<String> phrases) {
            SpeechContext context = new SpeechContext();
            context.mPhrases = phrases;
            mSpeechContexts.add(context);
            return this;
        }

        public Builder addSpeechContext(String name, List<String> phrases) {
            SpeechContext context = new SpeechContext(name);
            context.mPhrases = phrases;
            mSpeechContexts.add(context);
            return this;
        }

        public Builder addSpeechContext(String name, String grammar, List<String> phrases) {
            SpeechContext context = new SpeechContext(name);
            context.mGrammar = grammar;
            context.mPhrases = phrases;
            mSpeechContexts.add(context);
            return this;
        }

        public SpeechConfig build() throws Exception {
            if(mAudioEncoding == AudioEncoding.Unknown) {
                throw new Exception("Unknown Audio Encoding");
            } else if(mLanguageCode.isEmpty()) {
                throw new Exception("Language Code is Empty");
            }

            SpeechConfig newConfig = new SpeechConfig();
            newConfig.mAudioEncoding = mAudioEncoding;
            newConfig.mSampleRate = mSampleRate;
            newConfig.mMaxAlternatives = mMaxAlternatives;
            newConfig.mLanguageCode = mLanguageCode;
            newConfig.mClosestMatch = mClosestMatch;
            newConfig.mSpeechContexts = mSpeechContexts;
            return newConfig;
        }

    }

    private AudioEncoding mAudioEncoding = AudioEncoding.Linear16;
    private int mSampleRate = 0;
    private int mMaxAlternatives = 0;
    private String mLanguageCode = "";
    private boolean mClosestMatch = false;
    private List<SpeechContext> mSpeechContexts = new ArrayList<>();

    public AudioEncoding getAudioEncoding() {
        return mAudioEncoding;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getMaxAlternatives() {
        return mMaxAlternatives;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public boolean getClosestMatch() {
        return mClosestMatch;
    }

    public List<SpeechContext> getSpeechContexts() {
        return Collections.unmodifiableList(mSpeechContexts);
    }

    public HashMap<String, Object> getSpeechElements() {
        HashMap<String, Object> out = new HashMap<>();
        out.put("audioEncoding", mAudioEncoding);
        out.put("sampleRate", mSampleRate);
        out.put("maxAlternatives", mMaxAlternatives);
        out.put("closestMatch", mClosestMatch);
        List<HashMap<String, Object>> speechContexts = new ArrayList<>();
        for(SpeechContext context : mSpeechContexts) {
            HashMap<String, Object> speechContext = new HashMap<>();
            speechContext.put("name", context.getName());
            if(!context.getGrammar().isEmpty()) {
                speechContext.put("grammar", context.getGrammar());
            }
            speechContext.put("phrases", context.getPhrases());
            speechContexts.add(speechContext);
        }
        out.put("speechContexts", speechContexts);
        return out;
    }

}
