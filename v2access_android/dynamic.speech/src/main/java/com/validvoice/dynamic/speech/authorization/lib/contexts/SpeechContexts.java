package com.validvoice.dynamic.speech.authorization.lib.contexts;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpeechContexts extends ArrayList<SpeechContext> {

    public SpeechContexts() {

    }

    public SpeechContexts(int capacity) {
        super(capacity);
    }

    public SpeechContexts(@NonNull Collection<SpeechContext> c) {
        super(c);
    }

    public void add(String languageCode, List<String> phrases) {
        super.add(new SpeechContext(languageCode, phrases));
    }

    public void add(String name, String languageCode, List<String> phrases) {
        super.add(new SpeechContext(name, languageCode, phrases));
    }

    public void add(String name, String grammar, String languageCode, List<String> phrases) {
        super.add(new SpeechContext(name, grammar, languageCode, phrases));
    }

}
