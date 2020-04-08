package com.validvoice.dynamic.speech.authorization.lib.http;

import java.io.IOException;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

/**
 * The base interface of a http body. It can be implemented by different http libraries such as
 * Apache http, Android URLConnection, Square OKHttp and so on.
 */
public abstract class SveHttpBody {

    private final String contentType;
    private final long contentLength;

    /**
     * Writes the content of this request to {@code out}.
     *
     * @param out
     *          The outputStream the content of this body needs to be written to.
     * @throws IOException
     *           Throws an exception if the content of this body can not be written to {@code out}.
     */
    public abstract void writeTo(OutputStream out) throws IOException;

    /**
     * Creates an {@code SveHttpBody} with given {@code Content-Type} and {@code Content-Length}.
     *
     * @param contentType
     *          The {@code Content-Type} of the {@code SveHttpBody}.
     * @param contentLength
     *          The {@code Content-Length} of the {@code SveHttpBody}.
     */
    public SveHttpBody(String contentType, long contentLength) {
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    /**
     * Returns the number of bytes which will be written to {@code out} when {@link #writeTo} is
     * called, or {@code -1} if that count is unknown.
     *
     * @return The Content-Length of this body.
     */
    public long getContentLength() {
        return contentLength;
    }

    /**
     * Returns the {@code Content-Type} of this body.
     *
     * @return The {@code Content-Type} of this body.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     *
     * @param bufferedSink
     * @throws IOException
     */
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        writeTo(bufferedSink.outputStream());
    }

    public RequestBody toRequestBody() {
        return new RequestBody() {

            @Override
            public long contentLength() throws IOException {
                return getContentLength();
            }

            @Override
            public MediaType contentType() {
                return MediaType.parse(getContentType());
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                SveHttpBody.this.writeTo(sink);
            }
        };
    }
}
