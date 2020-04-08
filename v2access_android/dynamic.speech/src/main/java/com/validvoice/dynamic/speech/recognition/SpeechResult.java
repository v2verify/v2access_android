package com.validvoice.dynamic.speech.recognition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpeechResult {

    public static class Alternative {

        private int mIndex;
        private String mTranscript;
        private float mConfidence;

        private Alternative(float c, String t) {
            mIndex = 0;
            mTranscript = t;
            mConfidence = c;
        }

        private Alternative(int i, float c, String t) {
            mIndex = i;
            mTranscript = t;
            mConfidence = c;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getTranscript() {
            return mTranscript;
        }

        public float getConfidence() {
            return mConfidence;
        }
    }

    public static class Builder {

        private String mName;
        private int mError;
        private String mErrorData;
        private String mHighestTranscript;
        private float mHighestConfidence;
        private String mClosestTranscript;
        private int mClosestDistance;
        private List<Alternative> mAlternatives;

        public Builder() {
            mName = "";
            mError = -1;
            mErrorData = "";
            mHighestTranscript = "";
            mHighestConfidence = 0;
            mClosestTranscript = "";
            mClosestDistance = -1;
            mAlternatives = new ArrayList<>();
        }

        public String getName() {
            return mName;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public int getError() {
            return mError;
        }

        public Builder setError(int error) {
            mError = error;
            return this;
        }

        public int getErrorData() {
            return mError;
        }

        public Builder setErrorData(String data) {
            mErrorData = data;
            return this;
        }

        public String getHighestTranscript() {
            return mHighestTranscript;
        }

        public Builder setHighestTranscript(String highestTranscript) {
            mHighestTranscript = highestTranscript;
            return this;
        }

        public float getHighestConfidence() {
            return mHighestConfidence;
        }

        public Builder setHighestConfidence(float highestConfidence) {
            mHighestConfidence = highestConfidence;
            return this;
        }

        public String getClosestTranscript() {
            return mClosestTranscript;
        }

        public Builder setClosestTranscript(String closestTranscript) {
            mClosestTranscript = closestTranscript;
            return this;
        }

        public int getClosestDistance() {
            return mClosestDistance;
        }

        public Builder setClosestDistance(int closestDistance) {
            mClosestDistance = closestDistance;
            return this;
        }

        public void addAlternative(float c, String t) {
            mAlternatives.add(new Alternative(c, t));
        }

        public void addAlternative(int i, float c, String t) {
            mAlternatives.add(new Alternative(i, c, t));
        }

        public List<Alternative> getAlternatives() {
            return Collections.unmodifiableList(mAlternatives);
        }

        public SpeechResult build() {
            SpeechResult result = new SpeechResult();
            result.mName = mName;
            result.mError = mError;
            result.mHighestTranscript = mHighestTranscript;
            result.mHighestConfidence = mHighestConfidence;
            result.mClosestTranscript = mClosestTranscript;
            result.mClosestDistance = mClosestDistance;
            result.mAlternatives = mAlternatives;
            return result;
        }
    }

    private String mName;
    private int mError;
    private String mHighestTranscript;
    private float mHighestConfidence;
    private String mClosestTranscript;
    private int mClosestDistance;
    private List<Alternative> mAlternatives;

    public String getName() {
        return mName;
    }

    public int getError() {
        return mError;
    }

    public String getHighestTranscript() {
        return mHighestTranscript;
    }

    public float getHighestConfidence() {
        return mHighestConfidence;
    }

    public String getClosestTranscript() {
        return mClosestTranscript;
    }

    public int getClosestDistance() {
        return mClosestDistance;
    }

    public List<Alternative> getAlternatives() {
        return Collections.unmodifiableList(mAlternatives);
    }
}
