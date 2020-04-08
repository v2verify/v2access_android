package com.validvoice.dynamic.speech.authorization.lib;

import android.accounts.NetworkErrorException;
import android.net.NetworkInfo;
import android.util.Log;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.validvoice.dynamic.speech.BuildConfig;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpRequest;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpResponse;
import com.validvoice.dynamic.speech.authorization.lib.http.body.SveNullHttpBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.Dispatcher;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;

import static android.content.ContentValues.TAG;






public class SveWebAgent {

    public interface RouteCallback {
        void onFoundRoute();
        void onNoRoute();
    }

    public interface ResponseCallback {
        void onResponse(SveHttpResponse response);
        void onFailure(Exception ex);
    }

    public enum NetworkStatus {
        Unknown,
        NotConnected,
        Connected
    }

    private OkHttpClient mClient;
    private ClearableCookieJar mCookieJar;
    private NetworkStatus mNetworkStatus;
    private Map<String, String> mExtraHeaders;
    private CookieManager mCookieManager;

    ///
    /// Constructor
    ///

    public SveWebAgent() {
        mExtraHeaders = new HashMap<>();

        Dispatcher dispatcher = new Dispatcher();
        // Set 1, if we have more than 1, then async calls could potentially overlap,
        // the server does not support this
        dispatcher.setMaxRequests(1);
        // Set 1, we only ever go to 1 host, no need to allow more
        dispatcher.setMaxRequestsPerHost(1);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        // The server will never redirect
        builder.followRedirects(false);
        // The training of an enrollment could take a bit of time, depending on the amount of
        // speech provided.
        try {
            builder.readTimeout(SpeechApi.getSocketReadTimeout(), TimeUnit.SECONDS);

            if(SpeechApi.useSecureConnection()) {
                builder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS));
            }

            mCookieJar = new PersistentCookieJar(
                    new SetCookieCache(), new SharedPrefsCookiePersistor(SpeechApi.getContext()));

            builder.cookieJar(mCookieJar);

        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.dispatcher(dispatcher);

