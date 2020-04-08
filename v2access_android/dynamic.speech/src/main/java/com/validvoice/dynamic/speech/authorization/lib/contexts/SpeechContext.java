package com.validvoice.dynamic.speech.authorization.lib.contexts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SpeechContext {

    ///
    /// Private Variables
    ///

    private String mName;
    private String mGrammar;
    private String mLanguageCode;
    private List<String> mPhrases;

    ///
    /// Constructors
    ///

    public SpeechContext() {
        mName = UUID.randomUUID().toString();
        mGrammar = "";
        mLanguageCode = "";
        mPhrases = new ArrayList<>();
    }

    public SpeechContext(String languageCode) {
        mName = UUID.randomUUID().toString();
        mGrammar = "";
        mLanguageCode = languageCode;
        mPhrases = new ArrayList<>();
    }

    public SpeechContext(String languageCode, List<String> phrases) {
        mName = UUID.randomUUID().toString();
        mGrammar = "";
        mLanguageCode = languageCode;
        mPhrases = new ArrayList<>(phrases);
    }

    public SpeechContext(String name, String languageCode, List<String> phrases) {
        mName = name;
        mGrammar = "";
        mLanguageCode = languageCode;
        mPhrases = new ArrayList<>(phrases);
    }

    public SpeechContext(String name, String grammar, String languageCode, List<String> phrases) {
        mName = name;
        mGrammar = grammar;
        mLanguageCode = languageCode;
        mPhrases = new ArrayList<>(phrases);
    }

    ///
    /// Public Methods
    ///

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getGrammar() {
        return mGrammar;
    }

    public void setGrammar(String grammar) {
        mGrammar = grammar;
    }

    public String getLanguageCode() {
        return mLanguageCode;
    }

    public void setLanguageCode(String languageCode) {
        mLanguageCode = languageCode;
    }

    public List<String> getPhrases() {
        return mPhrases;
    }

    public void setPhrases(List<String> phrases) {
        mPhrases = phrases;
    }

    public String toString() {
        return "[LanguageCode: " + mLanguageCode + ", Phrases: " + mPhrases.toString() + "]";
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>(2);
        if(!mName.isEmpty()) {
            map.put("name", mName);
        }
        if(!mGrammar.isEmpty()) {
            map.put("grammar", mGrammar);
        }
        map.put("languageCode", mLanguageCode);
        map.put("phrases", mPhrases);
        return map;
    }

}
