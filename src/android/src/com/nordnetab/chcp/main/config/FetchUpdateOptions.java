package com.nordnetab.chcp.main.config;

import com.nordnetab.chcp.main.utils.JSONUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Nikolay Demyankov on 02.06.16.
 * <p/>
 * Model for fetch update options.
 */
public class FetchUpdateOptions {

    private static final String CONFIG_URL_JSON_KEY = "config-file";
    private static final String REQUEST_HEADERS_JSON_KEY = "request-headers";

    private String configURL;
    private Map<String, String> requestHeaders;

    public FetchUpdateOptions() {
        this(null, null);
    }

    @SuppressWarnings("unchecked")
    public FetchUpdateOptions(final JSONObject json) throws JSONException {
        if (json == null) {
            throw new JSONException("Can't parse null json object");
        }

        this.configURL = json.optString(CONFIG_URL_JSON_KEY, null);

        final JSONObject requestHeadersJson = (JSONObject) json.opt(REQUEST_HEADERS_JSON_KEY);
        if (requestHeadersJson != null) {
            this.requestHeaders = JSONUtils.toFlatStringMap(requestHeadersJson);
        }
    }

    /**
     * Constructor.
     *
     * @param configURL      chcp.json config url on the server
     * @param requestHeaders additional request headers.
     */
    public FetchUpdateOptions(final String configURL, final Map<String, String> requestHeaders) {
        this.configURL = configURL;
        this.requestHeaders = requestHeaders;
    }

    /**
     * Getter for chcp.json config url.
     *
     * @return config url
     */
    public String getConfigURL() {
        return configURL;
    }

    /**
     * Setter for chcp.json config url.
     * Use it in the child class to make object mutable, if needed so.
     *
     * @param configURL config url
     */
    protected void setConfigURL(String configURL) {
        this.configURL = configURL;
    }

    /**
     * Getter for additional request headers.
     *
     * @return request headers
     */
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Setter for request headers.
     * Use it in the child class to make object mutable, if needed so.
     *
     * @param requestHeaders request headers
     */
    protected void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }
}
