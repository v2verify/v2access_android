package com.validvoice.dynamic.speech.authorization.lib.http;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The http response we receive from sve server. Instances of this class are not immutable. The
 * response body may be consumed only once. The other fields are immutable.
 */
public final class SveHttpResponse {

    /**
     * Builder for {@code SveHttpResponse}.
     */
    public static final class Builder {

        private int statusCode;
        private InputStream content;
        private long totalSize;
        private String reasonPhrase;
        private String requestUrl;
        private Map<String, String> headers;
        private String contentType;

        /**
         * Creates an empty {@code Builder}.
         */
        public Builder() {
            this.totalSize = -1;
            this.headers = new HashMap<>();
        }

        /**
         * Makes a new {@code Builder} based on the given {@code SveHttpResponse}.
         *
         * @param response
         *          The {@code SveHttpResponse} where the {@code Builder}'s values come from.
         */
        public Builder(SveHttpResponse response) {
            super();
            this.setStatusCode(response.getStatusCode());
            this.setContent(response.getContent());
            this.setTotalSize(response.getTotalSize());
            this.setRequestUrl(response.getRequestUrl());
            this.setContentType(response.getContentType());
            this.setHeaders(response.getAllHeaders());
            this.setReasonPhrase(response.getReasonPhrase());
        }

        /**
         * Sets the status code of this {@code Builder}.
         *
         * @param statusCode
         *          The status code of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setStatusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        /**
         * Sets the content of this {@code Builder}.
         *
         * @param content
         *          The content of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setContent(InputStream content) {
            this.content = content;
            return this;
        }

        /**
         * Sets the total size of this {@code Builder}.
         *
         * @param totalSize
         *          The total size of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setTotalSize(long totalSize) {
            this.totalSize = totalSize;
            return this;
        }

        /**
         * Sets the reason phrase of this {@code Builder}.
         *
         * @param reasonPhrase
         *          The reason phrase of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setReasonPhrase(String reasonPhrase) {
            this.reasonPhrase = reasonPhrase;
            return this;
        }

        /**
         * Sets headers of this {@code Builder}. All existing headers will be cleared.
         *
         * @param headers
         *          The headers of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setHeaders(Map<String, String> headers) {
            this.headers = new HashMap<>(headers);
            return this;
        }

        /**
         * Adds headers to this {@code Builder}.
         *
         * @param headers
         *          The headers that need to be added.
         * @return This {@code Builder}.
         */
        public Builder addHeaders(Map<String, String> headers) {
            this.headers.putAll(headers);
            return this;
        }

        /**
         * Adds a header to this {@code Builder}.
         *
         * @param name
         *          The name of the header.
         * @param value
         *          The value of the header.
         * @return This {@code Builder}.
         */
        public Builder addHeader(String name, String value) {
            headers.put(name, value);
            return this;
        }

        /**
         * Sets the content type of this {@code Builder}.
         *
         * @param contentType
         *          The {@code Content-Type} of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder setRequestUrl(String requestUrl) {
            this.requestUrl = requestUrl;
            return this;
        }

        /**
         * Builds a {@link SveHttpResponse} by this {@code Builder}.
         *
         * @return A {@link SveHttpResponse} built on this {@code Builder}.
         */
        public SveHttpResponse build() {
            return new SveHttpResponse(this);
        }
    }

    private final int statusCode;
    private final InputStream content;
    private final long totalSize;
    private final String requestUrl;
    private final String reasonPhrase;
    private final Map<String, String> headers;
    private final String contentType;
    private boolean contentClosed;

    private SveHttpResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.content = builder.content;
        this.totalSize = builder.totalSize;
        this.reasonPhrase = builder.reasonPhrase;
        this.requestUrl = builder.requestUrl;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.contentType = builder.contentType;
        contentClosed = false;
    }

    /**
     * Gets the status code of this {@code SveHttpResponse}.
     *
     * @return The status code of this {@code SveHttpResponse}.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the content of the {@code SveHttpResponse}'s body. The content can only
     * be read once and can't be reset.
     *
     * @return The content of the {@code SveHttpResponse}'s body.
     */
    public InputStream getContent() {
        return content;
    }

    /**
     * Returns the size of the {@code SveHttpResponse}'s body. {@code -1} if the size of the
     * {@code SveHttpResponse}'s body is unknown.
     *
     * @return The size of the {@code SveHttpResponse}'s body.
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Gets the Url of this {@code SveHttpResponse}.
     *
     * @return The Url of this {@code SveHttpResponse}.
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Gets the reason phrase of this {@code SveHttpResponse}.
     *
     * @return The reason phrase of this {@code SveHttpResponse}.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * Gets the {@code Content-Type} of this {@code SveHttpResponse}.
     *
     * @return The {@code Content-Type} of this {@code SveHttpResponse}.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Retrieves the header value from this {@code SveHttpResponse} by the given header name.
     *
     * @param name
     *          The name of the header.
     * @return The value of the header.
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Tells if a header is in the header collection
     *
     * @param name The name of the header
     * @return True/False depending on whether the header is found
     */
    public boolean hasHeader(String name) {
        return headers.containsKey(name);
    }

    /**
     * Gets all headers from this {@code SveHttpResponse}.
     *
     * @return The headers of this {@code SveHttpResponse}.
     */
    public Map<String, String> getAllHeaders() {
        return headers;
    }

    /**
     * Close the HTTP Response Body.
     *
     * NOTE: MUST BE CALLED OTHERWISE RESPONSE BODY WILL LEAK
     */
    public void close() {
        if(!contentClosed && content != null) {
            try {
                content.close();
                contentClosed = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
