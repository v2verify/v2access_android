package com.validvoice.dynamic.speech.authorization.lib.http.body;

import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.RequestBody;
import okio.BufferedSink;

public class SveOkRequestHttpBody extends SveHttpBody {

    private RequestBody mBody;

    public SveOkRequestHttpBody(RequestBody body) {
        super("", 0);
        mBody = body;
    }

    /**
     * Returns the number of bytes which will be written to {@code out} when {@link #writeTo} is
     * called, or {@code -1} if that count is unknown.
     *
     * @return The Content-Length of this body.
     */
    @Override
    public long getContentLength() {
        try {
            return mBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Returns the {@code Content-Type} of this body.
     *
     * @return The {@code Content-Type} of this body.
     */
    @Override
    public String getContentType() {
        return mBody.contentType().toString();
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        throw new IllegalStateException("Should not reach here");
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        mBody.writeTo(bufferedSink);
    }

}
