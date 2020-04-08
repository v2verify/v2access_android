package com.validvoice.voxidem.scenes.subs.sve;

import com.validvoice.dynamic.voice.VoiceRecorder;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionSet {

    private static final int DEFAULT_MAX_SPEECH_LENGTH_MILLIS = VoiceRecorder.DEFAULT_SPEECH_MAX_LENGTH_MILLIS;
    private static final int DEFAULT_SPEECH_TIMEOUT_MILLIS = VoiceRecorder.DEFAULT_SPEECH_TIMEOUT_MILLIS;

    public enum Type {
        Question,
        Challenge,
        FreeSpeech
    }

    public enum Mode {
        Question,
        ChallengeOnly,
        RequiredOnly
    }

    public interface IQuestion {
        String getName();
        String getGrammar();
        String getQuestion();
        Type questionType();
        int maxSpeechLengthMillis();
        int speechTimeoutMillis();
        boolean isRequired();
        boolean isResultOverridable();
    }

    public static class QuestionBuilder {

        private String mName;
        private String mGrammar;
        private String mQuestion;
        private int mMaxSpeechLengthMillis;
        private int mSpeechTimeoutMillis;
        private boolean mIsRequired;
        private boolean mIsResultOverridable;

        public QuestionBuilder() {
            mName = "";
            mGrammar = "";
            mQuestion = "";
            mMaxSpeechLengthMillis = DEFAULT_MAX_SPEECH_LENGTH_MILLIS;
            mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;
            mIsRequired = false;
            mIsResultOverridable = false;
        }

        public QuestionBuilder setName(String name) {
            mName = name;
            return this;
        }

        public QuestionBuilder setGrammar(String grammar) {
            mGrammar = grammar;
            return this;
        }

        public QuestionBuilder setQuestion(String question) {
            mQuestion = question;
            return this;
        }

        public QuestionBuilder setMaxSpeechLength(int millis) {
            mMaxSpeechLengthMillis = millis;
            return this;
        }

        public QuestionBuilder setSpeechTimeout(int millis) {
            mSpeechTimeoutMillis = millis;
            return this;
        }

        public QuestionBuilder setRequired(boolean isRequired) {
            mIsRequired = isRequired;
            return this;
        }

        public QuestionBuilder setResultOverridable(boolean isResultOverridable) {
            mIsResultOverridable = isResultOverridable;
            return this;
        }

        public IQuestion build() {
            if(mQuestion.isEmpty()) throw new InvalidParameterException("Missing Question Value");
            return new Question(mName, mGrammar, mQuestion, mMaxSpeechLengthMillis, mSpeechTimeoutMillis, mIsRequired, mIsResultOverridable);
        }
    }

    public static class RandomQuestionBuilder {

        private String mName;
        private String mGrammar;
        private List<String> mQuestions;
        private int mMaxSpeechLengthMillis;
        private int mSpeechTimeoutMillis;
        private boolean mIsRequired;
        private boolean mIsResultOverridable;

        public RandomQuestionBuilder() {
            mName = "";
            mGrammar = "";
            mQuestions = new ArrayList<>();
            mMaxSpeechLengthMillis = DEFAULT_MAX_SPEECH_LENGTH_MILLIS;
            mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;
            mIsRequired = false;
            mIsResultOverridable = false;
        }

        public RandomQuestionBuilder setName(String name) {
            mName = name;
            return this;
        }

        public RandomQuestionBuilder setGrammar(String grammar) {
            mGrammar = grammar;
            return this;
        }

        public RandomQuestionBuilder addQuestion(String question) {
            mQuestions.add(question);
            return this;
        }

        public RandomQuestionBuilder setQuestions(List<String> questions) {
            mQuestions = questions;
            return this;
        }

        public RandomQuestionBuilder setMaxSpeechLength(int millis) {
            mMaxSpeechLengthMillis = millis;
            return this;
        }

        public RandomQuestionBuilder setSpeechTimeout(int millis) {
            mSpeechTimeoutMillis = millis;
            return this;
        }

        public RandomQuestionBuilder setRequired(boolean isRequired) {
            mIsRequired = isRequired;
            return this;
        }

        public RandomQuestionBuilder setResultOverridable(boolean isResultOverridable) {
            mIsResultOverridable = isResultOverridable;
            return this;
        }

        public IQuestion build() {
            if(mQuestions.isEmpty()) throw new InvalidParameterException("Missing 1 or more Question options");
            return new RandomQuestion(mName, mGrammar, mQuestions, mMaxSpeechLengthMillis, mSpeechTimeoutMillis, mIsRequired, mIsResultOverridable);
        }
    }

    public static class SecurityNumberQuestionBuilder {

        private String mName;
        private String mGrammar;
        private int mMax;
        private int mMaxSpeechLengthMillis;
        private int mSpeechTimeoutMillis;
        private boolean mIsResultOverridable;

        public SecurityNumberQuestionBuilder() {
            mName = "";
            mGrammar = "";
            mMax = -1;
            mMaxSpeechLengthMillis = DEFAULT_MAX_SPEECH_LENGTH_MILLIS;
            mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;
            mIsResultOverridable = false;
        }

        public SecurityNumberQuestionBuilder setName(String name) {
            mName = name;
            return this;
        }

        public SecurityNumberQuestionBuilder setGrammar(String grammar) {
            mGrammar = grammar;
            return this;
        }

        public SecurityNumberQuestionBuilder setMax(int max) {
            mMax = max;
            return this;
        }

        public SecurityNumberQuestionBuilder setMaxSpeechLength(int millis) {
            mMaxSpeechLengthMillis = millis;
            return this;
        }

        public SecurityNumberQuestionBuilder setSpeechTimeout(int millis) {
            mSpeechTimeoutMillis = millis;
            return this;
        }

        public SecurityNumberQuestionBuilder setResultOverridable(boolean isResultOverridable) {
            mIsResultOverridable = isResultOverridable;
            return this;
        }

        public IQuestion build() {
            if(mMax == -1) throw new InvalidParameterException("Missing Maximum Number Value");
            return new SecurityNumberQuestion(mName, mGrammar, mMax, mMaxSpeechLengthMillis, mSpeechTimeoutMillis, mIsResultOverridable);
        }
    }

    public static class SecurityPhraseQuestionBuilder {

        private String mName;
        private String mGrammar;
        private String mPhrase;
        private int mMaxSpeechLengthMillis;
        private int mSpeechTimeoutMillis;
        private boolean mIsResultOverridable;

        public SecurityPhraseQuestionBuilder() {
            mName = "";
            mGrammar = "";
            mPhrase = "";
            mMaxSpeechLengthMillis = DEFAULT_MAX_SPEECH_LENGTH_MILLIS;
            mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;
            mIsResultOverridable = false;
        }

        public SecurityPhraseQuestionBuilder setName(String name) {
            mName = name;
            return this;
        }

        public SecurityPhraseQuestionBuilder setGrammar(String grammar) {
            mGrammar = grammar;
            return this;
        }

        public SecurityPhraseQuestionBuilder setPhrase(String phrase) {
            mPhrase = phrase;
            return this;
        }

        public SecurityPhraseQuestionBuilder setMaxSpeechLength(int millis) {
            mMaxSpeechLengthMillis = millis;
            return this;
        }

        public SecurityPhraseQuestionBuilder setSpeechTimeout(int millis) {
            mSpeechTimeoutMillis = millis;
            return this;
        }

        public SecurityPhraseQuestionBuilder setResultOverridable(boolean isResultOverridable) {
            mIsResultOverridable = isResultOverridable;
            return this;
        }

        public IQuestion build() {
            if(mPhrase.isEmpty()) throw new InvalidParameterException("Missing Phrase Value");
            return new SecurityPhraseQuestion(mName, mGrammar, mPhrase, mMaxSpeechLengthMillis, mSpeechTimeoutMillis, mIsResultOverridable);
        }
    }

    public static class FreeSpeechQuestionBuilder {

        private String mName;
        private String mGrammar;
        private String mQuestion;
        private int mMaxSpeechLengthMillis;
        private int mSpeechTimeoutMillis;
        private boolean mIsRequired;
        private boolean mIsResultOverridable;

        public FreeSpeechQuestionBuilder() {
            mName = "";
            mGrammar = "";
            mQuestion = "";
            mMaxSpeechLengthMillis = DEFAULT_MAX_SPEECH_LENGTH_MILLIS;
            mSpeechTimeoutMillis = DEFAULT_SPEECH_TIMEOUT_MILLIS;
            mIsRequired = false;
            mIsResultOverridable = false;
        }

        public FreeSpeechQuestionBuilder setName(String name) {
            mName = name;
            return this;
        }

        public FreeSpeechQuestionBuilder setGrammar(String grammar) {
            mGrammar = grammar;
            return this;
        }

        public FreeSpeechQuestionBuilder setQuestion(String question) {
            mQuestion = question;
            return this;
        }

        public FreeSpeechQuestionBuilder setMaxSpeechLength(int millis) {
            mMaxSpeechLengthMillis = millis;
            return this;
        }

        public FreeSpeechQuestionBuilder setSpeechTimeout(int millis) {
            mSpeechTimeoutMillis = millis;
            return this;
        }

        public FreeSpeechQuestionBuilder setRequired(boolean isRequired) {
            mIsRequired = isRequired;
            return this;
        }

        public FreeSpeechQuestionBuilder setResultOverridable(boolean isResultOverridable) {
            mIsResultOverridable = isResultOverridable;
            return this;
        }

        public IQuestion build() {
            if(mQuestion.isEmpty()) throw new InvalidParameterException("Missing Free Speech Question Value");
            return new FreeSpeechQuestion(mName, mGrammar, mQuestion, mMaxSpeechLengthMillis, mSpeechTimeoutMillis, mIsRequired, mIsResultOverridable);
        }
    }

    private List<IQuestion> mQuestionList;
    private int mNextQuestion = -1;
    private boolean mHasSecurityQuestions = false;
    private boolean mHasOverridableQuestions = false;

    public QuestionSet() {
        mQuestionList = new ArrayList<>();
    }

    public QuestionSet addQuestion(IQuestion question) {
        if(question.questionType() == Type.Challenge) {
            mHasSecurityQuestions = true;
        }
        if(question.isResultOverridable()) {
            mHasOverridableQuestions = true;
        }
        mQuestionList.add(question);
        return this;
    }

    public int size() {
        return mQuestionList.size();
    }

    public void reset() {
        mNextQuestion = -1;
    }

    public int current() {
        return mNextQuestion;
    }

    public boolean nextQuestion() {
        ++mNextQuestion;
        return hasMoreQuestions();
    }

    public boolean nextRequiredQuestion() {
        int nextQuestion = mNextQuestion + 1;
        if(nextQuestion >= 0) {
            while (nextQuestion < size()) {
                IQuestion question = mQuestionList.get(nextQuestion);
                if (question.isRequired()) {
                    mNextQuestion = nextQuestion;
                    return true;
                }
                ++nextQuestion;
            }
        }
        return false;
    }

    public boolean nextSecurityQuestion() {
        int nextQuestion = mNextQuestion + 1;
        if(nextQuestion >= 0) {
            while (nextQuestion < size()) {
                IQuestion question = mQuestionList.get(nextQuestion);
                if (question.questionType() == Type.Challenge) {
                    mNextQuestion = nextQuestion;
                    return true;
                }
                ++nextQuestion;
            }
        }
        return false;
    }

    public boolean nextQuestion(Mode mode) {
        switch(mode) {
            case Question: return nextQuestion();
            case ChallengeOnly: return nextSecurityQuestion();
            case RequiredOnly: return nextRequiredQuestion();
        }
        return false;
    }

    public IQuestion getQuestion() {
        if(mNextQuestion < 0 || mNextQuestion >= size()) {
            throw new IndexOutOfBoundsException("Index Attempted: " + mNextQuestion + ", Size: " + size());
        }
        return mQuestionList.get(mNextQuestion);
    }

    public boolean isLastQuestion() {
        return mNextQuestion < 0 || mNextQuestion + 1 == size();
    }

    public boolean isResultOverridableQuestion() {
        int question = mNextQuestion;
        if(question < 0) {
            question = 0;
        } else if(question >= size()) {
            question = size() - 1;
        }
        return mQuestionList.get(question).isResultOverridable();
    }

    public boolean isLastRequiredQuestion() {
        if(isLastQuestion()) return true;
        int nextQuestion = mNextQuestion;

        if(nextQuestion >= 0) {
            // find last required question
            int lastRequiredQuestion = -1;
            while (nextQuestion < size()) {
                IQuestion question = mQuestionList.get(nextQuestion);
                if (question.isRequired()) {
                    lastRequiredQuestion = nextQuestion;
                }
                ++nextQuestion;
            }

            if (lastRequiredQuestion == -1) return false;
            return mNextQuestion == lastRequiredQuestion;
        }
        return true;
    }

    public boolean isLastSecurityQuestion() {
        if(isLastQuestion()) return true;
        int nextQuestion = mNextQuestion;

        if(nextQuestion >= 0) {
            // find last security question
            int lastSecurityQuestion = -1;
            while (nextQuestion < size()) {
                IQuestion question = mQuestionList.get(nextQuestion);
                if (question.questionType() == Type.Challenge) {
                    lastSecurityQuestion = nextQuestion;
                }
                ++nextQuestion;
            }

            if (lastSecurityQuestion == -1) return false;
            return mNextQuestion == lastSecurityQuestion;
        }
        return true;
    }

    public boolean isLastQuestion(Mode mode) {
        switch(mode) {
            case Question: return isLastQuestion();
            case ChallengeOnly: return isLastSecurityQuestion();
            case RequiredOnly: return isLastRequiredQuestion();
        }
        return false;
    }

    public boolean hasMoreQuestions() {
        return mNextQuestion >= 0 && mNextQuestion < size();
    }

    public boolean hasSecurityQuestions() {
        return mHasSecurityQuestions;
    }

    public boolean hasOverridableQuestions() {
        return mHasOverridableQuestions;
    }

    private static class Question implements IQuestion {

        private final String mName;
        private final String mGrammar;
        private final String mQuestion;
        private final int mMaxSpeechLengthMillis;
        private final int mSpeechTimeoutMillis;
        private final boolean mIsRequired;
        private final boolean mIsResultOverridable;

        Question(String name, String grammar, String question, int length, int speech, boolean isRequired,
                 boolean isResultOverridable) {
            mName = name;
            mGrammar = grammar;
            mQuestion = question;
            mMaxSpeechLengthMillis = length;
            mSpeechTimeoutMillis = speech;
            mIsRequired = isRequired;
            mIsResultOverridable = isResultOverridable;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getGrammar() {
            return mGrammar;
        }

        @Override
        public String getQuestion() {
            return mQuestion;
        }

        @Override
        public Type questionType() {
            return Type.Question;
        }

        @Override
        public int maxSpeechLengthMillis() {
            return mMaxSpeechLengthMillis;
        }

        @Override
        public int speechTimeoutMillis() {
            return mSpeechTimeoutMillis;
        }

        @Override
        public boolean isRequired() {
            return mIsRequired;
        }

        @Override
        public boolean isResultOverridable() {
            return mIsResultOverridable;
        }

    }

    private static class RandomQuestion implements IQuestion {

        private final String mName;
        private final String mGrammar;
        private final List<String> mQuestions;
        private final int mMaxSpeechLengthMillis;
        private final int mSpeechTimeoutMillis;
        private final boolean mIsRequired;
        private final boolean mIsResultOverridable;

        RandomQuestion(String name, String grammar, List<String> questions, int length, int speech, boolean isRequired,
                       boolean isResultOverridable) {
            mName = name;
            mGrammar = grammar;
            mQuestions = questions;
            mMaxSpeechLengthMillis = length;
            mSpeechTimeoutMillis = speech;
            mIsRequired = isRequired;
            mIsResultOverridable = isResultOverridable;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getGrammar() {
            return mGrammar;
        }

        @Override
        public String getQuestion() {
            Random random = new Random();
            int question = random.nextInt(mQuestions.size() - 1);
            return mQuestions.get(question);
        }

        @Override
        public Type questionType() {
            return Type.Question;
        }

        @Override
        public int maxSpeechLengthMillis() {
            return mMaxSpeechLengthMillis;
        }

        @Override
        public int speechTimeoutMillis() {
            return mSpeechTimeoutMillis;
        }

        @Override
        public boolean isRequired() {
            return mIsRequired;
        }

        @Override
        public boolean isResultOverridable() {
            return mIsResultOverridable;
        }

    }

    private static class SecurityNumberQuestion implements IQuestion {

        private final String mName;
        private final String mGrammar;
        private final int mMax;
        private final int mMaxSpeechLengthMillis;
        private final int mSpeechTimeoutMillis;
        private final boolean mIsResultOverridable;

        SecurityNumberQuestion(String name, String grammar, int max, int length, int speech,
                               boolean isResultOverridable) {
            mName = name;
            mGrammar = grammar;
            mMax = max;
            mMaxSpeechLengthMillis = length;
            mSpeechTimeoutMillis = speech;
            mIsResultOverridable = isResultOverridable;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getGrammar() {
            return mGrammar;
        }

        @Override
        public String getQuestion() {
            Random random = new Random();
            StringBuilder builder = new StringBuilder();
            int number = random.nextInt(9) + 1;
            builder.append(number);
            for (int i = 1; i < mMax; ++i) {
                number = random.nextInt(9) + 1;
                builder.append(' ').append(number);
            }
            return builder.toString();
        }

        @Override
        public Type questionType() {
            return Type.Challenge;
        }

        @Override
        public int maxSpeechLengthMillis() {
            return mMaxSpeechLengthMillis;
        }

        @Override
        public int speechTimeoutMillis() {
            return mSpeechTimeoutMillis;
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public boolean isResultOverridable() {
            return mIsResultOverridable;
        }

    }

    private static class SecurityPhraseQuestion implements IQuestion {

        private final String mName;
        private final String mGrammar;
        private final String mPhrase;
        private final int mMaxSpeechLengthMillis;
        private final int mSpeechTimeoutMillis;
        private final boolean mIsResultOverridable;

        SecurityPhraseQuestion(String name, String grammar, String phrase, int length, int speech,
                               boolean isResultOverridable) {
            mName = name;
            mGrammar = grammar;
            mPhrase = phrase;
            mMaxSpeechLengthMillis = length;
            mSpeechTimeoutMillis = speech;
            mIsResultOverridable = isResultOverridable;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getGrammar() {
            return mGrammar;
        }

        @Override
        public String getQuestion() {
            return mPhrase;
        }

        @Override
        public Type questionType() {
            return Type.Challenge;
        }

        @Override
        public int maxSpeechLengthMillis() {
            return mMaxSpeechLengthMillis;
        }

        @Override
        public int speechTimeoutMillis() {
            return mSpeechTimeoutMillis;
        }

        @Override
        public boolean isRequired() {
            return true;
        }

        @Override
        public boolean isResultOverridable() {
            return mIsResultOverridable;
        }
    }

    private static class FreeSpeechQuestion implements IQuestion {

        private final String mName;
        private final String mGrammar;
        private final String mQuestion;
        private final int mMaxSpeechLengthMillis;
        private final int mSpeechTimeoutMillis;
        private final boolean mIsRequired;
        private final boolean mIsResultOverridable;

        FreeSpeechQuestion(String name, String grammar, String question, int length, int speech,
                           boolean isRequired,  boolean isResultOverridable) {
            mName = name;
            mGrammar = grammar;
            mQuestion = question;
            mMaxSpeechLengthMillis = length;
            mSpeechTimeoutMillis = speech;
            mIsRequired = isRequired;
            mIsResultOverridable = isResultOverridable;
        }

        @Override
        public String getName() {
            return mName;
        }

        @Override
        public String getGrammar() {
            return mGrammar;
        }

        @Override
        public String getQuestion() {
            return mQuestion;
        }

        @Override
        public Type questionType() {
            return Type.FreeSpeech;
        }


        @Override
        public int maxSpeechLengthMillis() {
            return mMaxSpeechLengthMillis;
        }

        @Override
        public int speechTimeoutMillis() {
            return mSpeechTimeoutMillis;
        }

        @Override
        public boolean isRequired() {
            return mIsRequired;
        }

        @Override
        public boolean isResultOverridable() {
            return mIsResultOverridable;
        }
    }

}
