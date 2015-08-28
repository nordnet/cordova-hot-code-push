package com.nordnetab.chcp.network;

import com.nordnetab.chcp.config.ApplicationConfig;

/**
 * Created by Nikolay Demyankov on 24.07.15.
 *
 * Helper class to download application config from the server.
 */
public class ApplicationConfigDownloader extends JsonDownloader<ApplicationConfig> {

    public ApplicationConfigDownloader(String url) {
        super(url);
    }

    @Override
    protected ApplicationConfig createInstance(String json) {
        return ApplicationConfig.fromJson(json);
    }
}