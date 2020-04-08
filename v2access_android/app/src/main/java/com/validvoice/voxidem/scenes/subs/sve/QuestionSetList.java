package com.validvoice.voxidem.scenes.subs.sve;

import java.util.ArrayList;
import java.util.List;

public class QuestionSetList {

    private int mSetIndex = -1;
    private List<QuestionSet> mQuestionsList;

    public QuestionSetList() {
        mQuestionsList = new ArrayList<>();
    }

    public void addQuestion(QuestionSet.IQuestion question) {
        mQuestionsList.add(new QuestionSet().addQuestion(question));
    }

    public void addQuestionSet(QuestionSet questionSet) {
        mQuestionsList.add(questionSet);
    }

    public void reset() {
        mSetIndex = -1;
        for(QuestionSet set : mQuestionsList) {
            set.reset();
        }
    }

    public int size() {
        return mQuestionsList.size();
    }

    public int current() {
        return mSetIndex;
    }

    public boolean nextSet() {
        ++mSetIndex;
        return hasMoreSets();
    }

    public boolean hasMoreSets() {
        return mSetIndex >= 0 && mSetIndex < mQuestionsList.size();
    }

    public boolean isLastSet() {
        return mSetIndex < 0 || mSetIndex + 1 == size();
    }

    public boolean hasMoreQuestions() {
        if(mSetIndex >= 0 && mSetIndex < mQuestionsList.size()) {
            return mQuestionsList.get(mSetIndex).hasMoreQuestions();
        }
        return false;
    }

    public boolean isLastQuestion(QuestionSet.Mode mode) {
        if(mSetIndex >= 0 && mSetIndex < mQuestionsList.size()) {
            return mQuestionsList.get(mSetIndex).isLastQuestion(mode);
        }
        return false;
    }

    public boolean isResultOverridableQuestion() {
        if(mSetIndex >= 0 && mSetIndex < mQuestionsList.size()) {
            return mQuestionsList.get(mSetIndex).isResultOverridableQuestion();
        }
        return false;
    }

    public QuestionSet.IQuestion getQuestion() {
        if(mSetIndex >= 0 && mSetIndex < mQuestionsList.size()) {
            return mQuestionsList.get(mSetIndex).getQuestion();
        }
        return null;
    }

    public boolean nextQuestion(QuestionSet.Mode mode) {
        if(mSetIndex >= 0 && mSetIndex < mQuestionsList.size()) {
            return mQuestionsList.get(mSetIndex).nextQuestion(mode);
        }
        return false;
    }

}
