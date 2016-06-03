package com.nordnetab.chcp.main.network;

import com.nordnetab.chcp.main.config.ApplicationConfig;

import java.util.Map;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 * <p/>
 * Helper class to download application config from the server.
 *
 * @see ApplicationConfig
 * @see DownloadResult
 */
public class ApplicationConfigDownloader extends JsonDownloader<ApplicationConfig> {

    /**
     * Class constructor
     *
     * @param url            url from where to download application config
     * @param requestHeaders additional request headers
     */
    public ApplicationConfigDownloader(final String url, final Map<String, String> requestHeaders) {
        super(url, requestHeaders);
    }

    @Override
    protected ApplicationConfig createInstance(String json) {
        return ApplicationConfig.fromJson(json);
    }
}