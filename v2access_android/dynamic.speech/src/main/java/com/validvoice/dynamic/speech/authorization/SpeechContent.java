package com.validvoice.dynamic.speech.authorization;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SpeechContent {

    static class ContentPart {

        final byte[] mBuffer;
        final File mFile;
        final SpeechContexts mContexts;

        ContentPart(@NonNull byte[] buffer) {
            mBuffer = buffer;
            mFile = null;
            mContexts = null;
        }

        ContentPart(@NonNull File file) {
            mBuffer = null;
            mFile = file;
            mContexts = null;
        }

        ContentPart(@NonNull byte[] buffer, @Nullable SpeechContexts contexts) {
            mBuffer = buffer;
            mFile = null;
            mContexts = contexts;
        }

        ContentPart(@NonNull File file, @Nullable SpeechContexts contexts) {
            mBuffer = null;
            mFile = file;
            mContexts = contexts;
        }

    }

    List<ContentPart> mParts;

    public SpeechContent() {
        mParts = new ArrayList<>();
    }

    public void add(@NonNull byte[] buffer) {
        mParts.add(new ContentPart(buffer));
    }

    public void add(@NonNull File file) {
        mParts.add(new ContentPart(file));
    }

    public void add(@NonNull byte[] buffer, @Nullable SpeechContexts contexts) {
        mParts.add(new ContentPart(buffer, contexts));
    }

    public void add(@NonNull File file, @Nullable SpeechContexts contexts) {
        mParts.add(new ContentPart(file, contexts));
    }

    public void clear() {
        mParts.clear();
    }

}
