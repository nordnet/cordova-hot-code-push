package com.nordnetab.chcp.main.config;

import java.util.Map;

/**
 * Created by Nikolay Demyankov on 02.06.16.
 * <p/>
 * Model for fetch update options.
 */
public class FetchUpdateOptions {

    private String configURL;
    private Map<String, String> requestHeaders;

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