        mClient = builder.build();
        mNetworkStatus = NetworkStatus.Unknown;
    }

    public void clear() {
        mCookieJar.clearSession();
        mCookieJar.clear();
    }

    ///
    /// Public Methods
    ///

    public Map<String, String> getExtraHeaders() {
        return mExtraHeaders;
    }

    public NetworkStatus queryNetworkStatus() {
        try {
            if (SpeechApi.getConnectivityManager() == null) {
                mNetworkStatus = NetworkStatus.Unknown;
            } else {
                NetworkInfo activeNetwork = SpeechApi.getConnectivityManager().getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
                    mNetworkStatus = NetworkStatus.Connected;
                } else {
                    mNetworkStatus = NetworkStatus.NotConnected;
                }
            }
            return mNetworkStatus;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return NetworkStatus.Unknown;
    }

    public NetworkStatus getLastKnownNetworkStatus() {
        return mNetworkStatus;
    }

    ///
    /// Web Service Methods
    ///

    public void testRoute(final RouteCallback callback) {
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(HttpUrl.parse(SpeechApi.getServer()))
                    .build();

            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onNoRoute();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    // MUST ALWAYS close the response body ALWAYS
                    response.close();
                    // Doesn't matter if I call was successful, the fact we made it here
                    // means we made a connection to the server and the route exists
                    callback.onFoundRoute();
                }
            });
        } catch(Exception ex) {
            ex.printStackTrace();
            callback.onNoRoute();
        }
    }

    ///
    /// Web Service get Methods
    ///

    public SveHttpResponse get(String urlPath) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.GET)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse get(Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.GET)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse get(String urlPath, Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.GET)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public Boolean get(String urlPath, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.GET)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean get(Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.GET)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean get(String urlPath, Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.GET)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    ///
    /// Web Service post Methods
    ///

    public SveHttpResponse post(String urlPath) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse post(Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse post(String urlPath, Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse post(SveHttpBody body) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setBody(body)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse post(String urlPath, SveHttpBody body) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .setBody(body)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse post(String urlPath, Map<String, String> headers, SveHttpBody body) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .setHeaders(headers)
                        .setBody(body)
                        .build()
        );
        return handleRequest(request);
    }

    public Boolean post(String urlPath, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean post(Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean post(String urlPath, Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean post(SveHttpBody body, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setBody(body)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean post(String urlPath, SveHttpBody body, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .setBody(body)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean post(String urlPath, Map<String, String> headers, SveHttpBody body, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.POST)
                        .setUrlPath(urlPath)
                        .setHeaders(headers)
                        .setBody(body)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    ///
    /// Web Service put Methods
    ///

    public SveHttpResponse put(String urlPath) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse put(Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse put(String urlPath, Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse put(SveHttpBody body) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setBody(body)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse put(String urlPath, SveHttpBody body) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .setBody(body)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse put(String urlPath, Map<String, String> headers, SveHttpBody body) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .setHeaders(headers)
                        .setBody(body)
                        .build()
        );
        return handleRequest(request);
    }

    public Boolean put(String urlPath, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean put(Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean put(String urlPath, Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean put(SveHttpBody body, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setBody(body)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean put(String urlPath, SveHttpBody body, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .setBody(body)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean put(String urlPath, Map<String, String> headers, SveHttpBody body, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.PUT)
                        .setUrlPath(urlPath)
                        .setHeaders(headers)
                        .setBody(body)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    ///
    /// Web Service delete Methods
    ///

    public SveHttpResponse delete(String urlPath) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.DELETE)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse delete(Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.DELETE)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public SveHttpResponse delete(String urlPath, Map<String, String> headers) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.DELETE)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleRequest(request);
    }

    public Boolean delete(String urlPath, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.DELETE)
                        .setUrlPath(urlPath)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean delete(Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.DELETE)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    public Boolean delete(String urlPath, Map<String, String> headers, ResponseCallback callback) {
        Request request = buildRequest(
                new SveHttpRequest.Builder()
                        .setMethod(SveHttpRequest.Method.DELETE)
                        .setUrlPath(urlPath)
                        .addHeaders(headers)
                        .build()
        );
        return handleCallbackRequest(request, callback);
    }

    ///
    /// Private Methods
    ///

    private Request buildRequest(SveHttpRequest sveRequest) {

        try {

            Request.Builder requestBuilder = new Request.Builder();

            String urlPath = sveRequest.getUrlPath();
            if (urlPath != null) {
                requestBuilder.url(HttpUrl.parse(SpeechApi.getServer())
                        .newBuilder()
                        .addPathSegments(sveRequest.getUrlPath())
                        .build()
                );
            } else {
                requestBuilder.url(HttpUrl.parse(SpeechApi.getServer()));
            }

            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"), Locale.getDefault());
            DateFormat fmt = new SimpleDateFormat("Z", Locale.US);
            Headers.Builder headersBuilder = new Headers.Builder();
            for (Map.Entry<String, String> entry : mExtraHeaders.entrySet()) {
                headersBuilder.add(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : sveRequest.getAllHeaders().entrySet()) {
                headersBuilder.add(entry.getKey(), entry.getValue());
            }
            headersBuilder.add("Cloud-Developer-Key", SpeechApi.getDeveloperKey());
            headersBuilder.add("Cloud-Application-Key", SpeechApi.getApplicationKey());
            headersBuilder.add("Interaction-Source", SpeechApi.getApplicationVersion() + " (" + BuildConfig.VERSION_NAME + ")");
            headersBuilder.add("GMT-Offset", fmt.format(calendar.getTime()));
            requestBuilder.headers(headersBuilder.build());

            switch (sveRequest.getMethod()) {
                case GET:
                    requestBuilder.get();
                    break;
                case DELETE:
                    requestBuilder.delete();
                    break;
                case POST:
                    requestBuilder.post(getRequestBody(sveRequest));
                    break;
                case PUT:
                    requestBuilder.put(getRequestBody(sveRequest));
                    break;
                default:
                    throw new IllegalStateException("Unsupported http method " + sveRequest.getMethod().toString());
            }

            return requestBuilder.build();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private SveHttpResponse buildResponse(String requestUrl, Response response)
            throws IOException {
        // Status code
        int statusCode = response.code();

        ResponseBody body = response.body();

        // Content
        InputStream content = body.byteStream();

        // Total size
        int totalSize = (int)body.contentLength();

        // Reason phrase
        String reasonPhrase = response.message();

        // Headers
        Map<String, String> headers = new HashMap<>();
        for (String name : response.headers().names()) {
            headers.put(name, response.header(name));
        }

        // Content type
        String contentType = null;
        if (body.contentType() != null) {
            contentType = body.contentType().toString();
        }

        return new SveHttpResponse.Builder()
                .setStatusCode(statusCode)
                .setContent(content)
                .setTotalSize(totalSize)
                .setRequestUrl(requestUrl)
                .setReasonPhrase(reasonPhrase)
                .setHeaders(headers)
                .setContentType(contentType)
                .build();

    }

    private SveHttpResponse handleRequest(Request request) {
        NetworkStatus status = queryNetworkStatus();
        if(status == NetworkStatus.NotConnected) {
            Log.w("SveWebAgent", "handleCallbackRequest: Error: Network Not Available");
            return null;
        }
        try {
            Log.d("SveWebAgent", "handleRequest: Network Availability: " + status.toString());
            Log.d("SveWebAgent", "handleRequest: Url: " + request.url().toString());
            Log.d("SveWebAgent", "handleRequest: Method: " + request.method());
            Response response = mClient.newCall(request).execute();
            if (response != null) {
                Log.d("SveWebAgent", "handleRequest: Has Response");
                return buildResponse(request.url().toString(), response);
            }
            Log.d("SveWebAgent", "handleRequest: No Response");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Boolean handleCallbackRequest(final Request request, final ResponseCallback callback) {
        NetworkStatus status = queryNetworkStatus();
        mCookieManager = new CookieManager();
        mCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(mCookieManager);
        if(status == NetworkStatus.Unknown || status == NetworkStatus.Connected) {
            try {
                Log.d("SveWebAgent", "handleCallbackRequest: Network Availability: " + status.toString());
                Log.d("SveWebAgent", "handleCallbackRequest: Url: " + request.url().toString());
                Log.d("SveWebAgent", "handleCallbackRequest: Method: " + request.method());
                Log.d("SveWebAgent", "handleCallbackRequest: Headers: " + request.headers());
                mClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("SveWebAgent", "handleCallbackRequest: onFailure");
                        callback.onFailure(e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.d("SveWebAgent", "handleCallbackRequest: onResponse");
                        final int chunkSize = 2048;
                        Log.d(TAG, "onResponse:::::::::::::::::::::::response message::::"+response.message());
                        for (int i = 0; i < (new GsonBuilder().setPrettyPrinting().create().toJson(response).length()); i += chunkSize) {
                            Log.d(TAG,"handleCallbackRequest:::::::::::::::::::::::::::"+(new GsonBuilder().setPrettyPrinting().create().toJson(response).substring(i, Math.min((new GsonBuilder().setPrettyPrinting().create().toJson(response).length()), i + chunkSize))));
                        }
                      //  Log.d("onResponse", "handleCallbackRequest:::::::::::::::::::::::::::"+(new GsonBuilder().setPrettyPrinting().create().toJson(response).length());

                        SveHttpResponse resp = buildResponse(request.url().toString(), response);

                        if (!(request.url().toString().contains("Verification"))){
                            mCookieManager.getCookieStore().removeAll();
                        }
                        try {
                            callback.onResponse(resp);
                        } finally {
                            resp.close();
                        }
                    }
                });
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                callback.onFailure(ex);
            }
        } else {
            Log.w("SveWebAgent", "handleCallbackRequest: Error: Network Not Available");
            callback.onFailure(new NetworkErrorException("Network Not Available"));
        }
        return false;
    }

    private SveOkHttpRequestBody getRequestBody(SveHttpRequest sveRequest) {
        SveHttpBody sveBody = sveRequest.getBody();
        if(sveBody != null) {
            return new SveOkHttpRequestBody(sveBody);
        }
        return new SveOkHttpRequestBody(new SveNullHttpBody());
    }

    ///
    /// Public Static Functions
    ///

    public static String responseContentToString(SveHttpResponse response) {
        String content;
        InputStream responseStream = null;
        try {
            responseStream = response.getContent();
            content = new String(toByteArray(responseStream));
        } catch (IOException e) {
            content = e.getMessage();
        } finally {
            closeQuietly(responseStream);
        }
        return content;
    }

    public static JsonObject responseContentToJsonObject(SveHttpResponse response) {
        String content;
        InputStream responseStream = null;
        try {
            responseStream = response.getContent();
            content = new String(toByteArray(responseStream));
        } catch (IOException e) {
            content = e.getMessage();
        } finally {
            closeQuietly(responseStream);
        }
        return new Gson().fromJson(content, JsonObject.class);
    }

    public static JsonArray responseContentToJsonArray(SveHttpResponse response) {
        String content;
        InputStream responseStream = null;
        try {
            responseStream = response.getContent();
            content = new String(toByteArray(responseStream));
        } catch (IOException e) {
            content = e.getMessage();
        } finally {
            closeQuietly(responseStream);
        }
        return new Gson().fromJson(content, JsonArray.class);
    }

    ///
    /// Private Static Functions
    ///

    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    private static final int EOF = -1;
    private static long copyLarge(InputStream input, OutputStream output, byte[] buffer)
            throws IOException {
        long count = 0;
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static void closeQuietly(InputStream input) {
        if (input != null) {
            try {
                input.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    ///
    /// Private Static Class
    ///

    private static class SveOkHttpRequestBody extends RequestBody {

        private SveHttpBody sveBody;

        public SveOkHttpRequestBody(SveHttpBody sveBody) {
            this.sveBody = sveBody;
        }

        @Override
        public long contentLength() throws IOException {
            return sveBody.getContentLength();
        }

        @Override
        public MediaType contentType() {
            String contentType = sveBody.getContentType();
            return contentType == null ? null : MediaType.parse(sveBody.getContentType());
        }

        @Override
        public void writeTo(BufferedSink bufferedSink) throws IOException {
            sveBody.writeTo(bufferedSink);
        }

        public SveHttpBody getSveHttpBody() {
            return sveBody;
        }
    }

}
