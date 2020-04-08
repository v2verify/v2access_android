package com.validvoice.dynamic.speech.authorization.lib.http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The http request we send to sve server. Instances of this class are not immutable. The
 * request body may be consumed only once. The other fields are immutable.
 */
public class SveHttpRequest {

    /**
     * The {@code SveHttpRequest} method type.
     */
    public enum Method {

        GET, POST, PUT, DELETE;

        /**
         * Creates a {@code Method} from the given string. Valid stings are {@code GET}, {@code POST},
         * {@code PUT} and {@code DELETE}.
         *
         * @param string
         *          The string value of this {@code Method}.
         * @return A {@code Method} based on the given string.
         */
        public static Method fromString(String string) {
            Method method;
            switch (string) {
                case "GET":
                    method = GET;
                    break;
                case "POST":
                    method = POST;
                    break;
                case "PUT":
                    method = PUT;
                    break;
                case "DELETE":
                    method = DELETE;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid http method: <" + string + ">");
            }
            return method;
        }

        /**
         * Returns a string value of this {@code Method}.
         * @return The string value of this {@code Method}.
         */
        @Override
        public String toString() {
            String string;
            switch (this) {
                case GET:
                    string = "GET";
                    break;
                case POST:
                    string = "POST";
                    break;
                case PUT:
                    string = "PUT";
                    break;
                case DELETE:
                    string = "DELETE";
                    break;
                default:
                    throw new IllegalArgumentException("Invalid http method: <" + this+ ">");
            }
            return string;
        }
    }

    /**
     * Builder of {@code SveHttpRequest}.
     */
    public static final class Builder {

        private String urlPath;
        private Method method;
        private Map<String, String> headers;
        private SveHttpBody body;

        /**
         * Creates an empty {@code Builder}.
         */
        public Builder() {
            this.headers = new HashMap<>();
        }

        /**
         * Creates a new {@code Builder} based on the given {@code ParseHttpRequest}.
         *
         * @param request
         *          The {@code ParseHttpRequest} where the {@code Builder}'s values come from.
         */
        public Builder(SveHttpRequest request) {
            this.urlPath = request.urlPath;
            this.method = request.method;
            this.headers = new HashMap<>(request.headers);
            this.body = request.body;
        }

        /**
         * Sets the urlPath of this {@code Builder}.
         *
         * @param urlPath
         *          The urlPath of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setUrlPath(String urlPath) {
            this.urlPath = urlPath;
            return this;
        }

        /**
         * Sets the {@link Method} of this {@code Builder}.
         *
         * @param method
         *          The {@link Method} of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setMethod(Method method) {
            this.method = method;
            return this;
        }

        /**
         * Sets the {@link SveHttpBody} of this {@code Builder}.
         *
         * @param body
         *          The {@link SveHttpBody} of this {@code Builder}.
         * @return This {@code Builder}.
         */
        public Builder setBody(SveHttpBody body) {
            this.body = body;
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
         * Builds a {@link SveHttpRequest} based on this {@code Builder}.
         *
         * @return A {@link SveHttpRequest} built on this {@code Builder}.
         */
        public SveHttpRequest build() {
            return new SveHttpRequest(this);
        }
    }

    private final String urlPath;
    private final Method method;
    private final Map<String, String> headers;
    private final SveHttpBody body;

    private SveHttpRequest(Builder builder) {
        this.urlPath = builder.urlPath;
        this.method = builder.method;
        this.headers = Collections.unmodifiableMap(new HashMap<>(builder.headers));
        this.body = builder.body;
    }

    /**
     * Gets the url of this {@code SveHttpRequest}.
     *
     * @return The url of this {@code SveHttpRequest}.
     */
    public String getUrlPath() {
        return urlPath;
    }

    /**
     * Gets the {@code Method} of this {@code SveHttpRequest}.
     *
     * @return The {@code Method} of this {@code SveHttpRequest}.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets all headers from this {@code SveHttpRequest}.
     *
     * @return The headers of this {@code SveHttpRequest}.
     */
    public Map<String, String> getAllHeaders() {
        return headers;
    }

    /**
     * Retrieves the header value from this {@code SveHttpRequest} by the given header name.
     *
     * @param name
     *          The name of the header.
     * @return The value of the header.
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Gets http body of this {@code SveHttpRequest}.
     *
     * @return The http body of this {@code SveHttpRequest}.
     */
    public SveHttpBody getBody() {
        return body;
    }

}
